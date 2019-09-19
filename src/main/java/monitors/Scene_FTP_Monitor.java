package monitors;

import ftp.scene.SceneFTPManager;

public class Scene_FTP_Monitor extends Monitor {
   public static void main(String[] args) {
      setTITLE("SCENE_FTP");
      doAll(new SceneFTPManager());
   }
}
