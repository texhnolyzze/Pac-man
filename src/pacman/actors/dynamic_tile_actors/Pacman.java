package pacman.actors.dynamic_tile_actors;

import javafx.scene.canvas.GraphicsContext;
import pacman.GameContainer;
import pacman.GameObserver;
import static pacman.GameObserver.PACMAN_DIED;
import static pacman.GameObserver.PACMAN_TILE_CHANGED;
import pacman.actors.GameField;
import pacman.utils.Direction;
import pacman.utils.Timer;
import static pacman.GameObserver.POWER_UP_END;

/**
 *
 * @author Texhnolyze
 */
public class Pacman extends DynamicTileActor {

    public static final int NORMAL      = 2;
    public static final int POWER_UP    = 4;
    
    private Direction desired;
    
    public Pacman(int id, float x, float y, Direction dir, GameContainer game) {
        super(id, PACMAN, x, y, dir, NORMAL, game);
        desired = dir;
    }

    @Override
    public void reset() {
        super.reset(); 
        desired = initDir;
    }

    @Override
    public boolean isObstacle(int tile_x, int tile_y) {
        if (super.isObstacle(tile_x, tile_y))
            return true;
        return game.field.lookup(tile_x, tile_y) == GameField.GHOSTS_PEN_GATES;
    }
    
    public void setDesiredDirection(Direction desired) {
        this.desired = desired;
    }
    
    public void powerUp() {
        state = POWER_UP;
    }
    
    @Override
    public void dead() {
        super.dead(); 
        anims[4].setDrawOnce(true);
    }

    @Override
    protected int getCurrentAnimationIdx() {
        if (state == DEAD) 
            return 4;
        else 
            return dir.id;
    }

    @Override
    protected int getCurrentImageIdx() {
        return -1;
    }

    @Override
    public void update() {
        if (state == POWER_UP && game.judge.powerUpEnd()) {
            state = NORMAL;
            game.notifyObservers(POWER_UP_END);
        }
        if (canMoveInCurrentDir) {
            velocity = game.field.getCurrentVelocityFor(this);
            boolean tilePosChanged = move();
            if (tilePosChanged) {
                game.notifyObservers(PACMAN_TILE_CHANGED);
            }
        }
        if (dir != desired) {
            if (canChangeDirTo(desired)) {
                changeDirTo(desired);
                canMoveInCurrentDir = true;
                animationStopped = false;
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (!canMoveInCurrentDir) 
            animationStopped = true;
        super.draw(gc); 
    }
    
}
