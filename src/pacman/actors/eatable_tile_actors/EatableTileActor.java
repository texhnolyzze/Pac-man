package pacman.actors.eatable_tile_actors;

import javafx.scene.canvas.GraphicsContext;
import static pacman.App.DRAWING_TILE_SIZE;
import pacman.actors.TileActor;

/**
 *
 * @author Texhnolyze
 */
public abstract class EatableTileActor extends TileActor {

    protected boolean isEated;
    
    public EatableTileActor(int tile_x, int tile_y) {
        super(tile_x, tile_y);
    }
    
    public void eat() {
        isEated = true;
    }
    
    public void reset() {
        isEated = false;
    }
    
    public boolean isEated() {
        return isEated;
    }

    @Override
    public void update() {
        //nothing to do here.
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (!isEated) {
            gc.translate(tile_x * DRAWING_TILE_SIZE, tile_y * DRAWING_TILE_SIZE);
            draw0(gc);
            gc.translate(-tile_x * DRAWING_TILE_SIZE, -tile_y * DRAWING_TILE_SIZE);
        }
    }
    
    protected abstract void draw0(GraphicsContext gc);
    
}
