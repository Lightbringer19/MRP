package monitors;

import collector.Collector;

public class Collector_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("Collector Monitor");
      doAll(new Collector());
   }
}
