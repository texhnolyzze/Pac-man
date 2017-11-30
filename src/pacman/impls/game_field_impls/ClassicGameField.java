package pacman.impls.game_field_impls;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import pacman.App;
import static pacman.App.DRAWING_TILE_SIZE;
import static pacman.App.DTS_DIV_TS;
import pacman.GameContainer;
import pacman.GameController;
import pacman.actors.GameField;
import pacman.actors.dynamic_tile_actors.DynamicTileActor;
import pacman.actors.dynamic_tile_actors.Ghost;
import pacman.actors.dynamic_tile_actors.Pacman;
import pacman.actors.eatable_tile_actors.Bonus;
import pacman.utils.Direction;
import pacman.utils.IntVec2;
import pacman.utils.TickOscillator;
import pacman.utils.Vector2;

/**
 *
 * @author Texhnolyze
 */
public class ClassicGameField extends GameField {

    private static final Image[] MAZE_VIEWS = {
        App.getImage("classic_maze_views", "view_0"),
        App.getImage("classic_maze_views", "view_1")
    };
    
    private static final int[][] MAZE = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
        {3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3},
        {3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3},
        {3, 2, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 2, 3},
        {3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3},
        {3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3},
        {3, 1, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 1, 3},
        {3, 1, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 1, 3},
        {3, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 3},
        {3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 0, 3, 3, 0, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3},
        {0, 0, 0, 0, 0, 3, 1, 3, 3, 3, 3, 3, 0, 3, 3, 0, 3, 3, 3, 3, 3, 1, 3, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 3, 1, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 1, 3, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 3, 1, 3, 3, 0, 3, 3, 3, 4, 4, 3, 3, 3, 0, 3, 3, 1, 3, 0, 0, 0, 0, 0},
        {3, 3, 3, 3, 3, 3, 1, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 3, 0, 3, 3, 1, 3, 3, 3, 3, 3, 3},
        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
        {3, 3, 3, 3, 3, 3, 1, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 3, 0, 3, 3, 1, 3, 3, 3, 3, 3, 3},
        {0, 0, 0, 0, 0, 3, 1, 3, 3, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 3, 3, 1, 3, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 3, 1, 3, 3, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 3, 3, 1, 3, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 3, 1, 3, 3, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 3, 3, 1, 3, 0, 0, 0, 0, 0},
        {3, 3, 3, 3, 3, 3, 1, 3, 3, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 3, 3, 1, 3, 3, 3, 3, 3, 3},
        {3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3},
        {3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3},
        {3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3},
        {3, 2, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 2, 3},
        {3, 3, 3, 1, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 1, 3, 3, 3},
        {3, 3, 3, 1, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 1, 3, 3, 3},
        {3, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 3},
        {3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3},
        {3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3},
        {3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3},
        {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };
    
    private static final float BONUS_X_REAL = 14F * DRAWING_TILE_SIZE;
    private static final float BONUS_Y_REAL = 20.5F * DRAWING_TILE_SIZE;
    
    private static Bonus[] createBonuses() {
        Bonus[] bonuses = new Bonus[Bonus.BONUSES_COUNT];
        for (int i = 0; i < bonuses.length; i++) 
            bonuses[i] = new Bonus(BONUS_X_REAL, BONUS_Y_REAL, i);
        return bonuses;
    }
    
    private static final IntVec2[] NODES_TILE = {
        new IntVec2(1, 4),
        new IntVec2(6, 4),
        new IntVec2(12, 4),
        new IntVec2(15, 4),
        new IntVec2(21, 4),
        new IntVec2(26, 4),
        new IntVec2(1, 8),
        new IntVec2(6, 8),
        new IntVec2(9, 8),
        new IntVec2(12, 8),
        new IntVec2(15, 8),
        new IntVec2(18, 8),
        new IntVec2(21, 8),
        new IntVec2(26, 8),
        new IntVec2(1, 11),
        new IntVec2(6, 11),
        new IntVec2(9, 11),
        new IntVec2(12, 11),
        new IntVec2(15, 11),
        new IntVec2(18, 11),
        new IntVec2(21, 11),
        new IntVec2(26, 11),
        new IntVec2(9, 14),
        new IntVec2(12, 14),
        new IntVec2(15, 14),
        new IntVec2(18, 14),
        new IntVec2(6, 17),
        new IntVec2(9, 17),
        new IntVec2(18, 17),
        new IntVec2(21, 17),
        new IntVec2(9, 20),
        new IntVec2(18, 20),
        new IntVec2(1, 23),
        new IntVec2(6, 23),
        new IntVec2(9, 23),
        new IntVec2(12, 23),
        new IntVec2(15, 23),
        new IntVec2(18, 23),
        new IntVec2(21, 23),
        new IntVec2(26, 23),
        new IntVec2(1, 26),
        new IntVec2(3, 26),
        new IntVec2(6, 26),
        new IntVec2(9, 26),
        new IntVec2(12, 26),
        new IntVec2(15, 26),
        new IntVec2(18, 26),
        new IntVec2(21, 26),
        new IntVec2(24, 26),
        new IntVec2(26, 26),
        new IntVec2(1, 29),
        new IntVec2(3, 29),
        new IntVec2(6, 29),
        new IntVec2(9, 29),
        new IntVec2(12, 29),
        new IntVec2(15, 29),
        new IntVec2(18, 29),
        new IntVec2(21, 29),
        new IntVec2(24, 29),
        new IntVec2(26, 29),
        new IntVec2(1, 32),
        new IntVec2(12, 32),
        new IntVec2(15, 32),
        new IntVec2(26, 32)
    };
    
    private static final Map<IntVec2, Vector2> NODES_M = new HashMap<>();
    
    static {
        for (int i = 0; i < NODES_TILE.length; i++) {
            IntVec2 v = NODES_TILE[i];
            float x = (v.x + 0.5F) * DRAWING_TILE_SIZE;
            float y = (v.y + 0.5F) * DRAWING_TILE_SIZE;
            NODES_M.put(v, new Vector2(x, y));
        }
    }
    
    public ClassicGameField(GameContainer game) {
        super(game, MAZE, MAZE_VIEWS, NODES_M);
    }

    @Override
    public void reset() {
        super.reset(); 
        for (Bonus b : bonuses) b.eat();
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc); 
        bonuses[currentBonusIdx].draw(gc);
    }

    @Override
    protected int getConstantTick() {
        return 2;
    }
    
    private final TickOscillator t = new TickOscillator(10);
    
    @Override
    protected int getCurrentViewIndex() {
        int idx = 0;
        if (flickering) {
            if (t.getState()) idx = 1;
            t.tick();
        } 
        return idx;
    }
    
    private static final float[][][] SPEED_TABLE = {
        {   
            {0.8F, 0.71F, 0.9F, 0.79F},
            {0.9F, 0.79F, 0.95F, 0.83F},
            {1F, 0.87F, 1F, 0.87F},
            {0.9F, 0.79F, 0.9F, 0.79F}
        },
        {
            {0.75F, 0.5F, 0.4F},
            {0.85F, 0.55F, 0.45F},
            {0.95F, 0.6F, 0.5F},
            {0.95F, 0.95F, 0.5F}
        }
    };
    
    private static float MAX_V = 1.25F * DTS_DIV_TS;
    
    private static float getSpeedFor(float percents) {
        return MAX_V * percents;
    }
    
    @Override
    public float getCurrentVelocityFor(DynamicTileActor a) {
        if (a.getClass() == Ghost.class && (a.getState() & Ghost.DEAD) != 0)
            return 1.5F * MAX_V;
        int n = game.lvl;
        int idx1;
        int idx2 = n == 1 ? 0 : n <= 4 ? 1 : n <= 20 ? 2 : 3;
        int idx3;
        if (a.getClass() == Ghost.class) {
            idx1 = 1;
            Ghost g = (Ghost) a;
            if (isInTunnel(g)) idx3 = 2;
            else if ((g.getState() & Ghost.FRIGHTENED) != 0) idx3 = 1;
            else idx3 = 0;
        } else {
            idx1 = 0;
            if (!pacmanOnDotsLine) {
                if (!game.atLeastOneGhostIsFrightened()) 
                    idx3 = 0;
                else 
                    idx3 = 2;
            } else {
                if (!game.atLeastOneGhostIsFrightened()) 
                    idx3 = 1;
                else 
                    idx3 = 3;
            }
        }
        return getSpeedFor(SPEED_TABLE[idx1][idx2][idx3]);
    }
    
    @Override
    public Pacman initPacman() {
        float x = 14F * DRAWING_TILE_SIZE;
        float y = 26.5F * DRAWING_TILE_SIZE;
        return new Pacman(0, x, y, Direction.LEFT, game);
    }
    
    @Override
    public Ghost[] initGhosts() {
        return new Ghost[] {
            new Ghost(1, Ghost.BLINKY, 14 * DRAWING_TILE_SIZE, 14.5F * DRAWING_TILE_SIZE, Direction.LEFT, Ghost.SCATTER, game),
            new Ghost(2, Ghost.PINKY, 14 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE, Direction.BOTTOM, Ghost.IN_PEN, game),
            new Ghost(3, Ghost.INKY, 12 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE, Direction.TOP, Ghost.IN_PEN, game),
            new Ghost(4, Ghost.CLYDE, 16 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE, Direction.TOP, Ghost.IN_PEN, game)
        };
    }

    @Override
    public boolean canChangeDirTo(Direction desired, DynamicTileActor a) {
        if (a.getClass() == Pacman.class) return true;
        Ghost g = (Ghost) a;
        if ((g.getState() & Ghost.FRIGHTENED) != 0) return true;
        if (desired != Direction.TOP) return true;
        int x = g.tile_x;
        int y = g.tile_y;
        boolean b_1 = x == 12 || x == 15;
        boolean b_2 = y == 14 || y == 26;
        return !(b_1 && b_2);
    }
    
    private static final Vector2[][] IN_PEN_WAYPOINTS = {
        {
            new Vector2(14 * DRAWING_TILE_SIZE, 18 * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 17 * DRAWING_TILE_SIZE)
        },
        {
            new Vector2(12 * DRAWING_TILE_SIZE, 17 * DRAWING_TILE_SIZE),
            new Vector2(12 * DRAWING_TILE_SIZE, 18 * DRAWING_TILE_SIZE)
        },
        {
            new Vector2(16 * DRAWING_TILE_SIZE, 17 * DRAWING_TILE_SIZE),
            new Vector2(16 * DRAWING_TILE_SIZE, 18 * DRAWING_TILE_SIZE)
        }
    };
    
    @Override
    public Vector2[] getWaypointsInPen(Ghost g) {
        if (g.getType() == Ghost.BLINKY) 
            return null;
        else
            return IN_PEN_WAYPOINTS[g.getSerialNumber() - 2];
    }
    
    private static final Vector2[][] LEAVING_PEN_WAYPOINTS = { 
        {
            new Vector2(14 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 14.5F * DRAWING_TILE_SIZE)
        }, {
            new Vector2(14 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 14.5F * DRAWING_TILE_SIZE)
        }, {
            new Vector2(12 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 14.5F * DRAWING_TILE_SIZE)
        }, {
            new Vector2(16 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 17.5F * DRAWING_TILE_SIZE),
            new Vector2(14 * DRAWING_TILE_SIZE, 14.5F * DRAWING_TILE_SIZE)
        }
    };

    @Override
    public Vector2[] getWaypointsForLeavingPen(Ghost g) {
        return LEAVING_PEN_WAYPOINTS[g.getSerialNumber() - 1];
    }

    private static final IntVec2[] SCATTER_TARGET = {
        new IntVec2(25, 0),
        new IntVec2(2, 0),
        new IntVec2(27, 35),
        new IntVec2(0, 35)
    };
    
    @Override
    public IntVec2 getScatterStateTarget(Ghost g) {
        return SCATTER_TARGET[g.getSerialNumber() - 1];
    }
    
    private int prevPlayerScore = 0;
    private int scoreToExtraLife = 10000;

    private final Bonus[] bonuses = createBonuses();
    private int currentBonusIdx = -1;
    private boolean bonus1Activated, bonus2Activated;
    
    @Override
    public void notify(int event) {
        super.notify(event);
        checkBonus();
        checkExtraLife();
        switch (event) {
            case STAGE_STARTS:
                currentBonusIdx = Math.min(currentBonusIdx + 1, Bonus.BONUSES_COUNT - 1);
                bonus1Activated = false;
                bonus2Activated = false;
                break;
            case PACMAN_TILE_CHANGED:
                Bonus b = bonuses[currentBonusIdx];
                if (!b.isEated()) {
                    int px = game.pacman.tile_x;
                    int py = game.pacman.tile_y;
                    if (px == b.tile_x && py == b.tile_y) {
                        b.eat();
                        lastEated = b;
                        game.notifyObservers(PACMAN_ATE_THE_BONUS);
                    }
                }   break;
            case PACMAN_DIED:
                bonuses[currentBonusIdx].eat();
                break;
            default:
                break;
        }
    }
    
    private void checkExtraLife() {
        int n = game.player.getScore();
        int delta = n - prevPlayerScore;
        scoreToExtraLife -= delta;
        if (scoreToExtraLife <= 0) {
            scoreToExtraLife += 10000;
            game.notifyObservers(EXTRA_LIFE);
        }
        prevPlayerScore = game.player.getScore();
    }
    
    private void checkBonus() {
        if (!bonus1Activated) {
            if (dotsEated == 70) {
                bonus1Activated = true;
                bonuses[currentBonusIdx].reset();
            }
        } else if (dotsEated == 100) bonuses[currentBonusIdx].eat();
        if (!bonus2Activated) {
            if (dotsEated == 170) {
                bonus2Activated = true;
                bonuses[currentBonusIdx].reset();
            }
        } else if (dotsEated == 200) bonuses[currentBonusIdx].eat();
        
    }
    
    private static boolean isInTunnel(Ghost g) {
        int x = g.tile_x;
        return g.tile_y == 17 && ((x >= 0 && x <= 4) || (x >= 23 && x <= 27));
    }

    private final Vector2 READY_LABEL_POS, LIVES_REMAIN_LABEL_POS, SCORE_LABEL_POS; 
    
    {
        int x1 = App.centerText("READY?", GameController.F3, App.IN_THE_CENTER, xBoundPix);
        int x2 = App.centerText("LIVES: 3", GameController.F3, App.IN_THE_CENTER, xBoundPix);
        int x3 = App.centerText("SCORE: 0", GameController.F3, App.IN_THE_CENTER, xBoundPix);
        READY_LABEL_POS = new Vector2(x1, 20.8F * DRAWING_TILE_SIZE);
        LIVES_REMAIN_LABEL_POS = new Vector2(x2, 35.3F * DRAWING_TILE_SIZE);
        SCORE_LABEL_POS = new Vector2(x3, 1.8F * DRAWING_TILE_SIZE);
    }
    
    @Override
    public Vector2 getReadyLabelPosition() {
        return READY_LABEL_POS;
    }
    
    @Override
    public Vector2 getLivesRemainLabelPosition() {
        return LIVES_REMAIN_LABEL_POS;
    }
    
    @Override
    public Vector2 getScoreLabelPosition() {
        return SCORE_LABEL_POS;
    }
    
}
