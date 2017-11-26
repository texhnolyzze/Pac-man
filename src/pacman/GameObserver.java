package pacman;

/**
 *
 * @author Texhnolyze
 */
public interface GameObserver {
    
    int PACMAN_TILE_CHANGED         = 0;
    int PACMAN_ATE_THE_DOT          = 1;
    int PACMAN_ATE_THE_ENERGIZER    = 2;
    int PACMAN_ATE_THE_BONUS        = 3;
    int EXTRA_LIFE                  = 4;
    int STAGE_STARTS                = 6;
    int PACMAN_LEAVED_DOTS_LINE     = 7;
    int PACMAN_DIED                 = 8;
    int POWER_UP_END                = 9;
    int GHOST_DIED                  = 10;
    
    public void notify(int event);
    
}
