package tag_editor;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;
import utils.Constants;
import utils.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Map;

class TagEditor {
   
   private static final String WWW_MY_RECORD_POOL_COM = "www.MyRecordPool.com";
   private static final String WWW_GROOVYTUNES_ORG = "-www.groovytunes.org";
   
   private static String[] titleFilter = {
     "-www.groovytunes.org",
     "www.electronicfresh.com",
     "www.inevil.com",
     "inevil.com",
     " – SharingDB.eu",
     ", SharingDB.eu",
     "SharingDB"
   };
   
   static void editMP3(File file) {
      try {
         Log.write("Editing Tags: " + file.getName(), "Tagger");
         AudioFile audioFile = AudioFileIO.read(file);
         Tag tag = audioFile.getTag();
         if (tag != null) {
            tag.deleteField(FieldKey.TRACK);
            tag.deleteField(FieldKey.YEAR);
            tag.deleteField(FieldKey.COMPOSER);
            tag.deleteField(FieldKey.URL_OFFICIAL_ARTIST_SITE);
            tag.deleteField(FieldKey.COMMENT);
            tag.deleteField(FieldKey.DISC_NO);
            tag.setField(FieldKey.ALBUM, WWW_MY_RECORD_POOL_COM);
            tag.setField(FieldKey.ALBUM_ARTIST, WWW_MY_RECORD_POOL_COM);
            // title filtering
            String title = tag.getFirst(FieldKey.TITLE);
            title = filter(title, titleFilter);
            tag.setField(FieldKey.TITLE, title);
            // get artist and title from title
            String artist = tag.getFirst(FieldKey.ARTIST);
            if (artist.equals("For Promotional Use Only") || artist.equals("")) {
               String[] titleSplit = title.split(" - ");
               tag.setField(FieldKey.ARTIST, titleSplit[0]);
               tag.setField(FieldKey.TITLE, titleSplit[1]);
            }
            //artist filtering
            artist = filter(artist, titleFilter);
            tag.setField(FieldKey.ARTIST, artist);
            // genre filtering
            String genreDescription = tag.getFirst(FieldKey.GENRE);
            String[] falseGenres = {
              "sharing", "www.original-mass.net", "hotreleases", "electronicfresh"};
            for (String falseGenre : falseGenres) {
               boolean contains = genreDescription.toLowerCase().contains(falseGenre);
               if (contains) {
                  tag.deleteField(FieldKey.GENRE);
               }
            }
            // add artwork
            Artwork art = tag.getFirstArtwork();
            // if art present
            String category = file.getParentFile().getParentFile().getName();
            if (art != null && !category.equals("RECORDPOOL")) {
               // compare artwork with promoImage
               ImageExtractor imageExtractor = new ImageExtractor().invoke();
               byte[] artData = art.getBinaryData();
               if (Arrays.equals(imageExtractor.getMyrec(), artData) ||
                 Arrays.equals(imageExtractor.getElectroFresh(), artData) ||
                 Arrays.equals(imageExtractor.getHeyDj(), artData) ||
                 Arrays.equals(imageExtractor.getHeyDj2(), artData)) {
                  // if promotion image is present
                  Artwork logoArt = ArtworkFactory
                    .createArtworkFromFile(
                      new File(Constants.filesDir + "logo.jpg"));
                  tag.deleteArtworkField();
                  tag.addField(logoArt);
               }
            }
            // when no art
            else {
               Artwork logoArt = ArtworkFactory.createArtworkFromFile(
                 new File(Constants.filesDir + "logo.jpg"));
               tag.deleteArtworkField();
               tag.addField(logoArt);
            }
            audioFile.commit();
         }
         // rename file
         String fileName = file.getName();
         if (fileName.contains(WWW_GROOVYTUNES_ORG)) {
            String ReplacedName = fileName
              .replace(WWW_GROOVYTUNES_ORG, "");
            File dest = new File(file.getParentFile().getAbsolutePath() +
              "//" + ReplacedName);
            file.renameTo(dest);
         }
         Log.write("Tag Edited: " + file.getName(), "Tagger");
      } catch (Exception e) {
         try {
            Log.write(e, "Tagger");
            Log.write("Exception editMP3 " + e, "Tagger");
            Thread.sleep(1000);
         } catch (InterruptedException e1) {
            e1.printStackTrace();
         }
      }
   }
   
   static void EditMP4(File file) {
      try {
         Log.write("Editing MP4: " + file.getName(), "Tagger");
         MetadataEditor mediaMeta = MetadataEditor.createFrom(file);
         Map<Integer, MetaValue> itunesMeta = mediaMeta.getItunesMeta();
         // Edit Tags
         itunesMeta.put(-1452841618, MetaValue.createString(" ")); // genre
         itunesMeta.put(-1453039239, MetaValue.createString(" "));
         itunesMeta.put(757935405, MetaValue.createString(" "));
         itunesMeta.put(-1453101708, MetaValue.createString(" "));
         // Album
         itunesMeta.put(-1453233054, MetaValue.createString(WWW_MY_RECORD_POOL_COM));
         // Album Artist
         itunesMeta.put(1631670868, MetaValue.createString(WWW_MY_RECORD_POOL_COM));
         // Add Logo
         String logoPath = Constants.filesDir + "logo.jpg";
         InputStream logo = new FileInputStream(logoPath);
         byte[] mrpImage = IOUtils.toByteArray(logo);
         itunesMeta.put(1668249202, MetaValue.createOther(MetaValue.TYPE_JPEG, mrpImage));
         logo.close();
         // save
         mediaMeta.save(true);
         Log.write("MP4 Edited: " + file.getName(), "Tagger");
      } catch (Exception | AssertionError e) {
         Log.write("Exception editMP4 " + e, "Tagger");
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e1) {
            e1.printStackTrace();
         }
      }
   }
   
   static void EditMP3TagsInFolder(File folder) {
      File[] Mp3Folder = folder.listFiles();
      // extract artwork from file
      for (File file : Mp3Folder) {
         // Edit every mp3 file
         if (file.getName().toLowerCase().endsWith(".mp3")) {
            editMP3(file);
         }
      }
      getArtwork(Mp3Folder);
   }
   
   private static void getArtwork(File[] folder) {
      try {
         for (File file : folder) {
            if (file.getName().toLowerCase().endsWith(".mp3")) {
               AudioFile audioFile = AudioFileIO.read(file);
               Tag tag = audioFile.getTag();
               if (tag != null) {
                  Artwork art = tag.getFirstArtwork();
                  if (art != null) {
                     InputStream logo =
                       new FileInputStream(Constants.filesDir + "logo.jpg");
                     byte[] logoData = IOUtils.toByteArray(logo);
                     byte[] artData = art.getBinaryData();
                     if (!Arrays.equals(logoData, artData)) {
                        OutputStream os = new FileOutputStream(changeFileName(file));
                        os.write(artData);
                        os.close();
                        break;
                     }
                  }
               }
            }
         }
      } catch (IOException | CannotReadException | TagException | ReadOnlyFileException
        | InvalidAudioFrameException e) {
         Log.write("Exception getArtwork " + e, "Tagger");
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e1) {
            e1.printStackTrace();
         }
      }
   }
   
   private static File changeFileName(File file) {
      String pathToArt = file.getAbsolutePath().replace(".mp3", ".jpg");
      if (file.getName().contains(WWW_GROOVYTUNES_ORG)) {
         pathToArt = pathToArt.replace(WWW_GROOVYTUNES_ORG, "");
      }
      return new File(pathToArt);
   }
   
   static String filter(String stringToFilter, String[] filterValues) {
      for (String filterValue : filterValues) {
         stringToFilter = stringToFilter.replace(filterValue, "");
      }
      return stringToFilter;
   }
   
   @Data
   private static class ImageExtractor {
      private byte[] myrec;
      private byte[] electroFresh;
      private byte[] heyDj;
      private byte[] heyDj2;
      
      ImageExtractor invoke() throws IOException {
         InputStream myrecImage =
           new FileInputStream(Constants.filesDir + "pickmyrec.jpg");
         myrec = IOUtils.toByteArray(myrecImage);
         myrecImage.close();
         InputStream electroFreshImage =
           new FileInputStream(Constants.filesDir + "electronicfresh.jpg");
         electroFresh = IOUtils.toByteArray(electroFreshImage);
         electroFreshImage.close();
         InputStream heyDjImage =
           new FileInputStream(Constants.filesDir + "heydj.jpg");
         heyDj = IOUtils.toByteArray(heyDjImage);
         InputStream heyDj2Image =
           new FileInputStream(Constants.filesDir + "heydj2.jpg");
         heyDj2 = IOUtils.toByteArray(heyDj2Image);
         heyDj2Image.close();
         return this;
      }
   }
}
