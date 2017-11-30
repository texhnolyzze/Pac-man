package pacman.actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import static pacman.App.DRAWING_TILE_SIZE;
import pacman.GameContainer;
import pacman.GameObserver;
import pacman.actors.dynamic_tile_actors.DynamicTileActor;
import pacman.actors.dynamic_tile_actors.Ghost;
import pacman.actors.dynamic_tile_actors.Pacman;
import pacman.actors.eatable_tile_actors.Bonus;
import pacman.actors.eatable_tile_actors.Dot;
import pacman.actors.eatable_tile_actors.EatableTileActor;
import pacman.actors.eatable_tile_actors.Energizer;
import pacman.utils.Direction;
import pacman.utils.IntVec2;
import pacman.utils.Stack;
import pacman.utils.TickTimer;
import pacman.utils.Vector2;

public abstract class GameField implements Actor, GameObserver {

    public static final int EMPTY               = 0;
    public static final int DOT                 = 1;
    public static final int ENERGIZER           = 2;
    public static final int WALL                = 3;
    public static final int GHOSTS_PEN_GATES    = 4;
    
    public final int xBoundTile, yBoundTile;
    public final int xBoundPix, yBoundPix;

    protected GameContainer game;
    
    private final int[][] maze;
    private final Image[] mazeViews;
    
    protected final Map<IntVec2, EatableTileActor> m;
    protected final Map<IntVec2, Vector2> nodes; //impasses are also the nodes.
    
    public final int dotsCount, energizersCount;
    protected int dotsEated, energizersEated;
    
    protected boolean flickering;
    
    public PathFinder pathFinder;
    
    protected final IntVec2 temp = new IntVec2();
    
    public GameField(
            GameContainer game,
            int[][] maze, 
            Image[] mazeViews,
            Map<IntVec2, Vector2> nodes) 
    {
        this.game = game;
        this.maze = maze;
        this.mazeViews = mazeViews;
        this.nodes = nodes;
        this.m = new HashMap<>();
        yBoundTile = maze.length;
        xBoundTile = maze[0].length;
        yBoundPix = yBoundTile * DRAWING_TILE_SIZE;
        xBoundPix = xBoundTile * DRAWING_TILE_SIZE;
        int temp1 = 0, temp2 = 0;
        for (int y = 0; y < yBoundTile; y++) {
            for (int x = 0; x < xBoundTile; x++) {
                int code = maze[y][x];
                if (code == DOT) {
                    temp1++;
                    IntVec2 v = new IntVec2(x, y);
                    m.put(v, new Dot(x, y));
                } else if (code == ENERGIZER) {
                    temp2++;
                    IntVec2 v = new IntVec2(x, y);
                    m.put(v, new Energizer(x, y));
                }
            }
        }
        dotsCount = temp1;
        energizersCount = temp2;
        pathFinder = new PathFinder();
    }
    
    public void reset() {
        for (EatableTileActor a : m.values()) a.reset();
        dotsEated = 0;
        energizersEated = 0;
    }

    public int getDotsEated() {
        return dotsEated;
    }

    public int getEnergizersEated() {
        return energizersEated;
    }
    
    public void setFlickering(boolean flickering) {
        this.flickering = flickering;
    }
    
    public abstract float getCurrentVelocityFor(DynamicTileActor a);
    
    public abstract boolean canChangeDirTo(Direction desired, DynamicTileActor a);
    
    public abstract Pacman initPacman();
    
    public abstract Ghost[] initGhosts();
    
    public abstract Vector2[] getWaypointsInPen(Ghost g);
    
    public abstract Vector2[] getWaypointsForLeavingPen(Ghost g);
    
    public abstract IntVec2 getScatterStateTarget(Ghost g);
    
    public abstract Vector2 getReadyLabelPosition();
    
    public abstract Vector2 getLivesRemainLabelPosition();
    
    public abstract Vector2 getScoreLabelPosition();
    
    public int lookup(int x, int y) {
        return maze[y][x];
    }
    
    public Vector2 getNodeOn(int x, int y) {
        temp.x = x;
        temp.y = y;
        return nodes.get(temp);
    }
    
    protected abstract int getCurrentViewIndex();
    
    @Override
    public void draw(GraphicsContext gc) {
        int idx = getCurrentViewIndex();
        gc.drawImage(mazeViews[idx], 0, 0);
        for (EatableTileActor a : m.values())
            a.draw(gc);
    }
    
    @Override
    public void update() {
        if (pacmanOnDotsLine) {
            dotEatingTimer.tick();
            if (dotEatingTimer.passed(ticks)) {
                pacmanOnDotsLine = false;
                game.notifyObservers(PACMAN_LEAVED_DOTS_LINE);
            }
        }
    }

    protected Bonus lastEated;
    
    public Bonus getLastEatedBonus() {
        return lastEated;
    }
    
    private int ticks;
    protected abstract int getConstantTick();
    
    protected boolean pacmanOnDotsLine;
    private final TickTimer dotEatingTimer = new TickTimer();
    
    @Override
    public void notify(int event) {
        if (event == PACMAN_TILE_CHANGED) {
            int px = game.pacman.tile_x;
            int py = game.pacman.tile_y;
            temp.x = px;
            temp.y = py;
            EatableTileActor a = m.get(temp);
            if (a != null && !a.isEated()) {
                a.eat();
                int evnt;
                if (a.getClass() == Dot.class) {
                    dotsEated++;
                    this.ticks = (int) (DRAWING_TILE_SIZE / game.pacman.velocity) 
                            + getConstantTick();
                    dotEatingTimer.reset();
                    pacmanOnDotsLine = true;
                    evnt = PACMAN_ATE_THE_DOT;
                } else { //Energizer
                    energizersEated++;
                    evnt = PACMAN_ATE_THE_ENERGIZER;
                }
                game.notifyObservers(evnt);
            }
        }
    }
    
    public class PathFinder {
        
        private final IntVec2[][] all;
        
        {
            all = new IntVec2[yBoundTile][xBoundTile];
            for (int y = 0; y < yBoundTile; y++)
                for (int x = 0; x < xBoundTile; x++)
                    all[y][x] = new IntVec2(x, y);
        }
        
        public Stack<Vector2> getPath(int xFrom, int yFrom, int xTo, int yTo, DynamicTileActor a) {
            if (a.isObstacle(xTo, yTo))
                throw new RuntimeException();
            IntVec2 from = all[yFrom][xFrom];
            IntVec2 to = all[yTo][xTo];
            Queue<IntVec2> q = new LinkedList<>();
            HashSet<IntVec2> looked = new HashSet<>();
            HashMap<IntVec2, IntVec2> pathTo = new HashMap<>();
            q.add(from);
            looked.add(from);
            pathTo.put(from, from);
            while (!q.isEmpty()) {
                IntVec2 v = q.poll();
                if (v.equals(to)) break;
                for (IntVec2 adj : getAdjacentTo(v)) {
                    if (!a.isObstacle(adj.x, adj.y) && !looked.contains(adj)) {
                        q.add(adj);
                        looked.add(adj);
                        pathTo.put(adj, v);
                    }
                }
            }
            Stack<Vector2> nodes = new Stack<>();
            from = pathTo.get(to);
            while (from != to) {
                Vector2 node = getNodeOn(from.x, from.y);
                if (node != null) nodes.push(node);
                to = from;
                from = pathTo.get(to);
            }
            return nodes;
        }
        
        private final IntVec2[] temp = new IntVec2[4];
        
        private IntVec2[] getAdjacentTo(IntVec2 v) {
            int x = v.x, y = v.y;
            temp[0] = all[y][(x - 1 + xBoundTile) % xBoundTile];
            temp[1] = all[(y - 1 + yBoundTile) % yBoundTile][x];
            temp[2] = all[y][(x + 1 + xBoundTile) % xBoundTile];
            temp[3] = all[(y + 1 + yBoundTile) % yBoundTile][x];
            return temp;
        }
        
    }
    
}