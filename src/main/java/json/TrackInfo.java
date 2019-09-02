package json;

public class TrackInfo {
   private String title;
   private String artist;
   private String time;
   
   public TrackInfo(String title, String artist, int trackLength) {
   
   }
   
   public TrackInfo(String title, String artist, String time) {
      super();
      this.title = title;
      this.artist = artist;
      this.time = time;
   }
   
   public String getArtist() {
      return artist;
   }
   
   public void setArtist(String artist) {
      this.artist = artist;
   }
   
   public String getTime() {
      return time;
   }
   
   public void setTime(String time) {
      this.time = time;
   }
   
   public String getTitle() {
      return title;
   }
   
   public void setTitle(String title) {
      this.title = title;
   }
   
   @Override
   public String toString() {
      return "TrackInfo [title=" + title + ", artist=" + artist + ", time=" + time + "]";
   }
   
}