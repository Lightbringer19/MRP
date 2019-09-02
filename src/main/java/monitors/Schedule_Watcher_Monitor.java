package monitors;

import scheduler.ScheduleWatcher;

public class Schedule_Watcher_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("Schedule Watcher");
      doAll(new ScheduleWatcher());
   }
}
