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
    
    private final TickTimer timer = new TickTimer();
    
    private int freq;    
    
    private boolean drawOnce;
    private boolean stopAnimation;
    
    public Animation(int freq, Image...frames) {
        this.freq = freq;
        this.frames = frames;
    }
    
    public void draw(GraphicsContext gc) {
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
    
    public float getWidth() {
        return (float) frames[frameIdx].getWidth();
    }
    
    public float getHeight() {
        return (float) frames[frameIdx].getHeight();
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
        drawOnce = false;
        stopAnimation = false;
    }
    
    public void reset() {
        reset(freq);
    }
    
}

