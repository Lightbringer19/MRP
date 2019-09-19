package scraper.abstraction;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class test {
   static Timer timer = new Timer();
   
   public static void main(String[] args) {
      
      // long sec = 1000;
      // long min = sec * 60;
      // long hour = 60 * min;
      
      timer.schedule(new TheTask(), 0);
      
   }
   
   static class TheTask extends TimerTask {
      @Override
      public void run() {
         System.out.println("TEST");
         timer.schedule(new TheTask(), ThreadLocalRandom.current().nextInt(1, 5) * 1000);
      }
   }
}
