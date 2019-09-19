package monitors;

import ftp.djpool.DJCFtp;

public class DJC_FTP_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("DJ POOL FTP Monitor");
      doAll(new DJCFtp());
   }
}
