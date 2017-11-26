package pacman.impls.game_containers;

import pacman.GameContainer;
import pacman.impls.game_field_impls.ClassicGameField;
import pacman.impls.game_judge_impls.ClassicGameJudge;

/**
 *
 * @author Texhnolyze
 */
public class ClassicGameContainer extends GameContainer {
    
    public static GameContainer create() {
        GameContainer gc = new ClassicGameContainer();
        gc.field = new ClassicGameField(gc);
        gc.pacman = gc.field.initPacman();
        gc.ghosts = gc.field.initGhosts();
        gc.judge = new ClassicGameJudge(gc);
        return gc;
    }
    
}
