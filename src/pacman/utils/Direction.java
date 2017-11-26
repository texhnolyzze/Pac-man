package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public enum Direction {
    
    NONE(-1), RIGHT(0), BOTTOM(1), LEFT(2), TOP(3);
    
    public final int id;
    
    private Direction(int id) {
        this.id = id;
    }
    
    private static final Direction[] OPPOSITE = {
        LEFT, TOP, RIGHT, BOTTOM
    };
    
    public Direction getOpposite() {
        return OPPOSITE[id];
    }
    
    private static final Direction[][] PERPENDICULAR = {
        {BOTTOM, TOP},
        {RIGHT, LEFT},
        {BOTTOM, TOP},
        {RIGHT, LEFT}
    };
    
    public Direction[] getPerpendicular() {
        return PERPENDICULAR[id];
    }
    
    public boolean isVertical() {
        return this == TOP || this == BOTTOM;
    }
    
    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }
    
    public static final IntVec2[] TRANSLATION_VECTOR = {
        new IntVec2(1, 0), 
        new IntVec2(0, 1),
        new IntVec2(-1, 0),
        new IntVec2(0, -1)
    };
    
    private final IntVec2 temp = new IntVec2();
    
    public IntVec2 getTranslationVector(int scale) {
        IntVec2 v = TRANSLATION_VECTOR[id];
        temp.x = v.x * scale;
        temp.y = v.y * scale;
        return temp;
    }
    
}
