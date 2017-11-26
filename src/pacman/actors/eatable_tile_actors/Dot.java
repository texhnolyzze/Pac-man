package pacman.actors.eatable_tile_actors;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import pacman.App;

/**
 *
 * @author Texhnolyze
 */
public class Dot extends EatableTileActor {

    private static final Image DOT = App.getImage("dot");
    
    public Dot(int tile_x, int tile_y) {
        super(tile_x, tile_y);
    }

    @Override
    protected void draw0(GraphicsContext gc) {
        gc.drawImage(DOT, 0, 0);
    }
    
}
