package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class Timer {

    private long stamp = System.currentTimeMillis();
    
    public void reset() {
        stamp = System.currentTimeMillis();
        if (paused) dt = 0;
    }
    
    public long getMsPassed() {
        return paused ? dt : System.currentTimeMillis() - stamp;
    }
    
    public boolean passed(long ms) {
        return getMsPassed() >= ms;
    }
    
    private long dt;
    private boolean paused;
    
    public void pause() {
        paused = true;
        dt = System.currentTimeMillis() - stamp;
    }
    
    public void resume() {
        paused = false;
        stamp = System.currentTimeMillis() - dt;
    }
    
}
