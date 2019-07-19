package monitors;

import ftp.FtpAndScheduleController;

public class FTP_Monitor extends Monitor {
    public static void main(String[] args) {
        setTITLE("FTP_Monitor");
        doAll(new FtpAndScheduleController());
    }
}
