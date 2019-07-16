package monitors;

import boxcom.Uploader;

public class Uploader_Monitor extends Monitor {
    public static void main(String[] args) {
        setTITLE("Uploader");
        doAll(new Uploader());
    }
}
