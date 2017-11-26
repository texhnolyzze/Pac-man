package pacman.actors.dynamic_tile_actors;

import static pacman.App.DRAWING_TILE_SIZE;
import static pacman.App.TILE_SIZE;
import pacman.GameContainer;
import pacman.actors.GameField;
import pacman.utils.Direction;
import static pacman.utils.Direction.BOTTOM;
import static pacman.utils.Direction.LEFT;
import static pacman.utils.Direction.NONE;
import static pacman.utils.Direction.RIGHT;
import static pacman.utils.Direction.TOP;
import pacman.utils.IntVec2;
import pacman.utils.Stack;
import pacman.utils.Vector2;

/**
 *
 * @author Texhnolyze
 */
public class Ghost extends DynamicTileActor {
    
    public static final int IN_PEN         = 1 << 1;
    public static final int LEAVING_PEN    = 1 << 2;
    public static final int SCATTER        = 1 << 3;
    public static final int CHASE          = 1 << 4;
    public static final int FRIGHTENED     = 1 << 5;
    
    //if ghost starts from in_pen state or leaving_pen state, 
    //his initial direction must coincide with 
    //Direction d = (Vector2.getDirection(pos, waypoints[0]))
    //if d is NONE this means that the ghost is already on the 
    //first waypoint and it will just go to the next waypoint
    public Ghost(int id, int type, float x, float y, Direction dir, int state, GameContainer game) {
        super(id, type, x, y, dir, state, game);
        if ((state & FRIGHTENED) != 0)
            throw new RuntimeException();
        enter(state);
    }

    @Override
    public void reset() {
        super.reset(); 
        enter(state);
    }
    
    private void enter(int state) {
        this.state = state;
        if ((this.state & IN_PEN) != 0) {
            waypointIdx = 0;
            waypoints = game.field.getWaypointsInPen(this);
            if (waypoints == null) 
                this.state = this.state - IN_PEN + LEAVING_PEN;
            else {
                Direction d = Vector2.getDirection(pos, waypoints[0]);
                if (d == NONE) {
                    waypointIdx = (waypointIdx + 1) % waypoints.length;
                    this.dir = Vector2.getDirection(pos, waypoints[1]);                
                } else dir = d;
            }
        }
        if ((this.state & LEAVING_PEN) != 0) {
            waypointIdx = 0;
            waypoints = game.field.getWaypointsForLeavingPen(this);
            Direction d = Vector2.getDirection(pos, waypoints[0]);
            if (d == NONE) {
                waypointIdx++;
                this.dir = Vector2.getDirection(pos, waypoints[1]);                
            } else dir = d;
        }
    }

    @Override
    public boolean isObstacle(int tile_x, int tile_y) {
        if (super.isObstacle(tile_x, tile_y))
            return true;
        return game.field.lookup(tile_x, tile_y) == GameField.GHOSTS_PEN_GATES 
                && (state != DEAD && (state & LEAVING_PEN) == 0);
    }

    public void fright() {
        if ((state & FRIGHTENED) == 0) {
            if (state != DEAD) 
                state = state == CHASE || state == SCATTER ? FRIGHTENED : state + FRIGHTENED;
            if ((state & IN_PEN) == 0 && (state & LEAVING_PEN) == 0) 
                turnAround();
        }
    }
    
    private boolean usingWaypoints;
    private Stack<Vector2> nodes;
    private final Vector2 deadStateTarget = new Vector2();
    
    @Override
    public void dead() {
        super.dead(); 
        tile_x = (int) (pos.x / DRAWING_TILE_SIZE); //if ghost is in pen
        tile_y = (int) (pos.y / DRAWING_TILE_SIZE); //or leaving pen state
        //The last point in this array is the target of the ghost
        waypoints = game.field.getWaypointsForLeavingPen(this);
        waypointIdx = waypoints.length - 1;
        deadStateTarget.set(waypoints[waypointIdx]);
        int dest_x = (int) (deadStateTarget.x / DRAWING_TILE_SIZE);
        int dest_y = (int) (deadStateTarget.y / DRAWING_TILE_SIZE);
        nodes = game.field.pathFinder.getPath(tile_x, tile_y, dest_x, dest_y, this);
        if (nodes.isEmpty()) { //ghost died at the exit from pen
            usingWaypoints = true;
            pos.set(deadStateTarget);
            dir = Vector2.getDirection(pos, waypoints[--waypointIdx]);
        } else {
            Vector2 node = game.field.getNodeOn(tile_x, tile_y);
            if (node != null) { //ghost died in node
                nodes.pop();
                pos.set(node);
            } 
            if (!nodes.isEmpty()) {
                Vector2 n = nodes.top();
                Direction d = Vector2.getDirection(pos, n);
                tile_temp1.x = tile_x;
                tile_temp1.y = tile_y;
                int next_x = (int) (n.x / DRAWING_TILE_SIZE);
                int next_y = (int) (n.y / DRAWING_TILE_SIZE);
                while (tile_temp1.x != next_x && tile_temp1.y != next_y) {
                    tile_temp1.translateIn(d);
                    //this is the condition for cases where the path 
                    //to the pen runs through the boundaries of the game field
                    if (isObstacle(tile_temp1.x, tile_temp1.y)) { 
                        d = d.getOpposite();
                        break;
                    }
                }
                dir = d;
            } else dir = Vector2.getDirection(pos, deadStateTarget);
        }
    }

    @Override
    protected int getCurrentAnimationIdx() {
        if (state == DEAD) return -1;
        else if ((state & FRIGHTENED) != 0) {
            if (game.judge.getPassedPowerUpTime() < 0.8F) 
                return 4;
            else 
                return 5;
        } else 
            return dir.id;
    }

    @Override
    protected int getCurrentImageIdx() {
        if (state == DEAD) return dir.id;
        return -1;
    }

    private int waypointIdx;
    private Vector2[] waypoints;
    
    @Override
    public void update() {
        velocity = game.field.getCurrentVelocityFor(this);
        //No maze logic in_pen or in leaving_pen.
        if ((state & IN_PEN) != 0) {
            updateInInPenState();
        } else if ((state & LEAVING_PEN) != 0) {
            updateInLeavingPenState();
        } else if (state == FRIGHTENED) {
            updateInFrightenedState();
        } else if (state == DEAD) {
            updateInDeadState();
        } else { //ghost chasing or scattering.
            updateInChaseOrScatterState();
        }
    }
    
    private void stopFrightIfNeed() {
        if (game.judge.powerUpEnd()) {
            state -= FRIGHTENED;
            if (state == 0) 
                state = game.judge.timeToChase(this) ? CHASE : SCATTER;
        }
    }
    
    private void updateInInPenState() {
        Vector2.setToTranslationVector(velocity, vTemp, dir);
        pos.addLocal(vTemp);
        if (pos.equals(waypoints[waypointIdx], velocity)) {
            pos.x = waypoints[waypointIdx].x;
            pos.y = waypoints[waypointIdx].y;
            waypointIdx = (waypointIdx + 1) % waypoints.length;
            Vector2 next = waypoints[waypointIdx];
            dir = Vector2.getDirection(pos, next);
            if (game.judge.timeToLeavePen(this)) {
                enter(state - IN_PEN + LEAVING_PEN);
            }
        }
        if ((state & FRIGHTENED) != 0)
            stopFrightIfNeed();
    }
    
    private void updateInLeavingPenState() {
        Vector2.setToTranslationVector(velocity, vTemp, dir);            
        pos.addLocal(vTemp);
        if (pos.equals(waypoints[waypointIdx], velocity)) {
            pos.x = waypoints[waypointIdx].x;
            pos.y = waypoints[waypointIdx].y;
            waypointIdx++;
            if (waypointIdx == waypoints.length) {
                state = state - LEAVING_PEN;
                tile_x = (int) (pos.x / DRAWING_TILE_SIZE);
                tile_y = (int) (pos.y / DRAWING_TILE_SIZE);
                //There must always be at least one direction 
                //from the pen of ghosts
                if (state == 0) {//means that ghost is not frightened
                    state = game.judge.timeToChase(this) ? CHASE : SCATTER;
                    updatePossibleDirections(tile_x, tile_y);
                    IntVec2 target = getCurrentTarget();
                    dir = getBestDirection(possible, tile_x, tile_y, target.x, target.y);
                } else {
                    updatePossibleDirections(tile_x, tile_y);
                    dir = getRandomDirection(possible);
                }
            } else {
                Vector2 next = waypoints[waypointIdx];
                dir = Vector2.getDirection(pos, next);
            }
        }
        if ((state & FRIGHTENED) != 0)
            stopFrightIfNeed();
    }
    
    private void updateInFrightenedState() {
        move();
        if (inTheNode) {
            updatePossibleDirections(tile_x, tile_y);
            changeDirTo(getRandomDirection(possible));
        }
        stopFrightIfNeed();
    }
    
    private void updateInDeadState() {
        if (!usingWaypoints) { 
            move();
            if (pos.equals(deadStateTarget, velocity)) {
                usingWaypoints = true;
                pos.set(deadStateTarget);
                dir = Vector2.getDirection(pos, waypoints[--waypointIdx]);
            } else {
                if (inTheNode) {
                    if (!nodes.isEmpty()) pos.set(nodes.pop());
                    if (!nodes.isEmpty()) {
                        Vector2 next = nodes.top();
                        Direction d = Vector2.getDirection(pos, next);
                        if (d != dir) changeDirTo(d);
                    } else dir = Vector2.getDirection(pos, deadStateTarget);
                }
            }
        } else {
            Vector2.setToTranslationVector(velocity, vTemp, dir);            
            pos.addLocal(vTemp);
            if (pos.equals(waypoints[waypointIdx], velocity)) {
                pos.set(waypoints[waypointIdx--]);
                if (waypointIdx == -1) {
                    usingWaypoints = false;
                    enter(IN_PEN);
                } else {
                    Vector2 next = waypoints[waypointIdx];
                    dir = Vector2.getDirection(pos, next);
                }
            }
        }
    }
    
    private void updateInChaseOrScatterState() {
        move();
        if (inTheNode) {
            IntVec2 target = getCurrentTarget();
            updatePossibleDirections(tile_x, tile_y);
            changeDirTo(getBestDirection(possible, tile_x, tile_y, target.x, target.y));
        }
        boolean b = state == CHASE ? game.judge.timeToScatter(this) : game.judge.timeToChase(this);
        if (b) {
            state = state == CHASE ? SCATTER : CHASE;
            turnAround();
        }
    }
    
    private void turnAround() {
        //if the ghost at the end of the impasse - it does not turn
        if (canChangeDirTo(dir.getOpposite())) 
            changeDirTo(dir.getOpposite());
        else {
            Direction[] perp = dir.getPerpendicular();
            if (canChangeDirTo(perp[0]))
                changeDirTo(perp[0]);
            else if (canChangeDirTo(perp[1]))
                changeDirTo(perp[1]);
        }
    }
    
    private final Direction[] possible = new Direction[4];
    
    private Direction[] updatePossibleDirections(int tile_x, int tile_y) {
        possible[0] = RIGHT;
        possible[1] = BOTTOM;
        possible[2] = LEFT;
        possible[3] = TOP;
        possible[dir.getOpposite().id] = NONE;
        int noneNum = 1;
        int x_cpy = this.tile_x;
        int y_cpy = this.tile_y;
        this.tile_x = tile_x;
        this.tile_y = tile_y;
        for (Direction d : Direction.values()) {
            if (d == NONE) continue;
            setToNextCoords(d, tile_temp2);
            boolean b1 = isObstacle(tile_temp2.x, tile_temp2.y);
            boolean b2 = !game.field.canChangeDirTo(d, this);
            if (b1 || b2) {
                noneNum++;
                possible[d.id] = NONE;
            }
        }
        if (noneNum == 4) //ghost is in impass.
            possible[dir.getOpposite().id] = dir.getOpposite();
        this.tile_x = x_cpy;
        this.tile_y = y_cpy;
        return possible;
    }
    
    private Direction getBestDirection(Direction[] all, int from_x, int from_y, int target_x, int target_y) {
        int x_cpy = tile_x, y_cpy = tile_y;
        tile_x = from_x;
        tile_y = from_y;
        int bestDistSqr = Integer.MAX_VALUE;
        Direction bestDir = NONE;
        for (Direction d : all) {
            if (d == NONE) continue;
            setToNextCoords(d, tile_temp2);
            int xSub = target_x - tile_temp2.x;
            int ySub = target_y - tile_temp2.y;
            int distSqr = xSub * xSub + ySub * ySub;
            if (distSqr < bestDistSqr) {
                bestDir = d;
                bestDistSqr = distSqr;
            }
        }
        tile_x = x_cpy;
        tile_y = y_cpy;
        return bestDir;
    }
    
    private IntVec2 getCurrentTarget() {
        switch (state) {
            case CHASE:
                tile_temp3.x = game.pacman.tile_x;
                tile_temp3.y = game.pacman.tile_y;
                return specify(tile_temp3);
            case SCATTER:
                return game.field.getScatterStateTarget(this);
            default:
                return null;
        }
    }
    
    private IntVec2 specify(IntVec2 target) {
        if (type == BLINKY) return target;
        else if (type == PINKY) {
            Direction pacmanDir = game.pacman.dir;
            IntVec2 v = pacmanDir.getTranslationVector(4);
            target.x += v.x;
            target.y += v.y;
            target.closure(game.field.xBoundTile, game.field.yBoundTile);
            return target;
        } else if (type == CLYDE) {
            int xSub = target.x - tile_x;
            int ySub = target.y - tile_y;
            int distSqr = xSub * xSub + ySub * ySub;
            if (distSqr > TILE_SIZE * TILE_SIZE) return target;
            else return game.field.getScatterStateTarget(this);
        } else {
            Direction pacmanDir = game.pacman.dir;
            IntVec2 v = pacmanDir.getTranslationVector(2);
            target.x += v.x;
            target.y += v.y;
            for (Ghost g : game.ghosts) {
                if (g.type == BLINKY) {
                    int xSub = 2 * (target.x - g.tile_x);
                    int ySub = 2 * (target.y - g.tile_y);
                    target.x += xSub;
                    target.y += ySub;
                    target.closure(game.field.xBoundTile, game.field.yBoundTile);
                    return target;
                }
            }
        }
        return null;
    }
    
    private static Direction getRandomDirection(Direction[] all) {
        for (Direction d : all)
            //this logic makes ghost behavior 
            //predictable when it is frightened
            if (d != NONE)
                return d;
        return NONE;
    }
    
}
