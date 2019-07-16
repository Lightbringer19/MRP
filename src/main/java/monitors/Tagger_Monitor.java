package monitors;

import tag_editor.TagRunnerMain;

public class Tagger_Monitor extends Monitor {
    public static void main(String[] args) {
        setTITLE("Tagger");
        doAll(new TagRunnerMain());
    }
}
