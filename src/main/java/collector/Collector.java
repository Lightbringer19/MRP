package collector;

import com.google.gson.Gson;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.xuggle.xuggler.IContainer;
import json.InfoForPost;
import json.InfoFromBoxCom;
import json.TrackInfo;
import json.db.InfoAboutRelease;
import json.db.Release;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
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
import utils.FUtils;
import utils.Log;
import wordpress.Poster;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static collector.API_Image_Uploader.upload;
import static collector.Compressor.compress;
import static com.mongodb.client.model.Filters.eq;
import static com.xuggle.xuggler.IContainer.Type.READ;
import static wordpress.Poster.MONGO_CONTROL;

public class Collector {
   // public static void main(String[] args) {
   //     String pathname = "Z:\\8th Wonder Pool Videos 1907";
   //     File file = new File(pathname);
   //     File[] files = file.listFiles();
   //     InfoForPost collect = collect(files, "ReleaseName", "link", "artlink", new File(""), "RECORDPOOL VIDEOS");
   //     System.out.println(collect.toString());
   // }
   
   public static InfoForPost collect(File infoJsonFile) {
      
      InfoExtractor infoExtractor = new InfoExtractor(infoJsonFile).extract();
      InfoFromBoxCom infoFromBoxCom = infoExtractor.getInfoFromBoxCom();
      File folderToCollect = infoExtractor.getFolderToCollect();
      String postCategory = infoExtractor.getPostCategoryTEMP();
      // get art
      File[] art = folderToCollect.listFiles((dir, name) ->
        name.toLowerCase().endsWith(".jpg"));
      // upload art
      String artLink = "https://myrecordpool.com/wp-content/images/logo.jpg";
      if (postCategory.contains("RECORDPOOL")) {
         @SuppressWarnings("unchecked")
         Map<String, String> imageMap = (Map<String, String>) MONGO_CONTROL
           .staticImageLinksCollection.find().first().get("imageMap");
         for (Map.Entry<String, String> entry : imageMap.entrySet()) {
            if (infoFromBoxCom.getReleaseName().toLowerCase()
              .contains(entry.getKey().toLowerCase())) {
               artLink = entry.getValue();
            }
         }
      } else if (art.length != 0) {
         File theArt = art[0];
         // compress art
         Log.write("Compressing Art", "Poster");
         File compressedImage = compress(theArt);
         if (compressedImage == null) {
            Log.write("Corrupted Art deleted", "Poster");
            theArt.delete();
         } else {
            Log.write("Uploading Art", "Poster");
            // upload compressed art
            artLink = upload(compressedImage);
            compressedImage.delete();
         }
      }
      // get Audio Files
      File[] audioFiles = folderToCollect.listFiles((dir, name) -> {
         switch (postCategory) {
            case "SCENE-MP3":
            case "BEATPORT":
            case "RECORDPOOL MUSIC":
               return name.toLowerCase().endsWith(".mp3");
            case "SCENE-FLAC":
               return name.toLowerCase().endsWith(".flac");
            case "RECORDPOOL VIDEOS":
               return name.toLowerCase().endsWith(".mp4");
         }
         return false;
      });
      // collect info from Audio Files
      Log.write("Collecting Info About Files", "Poster");
      InfoForPost info = Collector.collect(audioFiles, infoFromBoxCom.getReleaseName(),
        infoFromBoxCom.getDownloadLinkBoxCom(), artLink, folderToCollect, postCategory);
      // insert info to DB
      Document foundRelease = MONGO_CONTROL.releasesCollection
        .find(eq("releaseName", info.getReleaseName())).first();
      if (foundRelease == null) {
         InfoAboutRelease infoAboutRelease = convertInfo(info);
         Release release = new Release();
         release.setReleaseName(info.getReleaseName());
         release.setCategory(info.getPostCategory());
         release.setBoxComDownloadLink(info.getLink());
         release.setPathToLocalFolder(folderToCollect.getAbsolutePath());
         release.setInfoAboutRelease(infoAboutRelease);
         MONGO_CONTROL.releasesCollection.insertOne(release.toDoc());
      } else {
         InfoAboutRelease infoAboutRelease = convertInfo(info);
         foundRelease.put("category", info.getPostCategory());
         foundRelease.put("boxComDownloadLink", info.getLink());
         foundRelease.put("infoAboutRelease", infoAboutRelease.toDoc());
         MONGO_CONTROL.releasesCollection.replaceOne((eq("_id",
           foundRelease.getObjectId("_id"))), foundRelease);
      }
      return info;
   }
   
   private static InfoAboutRelease convertInfo(InfoForPost info) {
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
   
   private static InfoForPost collect(File[] audioFiles, String releaseName, String link,
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
            if (Group.toLowerCase().contains("srp")
              || Group.toLowerCase().contains("gfvid")) {
               File[] nfo = folderToCollect.listFiles((dir, name) ->
                 name.endsWith(".nfo"));
               if (nfo != null && nfo[0] != null) {
                  if (Group.toLowerCase().contains("srp")) {
                     String nfoSt = FileUtils.readFileToString(nfo[0], "UTF-8")
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
                  } else {
                     String nfoSt = FileUtils.readFileToString(nfo[0], "UTF-8")
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
                  }
               }
            }
         } else {
            int playtime = 0;
            long size = 0;
            // video
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
                    "Poster");
                  size += video_file.length();
                  Map<Integer, MetaValue> itunesMeta = null;
                  try {
                     MetadataEditor mediaMeta = MetadataEditor.createFrom(video_file);
                     itunesMeta = mediaMeta.getItunesMeta();
                  } catch (AssertionError | IllegalArgumentException | NullPointerException e1) {
                     Log.write("Exception in Collector: " + e1, "Poster");
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
                  int trackLength;
                  try {
                     IContainer container = IContainer.make();
                     int result = container.open(video_file.getAbsolutePath(),
                       READ, null);
                     trackLength = (int) container.getDuration() / 1000000;
                     playtime = getPlaytime(playtime, TrackList,
                       key, title, artist, trackLength);
                  } catch (Exception e) {
                     Log.write(e.toString(), "Poster");
                  }
                  key++;
                  Log.write("Info About File Collected: " + video_file.getName(),
                    "Poster");
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
                  Log.write("Collecting Info About File: " + audio_file.getName(),
                    "Poster");
                  size += audio_file.length();
                  AudioFile track = null;
                  try {
                     track = AudioFileIO.read(audio_file);
                  } catch (CannotReadException e) {
                     Log.write("Exception: " + e, "Poster");
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
                       "Poster");
                  } catch (NullPointerException e) {
                     Log.write("Exception: " + e, "Poster");
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
         Log.write("EXCEPTION IN COLLECTOR " + e, "Poster");
         e.printStackTrace();
      }
      return info;
   }
   
   private static int getPlaytime(int playtime, HashMap<Integer, TrackInfo> trackList,
                                  int key, String title, String artist, int trackLength) {
      playtime += trackLength;
      int minutes = (trackLength % 3600) / 60;
      int seconds = trackLength % 60;
      String track_Length = String.format("%02d:%02d", minutes, seconds);
      TrackInfo ThisTrack = new TrackInfo(title, artist, track_Length);
      trackList.put(key, ThisTrack);
      return playtime;
   }
   
   private static class InfoExtractor {
      private File infoJsonFile;
      private InfoFromBoxCom infoFromBoxCom;
      private File folderToCollect;
      private String postCategoryTEMP;
      
      InfoExtractor(File infoJsonFile) {
         this.infoJsonFile = infoJsonFile;
      }
      
      InfoFromBoxCom getInfoFromBoxCom() {
         return infoFromBoxCom;
      }
      
      File getFolderToCollect() {
         return folderToCollect;
      }
      
      String getPostCategoryTEMP() {
         return postCategoryTEMP;
      }
      
      InfoExtractor extract() {
         String readFile = FUtils.readFile(infoJsonFile);
         Gson gson = new Gson();
         infoFromBoxCom = gson.fromJson(readFile, InfoFromBoxCom.class);
         folderToCollect = new File(infoFromBoxCom.getLocalPathToFolder());
         postCategoryTEMP = Poster.getCategory(folderToCollect);
         return this;
      }
   }
   
   private static String getFromNfo(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf(":", nfo.indexOf(extract)) + 1,
        nfo.indexOf(extractEnd)).trim();
   }
   
   private static String getFromNfoSpace(String nfo, String extract, String extractEnd) {
      return nfo.substring(nfo.indexOf("\n", nfo.indexOf(extract)),
        nfo.indexOf(extractEnd)).trim();
   }
}