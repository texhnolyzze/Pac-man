package pacman.actors;
/**
 *
 * @author Texhnolyze
 */
public abstract class TileActor implements Actor {
    
    public int tile_x, tile_y;
    
    protected TileActor() {}
    
    public TileActor(int tile_x, int tile_y) {
        this.tile_x = tile_x;
        this.tile_y = tile_y;
    }
    
}
