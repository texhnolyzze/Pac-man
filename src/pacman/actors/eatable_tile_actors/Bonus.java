package pacman.actors.eatable_tile_actors;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import pacman.App;
import static pacman.App.DRAWING_TILE_SIZE;

/**
 *
 * @author Texhnolyze
 */
public class Bonus extends EatableTileActor {
    
    public static final int BONUSES_COUNT = 8;

    public static final int BONUS_1 = 0;
    public static final int BONUS_2 = 1;
    public static final int BONUS_3 = 2;
    public static final int BONUS_4 = 3;
    public static final int BONUS_5 = 4;
    public static final int BONUS_6 = 5;
    public static final int BONUS_7 = 6;
    public static final int BONUS_8 = 7;
    
    public static final Image[] BONUSES = new Image[8];
    
    static {
        for (int i = 0; i < BONUSES_COUNT; i++) 
            BONUSES[i] = App.getImage("bonus_" + (i + 1));
    }
    
    public final int type;
    
    public final float x, y;
    
    {
        isEated = true; //by default all bonuses are eated.
    }
    
    public Bonus(float x, float y, int type) {
        super((int) (x / DRAWING_TILE_SIZE), (int) (y / DRAWING_TILE_SIZE));
        this.x = x;
        this.y = y;
        this.type = type;
    }

    @Override
    public void draw(GraphicsContext gc) { 
        if (!isEated) {
            Image i = BONUSES[type];
            float w_div2 = (float) (i.getWidth() / 2);
            float h_div2 = (float) (i.getHeight() / 2);
            gc.translate(x - w_div2, y - h_div2);
            gc.drawImage(i, 0, 0);
            gc.translate(-(x - w_div2), -(y - h_div2));            
        }
    }

    @Override
    protected void draw0(GraphicsContext gc) {
        //nothing to do here.
    }
    
}
