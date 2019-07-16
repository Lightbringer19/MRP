package monitors;

import ftp.scene.SceneFtpController;

public class SCENE_FTP_Monitor extends Monitor {
    public static void main(String[] args) {
        setTITLE("SCENE_FTP");
        doAll(new SceneFtpController());
    }
}
