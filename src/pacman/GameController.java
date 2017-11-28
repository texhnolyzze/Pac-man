package pacman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import static pacman.App.DRAWING_TILE_SIZE;
import static pacman.App.IN_THE_CENTER;
import static pacman.App.centerText;
import static pacman.GameObserver.STAGE_STARTS;
import pacman.actors.GameField;
import pacman.actors.dynamic_tile_actors.DynamicTileActor;
import pacman.actors.dynamic_tile_actors.Ghost;
import pacman.actors.dynamic_tile_actors.Pacman;
import pacman.actors.eatable_tile_actors.Bonus;
import pacman.actors.eatable_tile_actors.Energizer;
import pacman.impls.game_containers.ClassicGameContainer;
import pacman.utils.Direction;
import pacman.utils.Sound;
import pacman.utils.Timer;
import pacman.utils.Vector2;

/**
 *
 * @author Texhnolyze
 */
public class GameController extends AnimationTimer implements EventHandler<KeyEvent>, GameObserver {
    
    private static final Sound MAIN_SIREN_1         = App.getSound("main_siren_1");
    private static final Sound MAIN_SIREN_2         = App.getSound("main_siren_2");
    private static final Sound MAIN_SIREN_3         = App.getSound("main_siren_3");
    private static final Sound MAIN_SIREN_4         = App.getSound("main_siren_4");
    private static final Sound MAIN_SIREN_5         = App.getSound("main_siren_5");
    private static final Sound POWER_UP_SIREN       = App.getSound("power_up_siren");
    private static final Sound DEAD_GHOSTS_SIREN    = App.getSound("dead_ghosts_siren");
    
    private static final Sound EXTRA_LIFE_SOUND     = App.getSound("extra_life");
    private static final Sound EATING_DOTS          = App.getSound("eating_dots");
    private static final Sound EATING_BONUS         = App.getSound("bonus", 0.3D);
    
    private static final Sound EATING_GHOST         = App.getSound("eating_ghost");
    private static final Sound EATING_PACMAN        = App.getSound("eating_pacman");
    
    private static final Sound[] ALL                = {
        MAIN_SIREN_1, MAIN_SIREN_2, MAIN_SIREN_3, MAIN_SIREN_4, MAIN_SIREN_5,
        POWER_UP_SIREN, DEAD_GHOSTS_SIREN, EXTRA_LIFE_SOUND, EATING_DOTS, 
        EATING_BONUS, EATING_GHOST, EATING_PACMAN
    };
    
    private static final Class[][] GAME_CONTAINERS_CLASSES = {
        {ClassicGameContainer.class}, {}
    };
    
    private static final int MENU               = 0;
    private static final int PLAYING            = 1;
    private static final int STAGE_PASSED_STATE = 2;
    private static final int PLAYER_DEAD        = 3;
    private static final int GAME_OVER          = 4;
    
    private GameContainer game;
    
    private final GraphicsContext gc;
    
    private int state;
    
    public GameController(GraphicsContext gc) {
        this.gc = gc;
    }
    
    private void clearView() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, App.W, App.H);
    }

//  Game name drawing attributes
    private static final String GAME_NAME = "PACMAN";
    private static final Font F1 = App.getRetroFontOf(30);
    private static final double X1 = centerText(GAME_NAME, F1, IN_THE_CENTER);
    private static final double Y1 = App.H / 3D;
//  

//  Available game containers name drawing attributes
    private int selectedPage;
    private int selectedContainer;
    private static final Font F2 = App.getRetroFontOf(20);
    private static final String[][] GAME_CONTAINERS = {
        {"CLASSIC"}
    };
    private static final int[][] GAME_CONTAINERS_X = new int[GAME_CONTAINERS.length][];
    static {
        for (int i = 0; i < GAME_CONTAINERS.length; i++) {
            GAME_CONTAINERS_X[i] = new int[GAME_CONTAINERS[i].length];
            for (int j = 0; j < GAME_CONTAINERS_X[i].length; j++) {
                int x = App.centerText(GAME_CONTAINERS[i][j], F2, IN_THE_CENTER);
                GAME_CONTAINERS_X[i][j] = x;
            }
        }
    }
    private static final int X3 = App.centerText("< 1 / 1 >", F2, App.IN_THE_CENTER);
//
    
    private void drawMenu() {
        gc.setFont(F1);
        gc.setFill(Color.YELLOW);
        gc.fillText(GAME_NAME, X1, Y1);
        gc.setFont(F2);
        double y_offset = Y1 + App.H / 6;
        for (int i = 0; i < GAME_CONTAINERS[selectedPage].length; i++) {
            String s = GAME_CONTAINERS[selectedPage][i];
            Paint p;
            if (GAME_CONTAINERS[selectedPage][selectedContainer].equals(s)) 
                gc.setFill(Color.WHITE);
            else 
                gc.setFill(Color.GRAY);
            gc.fillText(s, GAME_CONTAINERS_X[selectedPage][i], y_offset);
            y_offset += App.H / 5;
        }
        gc.setFill(Color.WHITE);
        gc.fillText("< " + (selectedPage + 1) + " / " + GAME_CONTAINERS.length + " >", X3, App.H / 1.1D);
    }

//  HUD drawing attributes
    //this font also used to draw score increments.
    public static final Font F3 = App.getRetroFontOf(DRAWING_TILE_SIZE); 
    private static final String READY_STR = "READY?";
    private String scoreStr, livesRemainStr;
//

    private void drawReadyLabel() {
        gc.setFont(F3);
        gc.setFill(Color.YELLOW);
        Vector2 v = game.field.getReadyLabelPosition();
        gc.fillText(READY_STR, v.x, v.y);
    }
    
    private void drawHud() {
        gc.setFont(F3);
        gc.setFill(Color.YELLOW);
        Vector2 v1 = game.field.getScoreLabelPosition();
        gc.fillText(scoreStr, v1.x, v1.y);
        Vector2 v2 = game.field.getLivesRemainLabelPosition();
        gc.fillText(livesRemainStr, v2.x, v2.y);
    }
    
    private static final int DOT_EATING_SCORE_INCREMENT         = 10;
    private static final int ENERGIZER_EATING_SCORE_INCREMENT   = 50;
    
    private static final int[] BONUS_EATING_SCORE_INCREMENT = {
        100, 200, 500, 700, 1000, 2000, 3000, 5000
    };
    
//  Score increment drawing attributes
    private int ghostEatingScoreIncrement;
    private String ghostEatingScoreIncrementStr;
    private int x2, y2; //coordinates of score increment str after eating ghost
    
    static class BonusIncrementStr {
        private final Timer eatingTime = new Timer();
        private float x, y;
        private String str;
    }
    
    private Queue<BonusIncrementStr> q = new LinkedList<>();
//
    
    private void drawBonusEatingScoreIncrements() {
        if (!q.isEmpty()) {
            Iterator<BonusIncrementStr> it = q.iterator();
            gc.setFont(F3);
            gc.setFill(Color.PINK);
            while (it.hasNext()) {
                BonusIncrementStr bes = it.next();
                gc.fillText(bes.str, bes.x, bes.y);
                if (bes.eatingTime.passed(1000)) it.remove();
            }
        }
    }
    
    private final Timer t1 = new Timer();
    private final Timer t2 = new Timer();
    private final Timer t3 = new Timer();
    private final Timer t4 = new Timer();
    
    private boolean prePlaying;
    private boolean needFrightGhosts;
    
    private Ghost deadGhost;
    
    @Override
    public void handle(long now) {
        clearView();
        switch (state) {
            case MENU:
                drawMenu();
                break;
            case PLAYING:
                game.field.draw(gc);
                drawHud();
                drawBonusEatingScoreIncrements();
                if (prePlaying) {
                    if (t1.passed(1000)) {
                        drawReadyLabel();
                        game.pacman.draw(gc);
                        for (Ghost g : game.ghosts) g.draw(gc);
                        if (t1.passed(3500)) {
                            prePlaying = false;
                            game.judge.resumeTimers();
                            game.pacman.animationStopped = false;
                            for (Ghost g : game.ghosts) g.animationStopped = false;
                            ALL[getMainSirenNeedToPlayIdx()].play(true);
                            Energizer.ANIMATE = true;
                        }
                    }
                } else {
                    if (game.judge.isStagePassed()) {
                        t2.reset();
                        allSoundsOff();
                        state = STAGE_PASSED_STATE;
                        game.pacman.animationStopped = true;
                        for (Ghost g : game.ghosts) g.animationStopped = true;
                    } else {
                        if (deadGhost != null) {
                            gc.setFont(F3);
                            gc.setFill(Color.LIGHTSKYBLUE);
                            gc.fillText(ghostEatingScoreIncrementStr, x2, y2);
                            for (Ghost g : game.ghosts) {
                                if (g == deadGhost) continue;
                                if (g.getState() == Ghost.DEAD)
                                    g.update();
                                g.draw(gc);
                            }
                            if (t3.passed(700)) {
                                deadGhost.dead();
                                deadGhost = null;
                                game.judge.resumeTimers();
                                game.player.increaseScore(ghostEatingScoreIncrement);
                                scoreStr = "SCORE: " + game.player.getScore();
                                ghostEatingScoreIncrement <<= 1;
                                POWER_UP_SIREN.stop();
                                DEAD_GHOSTS_SIREN.playIfNot(true);
                            }
                        } else {
                            game.judge.update();
                            game.field.update();
                            game.pacman.update();
                            for (Ghost g : game.ghosts) g.update();
                            frightGhostsIfNeed();
                            checkDeaths();
                            game.pacman.draw(gc);
                            for (Ghost g : game.ghosts) g.draw(gc);
                            updateMainSiren();
                            updatePowerUpSiren();
                            updateDeadGhostsSiren();
                        }
                    }
                }   break;
            case STAGE_PASSED_STATE:
                game.field.draw(gc);
                drawHud();
                game.pacman.draw(gc);
                if (t2.passed(1500)) {
                    game.field.setFlickering(true);
                    if (t2.passed(3500)) {
                        t1.reset();
                        state = PLAYING;
                        prePlaying = true;
                        game.field.reset();
                        game.pacman.reset();
                        game.field.setFlickering(false);
                        for (Ghost g : game.ghosts) g.reset();
                        game.notifyObservers(STAGE_STARTS);
                        game.judge.pauseTimers();
                    }
                }   break;
            case PLAYER_DEAD:
                game.field.draw(gc);
                drawHud();
                if (!t4.passed(1500)) {
                    game.pacman.draw(gc);
                    for (Ghost g : game.ghosts) g.draw(gc);
                } else {
                    EATING_PACMAN.playIfNot(false);
                    if (game.pacman.getState() != Pacman.DEAD) {
                        game.pacman.dead();
                        game.player.decreaseLifesCount();
                        livesRemainStr = "LIVES: " + game.player.getLifesCount();
                        game.pacman.animationStopped = false;
                    }
                    game.pacman.draw(gc);
                    if (t4.passed(3000)) {
                        if (game.player.getLifesCount() == 0) {
                            state = GAME_OVER;
                        } else {
                            Energizer.ANIMATE = false;
                            game.pacman.reset();
                            game.pacman.animationStopped = true;
                            t1.reset();
                            prePlaying = true;
                            state = PLAYING;
                            for (Ghost g : game.ghosts) {
                                g.reset();
                                g.animationStopped = true;
                            }
                        }
                    }
                }   break;
            default: //Game over state
                
                break;            
        }
    }
    
    private void checkDeaths() {
        for (Ghost g : game.ghosts) {
            DynamicTileActor a = getDeadActor(g, game.pacman);
            if (a != null) {
                game.judge.pauseTimers();
                if (a == g) {
                    ghostEatingScoreIncrementStr = ghostEatingScoreIncrement + "";
                    x2 = (int) g.pos.x;
                    y2 = (int) g.pos.y;
                    deadGhost = g;
                    t3.reset();
                    game.notifyObservers(GHOST_DIED);
                    EATING_GHOST.play(false);
                } else {
                    allSoundsOff();
                    state = PLAYER_DEAD;
                    t4.reset();
                    game.pacman.animationStopped = true;
                    game.notifyObservers(PACMAN_DIED);
                }
                break;
            }
        }
    }
    
    private DynamicTileActor getDeadActor(Ghost g, Pacman p) {
        if (g.getState() != Ghost.DEAD && p.getState() != Pacman.DEAD) {
            if (doTheSpritesOverlap(g, p)) {
                if ((g.getState() & Ghost.FRIGHTENED) != 0) return g;
                else return p;
            }
        }
        return null;
    }

    private boolean doTheSpritesOverlap(Ghost g, Pacman p) {
        double gx = g.pos.x;
        double gy = g.pos.y;
        double px = p.pos.x;
        double py = p.pos.y;
        double x = gx - px;
        double y = gy - py;
        return (x * x + y * y) < (DRAWING_TILE_SIZE * DRAWING_TILE_SIZE) / 2;
    }
    
    private void frightGhostsIfNeed() {
        if (needFrightGhosts) {
            game.pacman.powerUp();
            for (Ghost g : game.ghosts) g.fright();
            needFrightGhosts = false;
        }
    }

    private void newGame() {
        try {
            Class<? extends GameContainer> clazz = GAME_CONTAINERS_CLASSES[selectedPage][selectedContainer];
            Method create = clazz.getMethod("create");
            game = (GameContainer) create.invoke(null);
            game.addObservers(game.field, game.judge, this);
            GameField gf = game.field;
            int w = gf.xBoundReal;
            int h = gf.yBoundReal;
            App.resize(w, h);
            game.player = new Player(3);
            Energizer.ANIMATE = false;
            game.pacman.animationStopped = true;
            for (Ghost g : game.ghosts) g.animationStopped = true;
            state = PLAYING;
            prePlaying = true;
            scoreStr = "SCORE: 0";
            livesRemainStr = "LIVES: 3";
            t1.reset();
            game.judge.pauseTimers();
            game.notifyObservers(STAGE_STARTS);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.exit(0);
        }
    }
    
    @Override
    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        if (state == MENU) {
            if (code == KeyCode.UP && selectedContainer > 0) selectedContainer--;
            else if (code == KeyCode.DOWN && selectedContainer < GAME_CONTAINERS[selectedPage].length - 1) selectedContainer++;
            else if (code == KeyCode.LEFT && selectedPage > 0) selectedPage--;
            else if (code == KeyCode.RIGHT && selectedPage < GAME_CONTAINERS.length - 1) selectedPage++;
            else if (code == KeyCode.ENTER) newGame();
        } else if (state == PLAYING) {
            Pacman p = game.pacman;
            if (code == KeyCode.UP) p.setDesiredDirection(Direction.TOP);
            else if (code == KeyCode.DOWN) p.setDesiredDirection(Direction.BOTTOM);
            else if (code == KeyCode.LEFT) p.setDesiredDirection(Direction.LEFT);
            else if (code == KeyCode.RIGHT) p.setDesiredDirection(Direction.RIGHT);
        }
    }
    
    @Override
    public void notify(int event) {
        switch (event) {
            case PACMAN_ATE_THE_DOT:
                EATING_DOTS.playIfNot(true);
                game.player.increaseScore(DOT_EATING_SCORE_INCREMENT);
                scoreStr = "SCORE: " + game.player.getScore();
                break;
            case PACMAN_ATE_THE_ENERGIZER:
                needFrightGhosts = true;
                stopMainSiren();
                if (!game.atLeastOneGhostIsDead()) POWER_UP_SIREN.playIfNot(true);
                game.player.increaseScore(ENERGIZER_EATING_SCORE_INCREMENT);
                scoreStr = "SCORE: " + game.player.getScore();
                ghostEatingScoreIncrement = 200;
                break;
            case PACMAN_LEAVED_DOTS_LINE:
                EATING_DOTS.fadeOut();
                break;
            case PACMAN_ATE_THE_BONUS:
                Bonus b = game.field.getLastEatedBonus();
                BonusIncrementStr bes = new BonusIncrementStr();
                String str = BONUS_EATING_SCORE_INCREMENT[b.type] + "";
                bes.str = str;
                Text text = new Text(str);
                text.setFont(F3);
                Bounds bounds = text.getBoundsInLocal();
                bes.x = (float) (b.x - bounds.getMaxX() / 2);
                bes.y = (float) (b.y - bounds.getMaxY() / 2);
                EATING_BONUS.play(false);
                q.add(bes);
                break;
            case EXTRA_LIFE:
                game.player.extraLife();
                EXTRA_LIFE_SOUND.play(false);
                livesRemainStr = "LIVES: " + game.player.getLifesCount();
                break;
        }
    }
    
    private void allSoundsOff() {
        for (Sound s : ALL) s.stop();
    }
    
    private int getMainSirenNeedToPlayIdx() {
        GameField gf = game.field;
        double d = (double) gf.getDotsEated() / gf.dotsCount;
        if (d < 0.2) return 0;
        else if (d < 0.4) return 1;
        else if (d < 0.6) return 2;
        else if (d < 0.8) return 3;
        else return 4;
    }
    
    private int getPlayingMainSiren() {
        for (int i = 0; i <= 4; i++) 
            if (ALL[i].isPlaying()) return i;
        return -1;
    }
    
    private void stopMainSiren() {
        int idx = getPlayingMainSiren();
        if (idx != -1) ALL[idx].stop();
    }
    
    private void updateMainSiren() {
        int playing = getPlayingMainSiren();
        if (playing != -1) {
            int needToPlay = getMainSirenNeedToPlayIdx();
            if (needToPlay != playing) {
                ALL[playing].stop();
                ALL[needToPlay].play(true);
            }
        }
    }
    
    private void updatePowerUpSiren() {
        if (POWER_UP_SIREN.isPlaying() && !game.atLeastOneGhostIsFrightened()) {
            POWER_UP_SIREN.stop();
            ALL[getMainSirenNeedToPlayIdx()].play(true);
        }
    }
    
    private void updateDeadGhostsSiren() {
        if (DEAD_GHOSTS_SIREN.isPlaying() && !game.atLeastOneGhostIsDead()) {
            DEAD_GHOSTS_SIREN.stop();
            if (game.atLeastOneGhostIsFrightened()) POWER_UP_SIREN.play(true);
            else ALL[getMainSirenNeedToPlayIdx()].play(true);
        }
    }
    
}
