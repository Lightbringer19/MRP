package monitors;

import reposter.Reposter;

public class Reposter_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("Reposter");
      doAll(new Reposter());
   }
}
