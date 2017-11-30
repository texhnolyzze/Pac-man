package pacman.actors.dynamic_tile_actors;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import pacman.App;
import static pacman.App.DRAWING_TILE_SIZE;
import pacman.GameContainer;
import pacman.actors.GameField;
import pacman.actors.TileActor;
import pacman.utils.Animation;
import pacman.utils.Direction;
import static pacman.utils.Direction.LEFT;
import static pacman.utils.Direction.TOP;
import pacman.utils.IntVec2;
import pacman.utils.Vector2;

/**
 *
 * @author Texhnolyze
 */
public abstract class DynamicTileActor extends TileActor {
    
    public static final int PACMAN  = 0;
    public static final int BLINKY  = 1;
    public static final int PINKY   = 2;
    public static final int INKY    = 3;
    public static final int CLYDE   = 4;
    
    public static final int DEAD = 1; //general state for ghosts and pacman.

    public final int id;
    public final int type;
    
    protected int state, initState;
    protected GameContainer game;
    
    protected Direction dir, initDir;

    public final Vector2 pos = new Vector2(), initPos = new Vector2();
    
    protected Image[] images;
    protected Animation[] anims;
    
    public float velocity;
    
    protected final Vector2 vTemp = new Vector2();
    protected final IntVec2 tile_temp1 = new IntVec2();
    protected final IntVec2 tile_temp2 = new IntVec2();
    protected final IntVec2 tile_temp3 = new IntVec2();
    
    public DynamicTileActor(int id, int type, float x, float y, Direction dir, int state, GameContainer game) {
        if ((state & DEAD) != 0)
            throw new RuntimeException();
        this.id = id;
        this.type = type;
        this.game = game;
        this.pos.set(x, y);
        this.state = state;
        this.initState = state;
        this.initPos.set(pos);
        this.initDir = dir;
        this.dir = initDir;
        this.tile_x = (int) (x / DRAWING_TILE_SIZE);
        this.tile_y = (int) (y / DRAWING_TILE_SIZE);
        this.images = getImagesOf(type);
        this.anims = getAnimationOf(type);
    }
    
    public void reset() {
        dir = initDir;
        pos.set(initPos);
        state = initState;
        canMoveInCurrentDir = true;
        this.tile_x = (int) (pos.x / DRAWING_TILE_SIZE);
        this.tile_y = (int) (pos.y / DRAWING_TILE_SIZE);
        for (Animation a : anims) a.reset();
    }
    
    public int getType() {
        return type;
    }
    
    public int getSerialNumber() {
        return type;
    }
    
    public int getState() {
        return state;
    }
    
    public void dead() {
        state = DEAD;
    }
    
    protected void setToNextCoords(Direction dir, IntVec2 dest) {
        int x = tile_x, y = tile_y, bound;
        if (dir.isHorizontal()) {
            bound = game.field.xBoundTile;
            x = dir == LEFT ? (x - 1 + bound) % bound : (x + 1 + bound) % bound;
        } else {
            bound = game.field.yBoundTile;
            y = dir == TOP ? (y - 1 + bound) % bound : (y + 1 + bound) % bound;
        }
        dest.x = x;
        dest.y = y;
    }

    public boolean animationStopped;
    
    @Override
    public void draw(GraphicsContext gc) {
        gc.translate(pos.x, pos.y);
        int animIdx = getCurrentAnimationIdx();
        if (animIdx != -1) {
            anims[animIdx].setAnimationStop(animationStopped);
            double w = anims[animIdx].getWidth() / 2;
            double h = anims[animIdx].getHeight() / 2;
            gc.translate(-w, -h);
            anims[animIdx].draw(gc);
            gc.translate(w, h);
        } else {
            int imgIdx = getCurrentImageIdx();
            gc.drawImage(
                images[imgIdx], 
                -images[imgIdx].getWidth() / 2, 
                -images[imgIdx].getHeight() / 2
            );
        }
        gc.translate(-pos.x, -pos.y);
    }
    
    protected abstract int getCurrentAnimationIdx();
    
    protected abstract int getCurrentImageIdx();
    
    protected boolean canChangeDirTo(Direction dir) {
        if (!game.field.canChangeDirTo(dir, this)) 
            return false;
        setToNextCoords(dir, tile_temp1);
        Vector2 node = game.field.getNodeOn(tile_x, tile_y);
        if (this.dir.getOpposite() == dir) {
            if (node == null) return true;
            else {
                if (!isObstacle(tile_temp1.x, tile_temp1.y))
                    return true;
                else {
                    float dist = pos.dist(node);
                    return dist > velocity;
                }
            }
        } else {
            if (isObstacle(tile_temp1.x, tile_temp1.y)) 
                return false;
            float dist = pos.dist(node);
            return dist < velocity;
        }
    }
    
    protected void changeDirTo(Direction dir) {
        if (this.dir != dir) {
            Vector2 node = game.field.getNodeOn(tile_x, tile_y);
            if (this.dir.getOpposite() != dir) {
                pos.x = node.x;
                pos.y = node.y;
            }
            this.dir = dir;
        }
    }
    
    public boolean isObstacle(int tile_x, int tile_y) {
        return game.field.lookup(tile_x, tile_y) == GameField.WALL;
    }

    protected boolean inTheNode;
    protected boolean canMoveInCurrentDir = true; //init dir is always valid.
    
    protected boolean move() {
        inTheNode = false; 
        setToNextCoords(dir, tile_temp1);
        Vector2.setToTranslationVector(velocity, vTemp, dir);
        pos.addLocal(vTemp);
        Vector2 node = game.field.getNodeOn(tile_x, tile_y);
        if (node != null) {
            if (pos.equals(node, velocity)) {
                inTheNode = true;
                if (isObstacle(tile_temp1.x, tile_temp1.y)) {
                    pos.x = node.x;
                    pos.y = node.y;
                    canMoveInCurrentDir = false;
                }
            }
        }
        int xb = game.field.xBoundPix;
        int yb = game.field.yBoundPix;
        pos.closureLocal(xb, yb);
        tile_x = (int) (pos.x / DRAWING_TILE_SIZE);
        tile_y = (int) (pos.y / DRAWING_TILE_SIZE);
        return tile_x == tile_temp1.x && tile_y == tile_temp1.y;
    }

    @Override
    public int hashCode() {
        return 203 + this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final DynamicTileActor other = (DynamicTileActor) obj;
        return this.id == other.id;
    }
    
    public static final Animation[] getAnimationOf(int actor) {
        if (actor == PACMAN) {
            Animation[] animations = new Animation[5];
            Image base = App.getImage("pacman_base");
            for (int dirId = 0; dirId < 4; dirId++) 
                animations[dirId] 
                        = new Animation(
                            2, 
                            base, 
                            App.getImage("pacman_" + dirId + "_" + 0), 
                            App.getImage("pacman_" + dirId + "_" + 1)
                        );
            Image[] deadFrames = new Image[13];
            deadFrames[0] = base;
            for (int i = 1; i < deadFrames.length; i++) 
                deadFrames[i] = App.getImage("pacman_died_" + (i - 1));
            animations[4] = new Animation(6, deadFrames);
            return animations;
        } else {
            Animation[] animations = new Animation[6];
            int ghost = actor - 1;
            for (int dirId = 0, idx = 0; dirId < 4; dirId++, idx += 2) {
                Image i1 = App.getImage("ghost_" + ghost + "_" + idx);
                Image i2 = App.getImage("ghost_" + ghost + "_" + (idx + 1));
                animations[dirId] = new Animation(4, i1, i2);
            }
            Image gf0 = App.getImage("ghost_frightened_0");
            Image gf1 = App.getImage("ghost_frightened_1");
            Image gf2 = App.getImage("ghost_frightened_2");
            Image gf3 = App.getImage("ghost_frightened_3");
            animations[4] = new Animation(4, gf0, gf1);
            animations[5] = new Animation(4, gf0, gf1, gf2, gf3);
            return animations;
        }
    }
    
    private static final Image DEAD_LEFT    = App.getImage("ghost_dead_0");
    private static final Image DEAD_BOTTOM  = App.getImage("ghost_dead_1");    
    private static final Image DEAD_RIGHT   = App.getImage("ghost_dead_2");
    private static final Image DEAD_TOP     = App.getImage("ghost_dead_3");
    
    private static final Image[] GHOST_DEAD_FRAMES = {
        DEAD_LEFT, DEAD_BOTTOM, DEAD_RIGHT, DEAD_TOP
    };
    
    public static final Image[] getImagesOf(int actor) {
        if (actor == PACMAN)
            return null;
        else {
            return GHOST_DEAD_FRAMES;
        }
    }
    
    
}
