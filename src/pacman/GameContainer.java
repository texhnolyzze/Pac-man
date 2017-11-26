package pacman;

import java.util.LinkedList;
import java.util.List;
import pacman.actors.GameField;
import pacman.actors.GameJudge;
import pacman.actors.dynamic_tile_actors.Ghost;
import pacman.actors.dynamic_tile_actors.Pacman;

/**
 *
 * @author Texhnolyze
 */
public class GameContainer {
    
    public Player player;
    
    public GameField field;
    public GameJudge judge;
    
    public Pacman pacman;
    public Ghost[] ghosts;
    
    public List<GameObserver> observers = new LinkedList<>();
    
    public int lvl = 1;
    
    public boolean atLeastOneGhostIsFrightened() {
        for (Ghost g : ghosts) 
            if ((g.getState() & Ghost.FRIGHTENED) != 0)
                return true;
        return false;
    }
    
    public boolean atLeastOneGhostIsDead() {
        for (Ghost g : ghosts) 
            if (g.getState() == Ghost.DEAD) 
                return true;
        return false;
    }
    
}
