package monitors.disk_monitor;

import java.io.File;

public class Disk_Monitor {
   public static void main(String[] args) {
      Space space = new Space().invoke();
      int intPercent = space.getIntPercent();
      String humanReadableByteCount = space.getHumanReadableByteCount();
      System.out.println(intPercent);
      System.out.println(humanReadableByteCount);
   }
   
   public static String humanReadableByteCount(long bytes, boolean si) {
      int unit = si ? 1000 : 1024;
      if (bytes < unit) {
         return bytes + " B";
      }
      int exp = (int) (Math.log(bytes) / Math.log(unit));
      String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
      return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
   }
   
   static class Space {
      private int intPercent;
      private String humanReadableByteCount;
      
      public int getIntPercent() {
         return intPercent;
      }
      
      public String getHumanReadableByteCount() {
         return humanReadableByteCount;
      }
      
      public Space invoke() {
         long totalSpace = new File("C:/").getTotalSpace();
         long freeSpace = new File("C:/").getFreeSpace();
         
         double percent = (double) 100 * ((double) freeSpace / (double) totalSpace);
         intPercent = (int) (percent + 0.5);
         
         humanReadableByteCount = humanReadableByteCount(freeSpace, false);
         return this;
      }
   }
}
