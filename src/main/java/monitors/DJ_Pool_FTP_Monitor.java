package monitors;

import ftp.djpool.DjPoolFtp;

public class DJ_Pool_FTP_Monitor extends Monitor {
    public static void main(String[] args) {
        setTITLE("DJ POOL FTP Monitor");
        doAll(new DjPoolFtp());
    }
}
