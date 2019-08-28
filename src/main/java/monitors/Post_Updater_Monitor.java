package monitors;

import wordpress.PostUpdater;

public class Post_Updater_Monitor extends Monitor {
    
    public static void main(String[] args) {
        setTITLE("Post Updater");
        doAll(new PostUpdater());
    }
}
