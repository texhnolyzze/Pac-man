package pacman.actors;

import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author Texhnolyze
 */
public interface Actor {
    
    public void update();

    public void draw(GraphicsContext gc);
    
}
