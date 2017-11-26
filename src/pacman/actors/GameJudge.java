package pacman.actors;

import java.util.LinkedList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import pacman.GameContainer;
import pacman.GameObserver;
import pacman.actors.dynamic_tile_actors.Ghost;
import pacman.utils.Timer;

/**
 *
 * @author Texhnolyze
 */
public abstract class GameJudge implements Actor, GameObserver {

    protected final GameContainer game;
    
    protected final Timer stageStarted    = new Timer();
    protected final Timer dotEated        = new Timer();
    protected final Timer energizerEated  = new Timer();
    
    protected final List<Timer> allTimers = new LinkedList<>();
    
    {
        allTimers.add(stageStarted);
        allTimers.add(dotEated);
        allTimers.add(energizerEated);
    }
    
    public GameJudge(GameContainer game) {
        this.game = game;
    }
    
    public void reset() {
        for (Timer t : allTimers) t.reset();
    }
    
    @Override
    public void update() {
        //empty.
    }

    @Override
    public final void draw(GraphicsContext gc) {
        //nothing to do here.
    }

    private int powerUpTimeMs;
    
    @Override
    public void notify(int event) {
        if (event == PACMAN_ATE_THE_DOT) {
            dotEated.reset();
        } else if (event == PACMAN_ATE_THE_ENERGIZER) {
            energizerEated.reset();
            powerUpTimeMs = getNextPowerUpTimeMs();
        } else if (event == STAGE_STARTS) {
            for (Timer t : allTimers) t.reset();
        }
    }
    
    public abstract boolean isStagePassed();
    
    public abstract int getNextPowerUpTimeMs();
    
    public boolean powerUpEnd() {
        return energizerEated.passed(powerUpTimeMs);
    }
    
    public double getPassedPowerUpTime() {
        return (double) energizerEated.getMsPassed() / powerUpTimeMs;
    }
    
    public abstract boolean timeToLeavePen(Ghost g);

    public abstract boolean timeToScatter(Ghost g);
    
    public abstract boolean timeToChase(Ghost g);
    
    public void pauseTimers() {
        for (Timer ts : allTimers) ts.pause();
    }
    
    public void resumeTimers() {
        for (Timer ts : allTimers) ts.resume();
    }
    
}
