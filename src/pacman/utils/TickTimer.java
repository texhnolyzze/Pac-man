package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class TickTimer {

    private int ticksPassed;
    
    public TickTimer() {}
    
    public void tick() {
        ticksPassed++;
    }
    
    public int getTicksPassed() {
        return ticksPassed;
    }
    
    public void setTicksPassed(int ticksPassed) {
        this.ticksPassed = ticksPassed;
    }
    
    public boolean passed(int ticks) {
        return ticksPassed >= ticks;
    }
    
    public void reverse(int n) {
        ticksPassed = n - ticksPassed;
    }
    
    public void reset() {
        ticksPassed = 0;
    }
    
}

