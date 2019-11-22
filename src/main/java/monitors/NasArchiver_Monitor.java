package monitors;

import archiver.NasArchiver;

public class NasArchiver_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("NAS ARCHIVER Monitor");
      doAll(new NasArchiver());
   }
}
