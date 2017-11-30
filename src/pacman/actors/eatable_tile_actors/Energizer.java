package pacman.actors.eatable_tile_actors;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import pacman.App;
import pacman.utils.TickOscillator;

/**
 *
 * @author Texhnolyze
 */
public class Energizer extends EatableTileActor {

    public static boolean ANIMATE = true;
    
    private static final Image ENERGIZER = App.getImage("energizer");
    
    private final TickOscillator t = new TickOscillator(15);
    
    public Energizer(int tile_x, int tile_y) {
        super(tile_x, tile_y);
    }

    @Override
    public void reset() {
        super.reset(); 
        t.reset();
    }
    
    @Override
    protected void draw0(GraphicsContext gc) {
        if ((ANIMATE && t.getState()) || (!ANIMATE)) 
            gc.drawImage(ENERGIZER, 0, 0);
        if (ANIMATE) 
            t.tick();
    }
    
}
