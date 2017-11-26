package pacman.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 *
 * @author Texhnolyze
 */
public class Animation {

    private int frameIdx;
    private final Image[] frames;
    
    private final TickTimer timer;
    
    private int freq;    
    
    private boolean drawOnce;
    private boolean stopAnimation;
    private boolean visible = true;
    
    public Animation(int freq, Image...frames) {
        validate(frames);
        this.freq = freq;
        this.frames = frames;
        timer = new TickTimer();
    }
    
    public void draw(GraphicsContext gc) {
        if (visible) {
            if (!drawOnce || (drawOnce && frameIdx != frames.length - 1)) {
                gc.drawImage(frames[frameIdx], 0, 0);
                if (!stopAnimation) {
                    timer.tick();
                    if (timer.passed(freq)) { 
                        frameIdx = (frameIdx + 1) % frames.length;
                        timer.reset();
                    }
                }
            }
        }
    }
    
    public float getWidth() {
        return (float) frames[frameIdx].getWidth();
    }
    
    public float getHeight() {
        return (float) frames[frameIdx].getHeight();
    }
    
    public void setVisibility(boolean visible) {
        this.visible = visible;
    }
    
    public void setDrawOnce(boolean drawOnce) {
        this.drawOnce = drawOnce;
    }
    
    public void setAnimationStop(boolean stopAnimation) {
        this.stopAnimation = stopAnimation;
    } 

    public void reset(int freq) {
        this.freq = freq;
        frameIdx = 0;
        visible = true;
        drawOnce = false;
        stopAnimation = false;
    }
    
    public void reset() {
        reset(freq);
    }
    
    private void validate(Image...frames) {
        if (frames.length < 2) 
            throw new IllegalArgumentException("The number of frames must be at least 2.");
    }
    
}

