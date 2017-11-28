package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class TickTimer {

    private int ticksPassed;
    
    public void tick() {
        ticksPassed++;
    }
    
    public boolean passed(int ticks) {
        return ticksPassed >= ticks;
    }
    
    public void reset() {
        ticksPassed = 0;
    }
    
}

