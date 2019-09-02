package json;

import java.util.HashMap;

public class InfoForPost {
   
   private String releaseName;
   private String link;
   private String artLink;
   private String postCategory;
   
   private String Artist;
   private String Album;
   private String Genre;
   private String Released;
   private String Tracks;
   private String Playtime;
   private String Group;
   private String Format;
   private String Bitrate;
   private String Sample_Rate;
   private String Size;
   
   private HashMap<Integer, TrackInfo> TrackList;
   
   public InfoForPost() {
   }
   
   public InfoForPost(String releaseName, String link, String artLink, String postCategory, String artist,
                      String album, String genre, String released, String tracks, String playtime, String group, String format,
                      String bitrate, String sample_Rate, String size, HashMap<Integer, TrackInfo> trackList) {
      this.releaseName = releaseName;
      this.link = link;
      this.artLink = artLink;
      this.postCategory = postCategory;
      Artist = artist;
      Album = album;
      Genre = genre;
      Released = released;
      Tracks = tracks;
      Playtime = playtime;
      Group = group;
      Format = format;
      Bitrate = bitrate;
      Sample_Rate = sample_Rate;
      Size = size;
      TrackList = trackList;
   }
   
   public String getAlbum() {
      return Album;
   }
   
   public void setAlbum(String album) {
      Album = album;
   }
   
   public String getArtist() {
      return Artist;
   }
   
   public void setArtist(String artist) {
      Artist = artist;
   }
   
   public String getArtLink() {
      return artLink;
   }
   
   public void setArtLink(String artLink) {
      this.artLink = artLink;
   }
   
   public String getBitrate() {
      return Bitrate;
   }
   
   public void setBitrate(String bitrate) {
      Bitrate = bitrate;
   }
   
   public String getFormat() {
      return Format;
   }
   
   public void setFormat(String format) {
      Format = format;
   }
   
   public String getGenre() {
      return Genre;
   }
   
   public void setGenre(String genre) {
      Genre = genre;
   }
   
   public String getGroup() {
      return Group;
   }
   
   public void setGroup(String group) {
      Group = group;
   }
   
   public String getLink() {
      return link;
   }
   
   public void setLink(String link) {
      this.link = link;
   }
   
   public String getPlaytime() {
      return Playtime;
   }
   
   public void setPlaytime(String playtime) {
      Playtime = playtime;
   }
   
   public String getReleased() {
      return Released;
   }
   
   public void setReleased(String released) {
      Released = released;
   }
   
   public String getReleaseName() {
      return releaseName;
   }
   
   public void setReleaseName(String releaseName) {
      this.releaseName = releaseName;
   }
   
   public String getSample_Rate() {
      return Sample_Rate;
   }
   
   public void setSample_Rate(String sample_Rate) {
      Sample_Rate = sample_Rate;
   }
   
   public String getSize() {
      return Size;
   }
   
   public void setSize(String size) {
      Size = size;
   }
   
   public HashMap<Integer, TrackInfo> getTrackList() {
      return TrackList;
   }
   
   public void setTrackList(HashMap<Integer, TrackInfo> trackList) {
      TrackList = trackList;
   }
   
   public String getTracks() {
      return Tracks;
   }
   
   public void setTracks(String tracks) {
      Tracks = tracks;
   }
   
   public String getPostCategory() {
      return postCategory;
   }
   
   public void setPostCategory(String postCategory) {
      this.postCategory = postCategory;
   }
   
   @Override
   public String toString() {
      return "InfoForPost [releaseName=" + releaseName + ", link=" + link + ", artLink=" + artLink + ", postCategory="
        + postCategory + ", Artist=" + Artist + ", Album=" + Album + ", Genre=" + Genre + ", Released="
        + Released + ", Tracks=" + Tracks + ", Playtime=" + Playtime + ", Group=" + Group + ", Format=" + Format
        + ", Bitrate=" + Bitrate + ", Sample_Rate=" + Sample_Rate + ", Size=" + Size + ", TrackList="
        + TrackList + "]";
   }
   
}
