package collector;

import json.TrackInfo;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class TEST implements NfoExtractionInterface {
   public static void main(String[] args) {
      
      TEST test = new TEST();
      
      File file = new File("GDS_SERVER/srp");
      test.extract(file);
   }
   
   @SneakyThrows
   void extract(File folderToCollect) {
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
            System.out.println(nfoSt);
            Tracks = "1";
            Artist = getFromNfo(nfoSt, "Artist", "Track Title");
            String title = getFromNfo(nfoSt, "Title", "Genre");
            Genre = getFromNfo(nfoSt, "Genre", "Year");
            Released = getFromNfo(nfoSt, "Year.", "Rip date");
            System.out.println(Released);
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
   }
}
