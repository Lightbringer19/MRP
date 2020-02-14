package scraper.late_night_record_pool;

import lombok.Data;

import java.util.List;

@Data
public class LNRPApiResponse {
   private int total;
   private List<LNRPRelease> releases;
   
   @Data
   public static class LNRPRelease {
      
      private int priority;
      private int id;
      private int remix;
      private String artist;
      private String title;
      private String added;
      private String time;
      private int year;
      private String label;
      private int throwback;
      private int certified;
      private String comments;
      private int score;
      private int ratings;
      private int max_bpm;
      private String updated;
      private String genre;
      private String bpm;
      private double rating;
      private List<Version> versions;
      
      @Data
      public static class Version {
         private int id;
         private String type;
         private String time;
      }
   }
}
