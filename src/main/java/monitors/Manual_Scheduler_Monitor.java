package monitors;

import utils.manual.Manual_Scheduler;

public class Manual_Scheduler_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("Manual Scheduler Monitor");
      Thread thread = new Thread(() -> Manual_Scheduler.main(null));
      doAll(thread);
   }
}
