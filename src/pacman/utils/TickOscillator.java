package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class TickOscillator {

    private int freq;
    private int ticksPassed;
    
    private boolean increment = true;
    
    public TickOscillator(int freq) {
        reset(freq);
    }
    
    public boolean getState() {
        return increment;
    }
    
    public int getTicksPassed() {
        return ticksPassed;
    }
    
    public void tick() {
        if (increment) {
            if (++ticksPassed == freq) increment = false;
        } else { 
            if (--ticksPassed == 0) increment = true;
        }
    }
    
    public void reset(int freq) {
        this.freq = freq;
        ticksPassed = 0;
        increment = true;
    }
    
    public void reset() {
        reset(freq);
    }
    
}
