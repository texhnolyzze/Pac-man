package pacman.utils;

import java.io.File;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;

/**
 *
 * @author Texhnolyze
 */
public class Sound {
    
    static {
        TinySound.init();
    }
    
    private final Music m;
    
    public Sound(String fileName) {
        this(fileName, 1.0D);
    }
    
    public Sound(String fileName, double vol) {
        m = TinySound.loadMusic(new File(fileName).getAbsoluteFile());
        m.setVolume(vol);
    }
    
    public boolean isPlaying() {
        return m.playing();
    }
    
    public void play(boolean loop) {
        m.rewind();
        m.play(loop);
    }
    
    public void playIfNot(boolean loop) {
        if (!m.playing()) {
            play(loop);
        }
    }
    
    public void stop() {
        m.stop();
        m.rewind();
    }
    
    public void fadeOut() {
        m.setLoop(false);
    }

}
