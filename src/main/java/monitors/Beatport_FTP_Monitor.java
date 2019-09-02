package monitors;

import ftp.beatport.BeatportFtp;

public class Beatport_FTP_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("BeatPort FTP Monitor");
      doAll(new BeatportFtp());
   }
}
