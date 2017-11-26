package pacman.impls.game_judge_impls;

import java.util.HashMap;
import java.util.Map;
import pacman.GameContainer;
import pacman.actors.GameJudge;
import pacman.actors.dynamic_tile_actors.Ghost;
import pacman.utils.Timer;

/**
 *
 * @author Texhnolyze
 */
public class ClassicGameJudge extends GameJudge {

    public ClassicGameJudge(GameContainer game) {
        super(game);
        allTimers.add(scatterChaseTimer);
    }
    
    @Override
    public boolean timeToLeavePen(Ghost g) {
        if (g.type == Ghost.BLINKY) return true;
        if (dotEated.passed(4000)) {
            int idx = getPreferredGhostIdx();
            if (g == game.ghosts[idx]) {
                dotEated.reset();
                return true;
            }
        }
        if (usingGlobalDotCounter) {
            return (g.type == Ghost.PINKY && globalDotCounter == 7) || 
                   (g.type == Ghost.INKY  && globalDotCounter == 17);
        }
        else {
            Integer i = personalDotCounter.get(g);
            if (i == null) {
                i = 0;
                personalDotCounter.put(g, i);
            }
            return i >= PERSONAL_DOT_COUNTER_LIMIT[idx3][g.getSerialNumber() - 2];
        }
    }
    
//  Scatter / Chase duration in milliseconds.
//  Even index - for scatter, odd - for chase
    private static final long[][] SCATTER_CHASE_TABLE = {
        {7000, 20000, 7000, 20000, 5000,  20000,  5000,  Long.MAX_VALUE},
        {7000, 20000, 7000, 20000, 5000, 1033000,  17,   Long.MAX_VALUE},
        {5000, 20000, 5000, 20000, 5000, 1037000,  17,   Long.MAX_VALUE}
    };

    private boolean chasing = false;
    private final Timer scatterChaseTimer = new Timer();
    
    @Override
    public boolean timeToScatter(Ghost g) {
        return !chasing;
    }

    @Override
    public boolean timeToChase(Ghost g) {
        return chasing;
    }

    //the function of power up time in ms on each level is:
    //f(lvl) = max(0, 8000 * (1 / ((1 - 1 / 21 ^ 2) * lvl ^ 2) - 1 / (21 ^ 2 - 1));
    //f(1) = 8000, f(21) = 0;
    @Override
    public int getNextPowerUpTimeMs() {
        int lvl = game.lvl;
        double d = 8000D * (1D / ((1D - 1D / (21D * 21D)) * lvl * lvl) - 1D / (21D * 21D - 1D));
        return Math.max(0, (int) d); 
    }
    
    private int idx1;
    private int idx2;
    private int idx3;
    
    @Override
    public void update() {
        super.update(); 
        if (scatterChaseTimer.passed(SCATTER_CHASE_TABLE[idx1][idx2])) {
            idx2++;
            scatterChaseTimer.reset();
            chasing = !chasing;
        }
    }
    
    private int getPreferredGhostIdx() {
        int preferredIdx = -1;
        for (int i = 0; i < game.ghosts.length; i++) {
            Ghost g = game.ghosts[i];
            if (g.type == Ghost.BLINKY) continue;
            if ((g.getState() & Ghost.IN_PEN) != 0) {
                if (g.type == Ghost.PINKY) return i;
                else {
                    if (preferredIdx == -1) preferredIdx = i;
                    else {
                        Ghost p = game.ghosts[preferredIdx];
                        if (g.type == Ghost.INKY && p.type == Ghost.CLYDE)
                            return i;
                    }
                }
            }
        }
        return preferredIdx;
    }
    
    private int globalDotCounter;
    private boolean usingGlobalDotCounter;
    private final Map<Ghost, Integer> personalDotCounter = new HashMap<>();
    
    private static final int[][] PERSONAL_DOT_COUNTER_LIMIT = {
        { 0, 30, 60 },
        { 0, 0,  50 },
        { 0, 0,  0 }
    };
    
    @Override
    public void notify(int event) {
        super.notify(event);
        if (event == STAGE_STARTS) {
            chasing = false;
            usingGlobalDotCounter = false;
            personalDotCounter.clear();
            int lvl = game.lvl;
            idx1 = lvl == 1 ? 0 : lvl <= 4 ? 1 : 2;
            idx2 = lvl == 1 ? 0 : lvl == 2 ? 1 : 2;
        } else if (event == PACMAN_ATE_THE_DOT) {
            if (!usingGlobalDotCounter) {
                int idx = getPreferredGhostIdx();
                if (idx != -1) {
                    Ghost g = game.ghosts[idx];
                    int n = personalDotCounter.getOrDefault(g, 0) + 1;
                    personalDotCounter.put(g, n);
                }
            } else {
                globalDotCounter++;
                if (globalDotCounter == 32) {
                    for (Ghost g : game.ghosts) {
                        if (g.type == Ghost.CLYDE 
                                && (g.getState() & Ghost.IN_PEN) != 0) {
                            usingGlobalDotCounter = false;
                            break;
                        }
                    }
                }
            }
        } else if (event == PACMAN_ATE_THE_ENERGIZER) {
            scatterChaseTimer.pause();
        } else if (event == POWER_UP_END) {
            scatterChaseTimer.resume();
        } else if (event == PACMAN_DIED) {
            globalDotCounter = 0;
            usingGlobalDotCounter = true;
        }
    }

    @Override
    public boolean isStagePassed() {
        return game.field.dotsCount == game.field.getDotsEated() 
                && game.field.energizersCount == game.field.getEnergizersEated();
    }
    
}
