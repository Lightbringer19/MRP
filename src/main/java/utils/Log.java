package utils;

import java.io.*;

public class Log {
   
   public static void write(String logText, String logName) {
      try {
         System.out.println(logText);
      } catch (Exception e) {
         Log.write(e, logName + " EXCEPTION IN LOGGER");
      }
      logToFile(logText, logName);
   }
   
   public static void write(Exception e, String logName) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      String logText = errors.toString();
      try {
         System.out.println(logText);
      } catch (Exception ex) {
         Log.write(ex, logName + " EXCEPTION IN LOGGER EXCEPTIONS");
      }
      logToFile(logText, logName);
   }
   
   private static void logToFile(String logText, String logName) {
      try {
         new File(Constants.filesDir + "LOG//").mkdirs();
         BufferedWriter writer = new BufferedWriter(
           new FileWriter(Constants.filesDir + "LOG//" + logName + ".txt", true));
         writer.newLine(); // Add new line
         writer.write(CheckDate.getTimeForLog() + logText);
         writer.close();
      } catch (IOException e) {
         System.out.println("Log.Write: " + e);
      }
   }
}