package collector;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import json.InfoForPost;
import json.TrackInfo;
import json.db.InfoAboutRelease;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;
import utils.CheckDate;
import utils.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public interface CollectorInterface extends NfoExtractionInterface {
   default InfoForPost collect(File[] audioFiles, String releaseName, String link,
                               String artLink, File folderToCollect, String postCategory) {
      InfoForPost info = null;
      
      String Artist = "No Info";
      String Album = "No Info";
      String Genre = "No Info";
      String Released = "No Info";
      String Tracks = "No Info";
      String Playtime = "No Info";
      String Group = "No Info";
      String Format = "No Info";
      String Bitrate = "No Info";
      String Sample_Rate = "No Info";
      String Size = "No Info";
      HashMap<Integer, TrackInfo> TrackList = new HashMap<>();
      
      try {
         if (postCategory.equals("SCENE-MVID")) {
            String[] folderElements = folderToCollect.getName().split("-");
            Group = folderElements[folderElements.length - 1];
            File nfo = Arrays.stream(folderToCollect.listFiles())
              .filter(file -> file.getName().endsWith(".nfo"))
              .findFirst()
              .orElse(null);
            if (nfo != null) {
               if (Group.toLowerCase().contains("srp")) {
                  String nfoSt = FileUtils.readFileToString(nfo, "UTF-8")
                    .replaceAll("\\P{Print}", "")
                    .trim();
                  Tracks = "1";
                  Artist = getFromNfo(nfoSt, "Artist", "Track Title");
                  String title = getFromNfo(nfoSt, "Title", "Genre");
                  Genre = getFromNfo(nfoSt, "Genre", "Year");
                  Released = getFromNfo(nfoSt, "Year", "Rip date");
                  Size = getFromNfo(nfoSt, "Size", "Resolution");
                  Playtime = getFromNfo(nfoSt, "Length", "Size");
                  Format = getFromNfo(nfoSt, "Format", "Audio");
                  String audio = getFromNfo(nfoSt, "Audio", "Deinterlace");
                  Sample_Rate = Arrays.stream(audio.split(" "))
                    .filter(s -> s.contains("Hz"))
                    .findFirst()
                    .orElse("???Hz");
                  Bitrate = Arrays.stream(audio.split(" "))
                    .filter(s -> s.contains("kbps"))
                    .findFirst()
                    .orElse("???kbps");
                  TrackList.put(0, new TrackInfo(title, Artist, Playtime));
               } else if (Group.toLowerCase().contains("gfvid")) {
                  String nfoSt = FileUtils.readFileToString(nfo, "UTF-8")
                    .trim();
                  String title = getFromNfo(nfoSt, "TITLE:", "TV DATE:");
                  Artist = title.split(" - ")[0];
                  Genre = getFromNfo(nfoSt, "GENRE", "SUBGENRE");
                  Released = getFromNfo(nfoSt, "SHOW DATE", "RUNTIME");
                  Size = getFromNfo(nfoSt, "SIZE", "ARCHIVES");
                  Playtime = getFromNfo(nfoSt, "RUNTIME", "GENRE");
                  Format = getFromNfo(nfoSt, "CODEC", "BITRATE");
                  Bitrate = getFromNfo(nfoSt, "AUDIO", "INFOS");
                  Sample_Rate = getFromNfo(nfoSt, "INFOS", "FASHION");
                  String trackList = getFromNfoSpace(nfoSt, "TRACKLIST", "NOTES");
                  if (trackList.replaceAll("\n", "")
                    .replaceAll(" ", "").equals("")) {
                     TrackList.put(0, new TrackInfo(title.split(" - ")[1],
                       Artist, Playtime));
                  } else {
                     String[] tracks = trackList.split("\n");
                     for (int i = 0; i < tracks.length; i++) {
                        String[] trackSplit = tracks[i].split(" {2}");
                        TrackList.put(i, new TrackInfo(trackSplit[0], Artist,
                          trackSplit[trackSplit.length - 1]));
                     }
                  }
                  Tracks = String.valueOf(TrackList.size());
               } else if (Group.toLowerCase().contains("iuf")) {
                  String nfoSt = FileUtils.readFileToString(nfo, "UTF-8")
                    .replaceAll("\\P{Print}", "")
                    .trim();
                  Tracks = "1";
                  Artist = getFromNfoDot(nfoSt, "Artist", "Title");
                  String title = getFromNfoDot(nfoSt, "Title", "Genre");
                  Genre = getFromNfoDot(nfoSt, "Genre", "Video Year");
                  Released = getFromNfoDot(nfoSt, "Rel.Date", "Encoding Info");
                  Size = getFromNfoDot(nfoSt, "Size", "octets");
                  long fileSizeInKB = Long.parseLong(Size) / 1024;
                  long fileSizeInMB = fileSizeInKB / 1024;
                  Size = fileSizeInMB + " MB";
                  Playtime = getFromNfoDot(nfoSt, "Length", "Size");
                  Format = getFromNfoDot(nfoSt, "Format", "Resolution");
                  Bitrate = getFromNfoDot(nfoSt, "Bitrate", "Deinterlace");
                  TrackList.put(0, new TrackInfo(title, Artist, Playtime));
               } else if (Group.toLowerCase().contains("pmv")) {
                  String nfoSt = FileUtils.readFileToString(nfo, "UTF-8")
                    .replaceAll("\\P{Print}", "")
                    .trim();
                  Artist = getFromNfo(nfoSt, "ARTiST", "TiTLE");
                  String title = getFromNfo(nfoSt, "TiTLE", "REL.DATE")
                    .replaceAll("~", "").trim();
                  Genre = getFromNfo(nfoSt, "GENRE", "FORMAT")
                    .replaceAll("~", "").trim();
                  Released = getFromNfo(nfoSt, "REL.DATE", "AIR DATE");
                  Size = getFromNfo(nfoSt, "SIZE", "Bytes");
                  long fileSizeInKB = Long.parseLong(Size) / 1024;
                  long fileSizeInMB = fileSizeInKB / 1024;
                  Size = fileSizeInMB + " MB";
                  Playtime = getFromNfo(nfoSt, "LENGTH", "DAR/SAR");
                  Format = getFromNfo(nfoSt, "VIDEO CODEC", "AUDIO CODEC");
                  Bitrate = getFromNfo(nfoSt, "AVERAGE BITRATE:", "CONTAINER");
                  TrackList.put(0, new TrackInfo(title, Artist, Playtime));
               }
            }
         } else {
            int playtime = 0;
            long size = 0;
            // ==============VIDEO==============
            if (postCategory.equals("RECORDPOOL VIDEOS")) {
               // set info_table
               Artist = "Mixed";
               Album = "Mixed";
               Genre = "Mixed";
               Released = CheckDate.getTodayForPost();
               Tracks = Integer.toString(audioFiles.length);
               Group = "MyRecordPool";
               Format = "MP4";
               Bitrate = "320" + " Kbps";
               Sample_Rate = "44100" + " Hz";
               // set track_list
               int key = 0;
               for (File video_file : audioFiles) {
                  Log.write("File Collecting: " + video_file.getName(),
                    "Collector");
                  size += video_file.length();
                  Map<Integer, MetaValue> itunesMeta = null;
                  try {
                     MetadataEditor mediaMeta = MetadataEditor.createFrom(video_file);
                     itunesMeta = mediaMeta.getItunesMeta();
                  } catch (AssertionError | IllegalArgumentException | NullPointerException e1) {
                     Log.write("Exception in Collector: " + e1, "Collector");
                  }
                  String title;
                  try {
                     title = itunesMeta.get(-1452383891).toString();
                  } catch (NullPointerException e) {
                     try {
                        String[] fileInfo = video_file.getName().split(" - ");
                        title = fileInfo[1].replace(".mp4", "");
                     } catch (ArrayIndexOutOfBoundsException e1) {
                        title = "No Info";
                     }
                  }
                  String artist;
                  try {
                     artist = itunesMeta.get(-1455336876).toString();
                  } catch (NullPointerException e) {
                     String[] fileInfo = video_file.getName().split(" - ");
                     artist = fileInfo[0].replace(".mp4", "");
                     //						artist = "No Info";
                  }
                  // Length
                  try {
                     int trackLength = getTrackLength(video_file.getAbsolutePath());
                     playtime = getPlaytime(playtime, TrackList,
                       key, title, artist, trackLength);
                  } catch (Exception e) {
                     Log.write(e, "Collector");
                  }
                  key++;
                  Log.write("Info About File Collected: " + video_file.getName(),
                    "Collector");
               }
            }
            // ==============MUSIC==============
            else {
               // set info_table
               AudioFile audioFile = AudioFileIO.read(audioFiles[0]);
               Tag tag = audioFile.getTag();
               if (tag != null) {
                  Artist = tag.getFirst(FieldKey.ARTIST);
                  Album = tag.getFirst(FieldKey.ALBUM);
                  if (!tag.getFirst(FieldKey.GENRE).equals("")) {
                     Genre = tag.getFirst(FieldKey.GENRE);
                  } else {
                     Genre = "Mixed";
                  }
                  if (postCategory.contains("RECORDPOOL") || postCategory.equals("BEATPORT")) {
                     Released = CheckDate.getTodayForPost();
                  }
                  if (postCategory.equals("SCENE-MP3")) {
                     Mp3File mp3file;
                     try {
                        mp3file = new Mp3File(audioFiles[0]);
                        Released = mp3file.getId3v1Tag().getYear();
                     } catch (UnsupportedTagException | InvalidDataException |
                       NullPointerException ignored) {
                     }
                  }
                  if (postCategory.equals("SCENE-FLAC")) {
                     Released = tag.getFirst(FieldKey.YEAR);
                  }
                  Tracks = Integer.toString(audioFiles.length);
                  String[] folderElements = folderToCollect.getName().split("-");
                  if (postCategory.contains("RECORDPOOL") || postCategory.equals("BEATPORT")) {
                     Group = "MyRecordPool";
                  } else {
                     Group = folderElements[folderElements.length - 1];
                  }
                  Format = audioFile.getAudioHeader().getFormat().toUpperCase();
                  Bitrate = audioFile.getAudioHeader().getBitRate() + " Kbps";
                  Sample_Rate = audioFile.getAudioHeader().getSampleRate() + " Hz";
               }
               // set track_list
               int key = 0;
               for (File audio_file : audioFiles) {
                  Log.write("Collecting Info About Music File: " + audio_file.getName(),
                    "Collector");
                  size += audio_file.length();
                  AudioFile track = null;
                  try {
                     track = AudioFileIO.read(audio_file);
                  } catch (CannotReadException e) {
                     Log.write("Exception: " + e, "Collector");
                  }
                  try {
                     Tag track_tag = track.getTag();
                     if (tag != null) {
                        String title = track_tag.getFirst(FieldKey.TITLE);
                        String artist = track_tag.getFirst(FieldKey.ARTIST);
                        // Length
                        int trackLength = track.getAudioHeader().getTrackLength();
                        playtime = getPlaytime(playtime, TrackList, key,
                          title, artist, trackLength);
                     }
                     key++;
                     Log.write("Info About File Collected: " + audio_file.getName(),
                       "Collector");
                  } catch (NullPointerException e) {
                     Log.write("Exception: " + e, "Collector");
                  }
               }
            }
            // set size
            long fileSizeInKB = size / 1024;
            long fileSizeInMB = fileSizeInKB / 1024;
            Size = fileSizeInMB + " MB";
            // set playtime
            int hours = playtime / 3600;
            int minutes = (playtime % 3600) / 60;
            int seconds = playtime % 60;
            Playtime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            if (playtime == 0) {
               Playtime = "Unknown";
            }
         }
         // set Full Info for WPPost
         info = new InfoForPost(releaseName, link, artLink, postCategory, Artist, Album,
           Genre, Released, Tracks, Playtime, Group,
           Format, Bitrate, Sample_Rate, Size, TrackList);
      } catch (CannotReadException | IOException | TagException | ReadOnlyFileException
        | InvalidAudioFrameException e) {
         Log.write("EXCEPTION IN COLLECTOR " + e, "Collector");
         Log.write(e, "Collector");
      }
      return info;
   }
   
   @SneakyThrows
   static int getTrackLength(String path) {
      ProcessBuilder builder = new ProcessBuilder("Files/MediaInfo_CLI_19.09_Windows_x64/MediaInfo.exe", path);
      builder.redirectErrorStream(true);
      Process process = builder.start();
      
      StringBuilder buffer = new StringBuilder();
      try (Reader reader = new InputStreamReader(process.getInputStream())) {
         for (int i; (i = reader.read()) != -1; ) {
            buffer.append((char) i);
         }
      }
      
      int status = process.waitFor();
      String collectedMediaInfo = "";
      if (status == 0) {
         collectedMediaInfo = buffer.toString();
      }
      int dIn = collectedMediaInfo.indexOf("Duration");
      String duration = collectedMediaInfo.substring(dIn, collectedMediaInfo.indexOf("\n", dIn))
        .replace("Duration                                 : ", "")
        .replace(" min ", ":")
        .replace(" s", "")
        .trim();
      int min = Integer.parseInt(duration.split(":")[0]);
      int sec = Integer.parseInt(duration.split(":")[1]);
      return min * 60 + sec;
   }
   
   default int getPlaytime(int playtime, HashMap<Integer, TrackInfo> trackList,
                           int key, String title, String artist, int trackLength) {
      playtime += trackLength;
      int minutes = (trackLength % 3600) / 60;
      int seconds = trackLength % 60;
      String track_Length = String.format("%02d:%02d", minutes, seconds);
      TrackInfo ThisTrack = new TrackInfo(title, artist, track_Length);
      trackList.put(key, ThisTrack);
      return playtime;
   }
   
   default InfoAboutRelease convertInfo(InfoForPost info) {
      InfoAboutRelease infoAboutRelease = new InfoAboutRelease();
      infoAboutRelease.setLinkToArt(info.getArtLink());
      infoAboutRelease.setArtist(info.getArtist());
      infoAboutRelease.setAlbum(info.getAlbum());
      infoAboutRelease.setGenre(info.getGenre());
      infoAboutRelease.setReleased(info.getReleased());
      infoAboutRelease.setNumberOfTracks(info.getTracks());
      infoAboutRelease.setPlaytime(info.getPlaytime());
      infoAboutRelease.setGroup(info.getGroup());
      infoAboutRelease.setFormat(info.getFormat());
      infoAboutRelease.setBitrate(info.getBitrate());
      infoAboutRelease.setSampleRate(info.getSample_Rate());
      infoAboutRelease.setSize(info.getSize());
      HashMap<Integer, TrackInfo> trackList = info.getTrackList();
      List<InfoAboutRelease.Track> listOfTracks = new ArrayList<>();
      for (TrackInfo value : trackList.values()) {
         InfoAboutRelease.Track track = new InfoAboutRelease.Track(value.getTitle(), value.getArtist(), value.getTime());
         listOfTracks.add(track);
      }
      infoAboutRelease.setTrackList(listOfTracks);
      return infoAboutRelease;
      
   }
   
}
