package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class IntVec2 {
    
    public int x, y;
    
    public IntVec2() {}
    
    public IntVec2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void closure(int xBound, int yBound) {
        x = Math.abs(x + xBound) % xBound;
        y = Math.abs(y + yBound) % yBound;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final IntVec2 other = (IntVec2) obj;
        if (this.x != other.x) return false;
        return this.y == other.y;
    }
    
    public void translateIn(Direction d) {
        if (d.isHorizontal()) {
            if (d == Direction.LEFT) 
                x--;
            else 
                x++;
        } else {
            if (d == Direction.TOP)
                y--;
            else 
                y++;
        }
    }
    
    public static Direction getDirection(int xFrom, int yFrom, int xTo, int yTo) {
        int xSub = xFrom - xTo;
        if (xSub != 0) {
            if (xSub == 1) 
                return Direction.LEFT;
            else 
                return Direction.RIGHT;
        } else {
            int ySub = yFrom - yTo;
            if (ySub != 0) {
                if (ySub == 1)
                    return Direction.TOP;
                else
                    return Direction.BOTTOM;
            } else 
                return Direction.NONE;
        }
    }
    
    @Override
    public String toString() {
        return "(x=" + x + ", y=" + y + ')';
    }
    
}
