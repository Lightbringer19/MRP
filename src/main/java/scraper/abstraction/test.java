package scraper.abstraction;

import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;

public class test {
   public static void main(String[] args) throws InterruptedException {
      long sec = 1000;
      long min = sec * 60;
      long hour = 60 * min;
      
      Timer timer = new Timer();
      TimerTask check = new TimerTask() {
         @Override
         @SneakyThrows
         public void run() {
            System.out.println("TEST");
         }
      };
      // timer.schedule(check, 0, ThreadLocalRandom.current().nextInt(1, 10) * sec);
      while (true) {
         check.run();
         sleep(ThreadLocalRandom.current().nextInt(1, 10) * sec);
      }
   }
}
