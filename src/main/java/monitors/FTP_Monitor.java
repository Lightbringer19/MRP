package monitors;

import ftp.RpAndBeatFTPManager;

public class FTP_Monitor extends Monitor {
    public static void main(String[] args) {
        setTITLE("FTP_Monitor");
        doAll(new RpAndBeatFTPManager());
    }
}
