package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class Vector2 {

    public float x, y;
    
    public Vector2() {}
    
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2(Vector2 v) {
        x = v.x;
        y = v.y;
    }
    
    public Vector2 set(Vector2 v) {
        return set(v.x, v.y);
    }
    
    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public float dist(Vector2 v) {
        float xx = x - v.x;
        float yy = y - v.y;
        return (float) Math.sqrt(xx * xx + yy * yy);
    }
    
    public Vector2 add(Vector2 v) {
        return add(v.x, v.y);
    }
    
    public Vector2 add(float x, float y) {
        return new Vector2(this.x + x, this.y + y);
    }
    
    public Vector2 addLocal(Vector2 v) {
        return addLocal(v.x, v.y);
    }
    
    public Vector2 addLocal(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }
    
    public Vector2 sub(Vector2 v) {
        return sub(v.x, v.y);
    }
    
    public Vector2 sub(float x, float y) {
        return add(-x, -y);
    }
    
    public Vector2 subLocal(Vector2 v) {
        return subLocal(v.x, v.y);
    } 
    
    public Vector2 subLocal(float x, float y) {
        return addLocal(-x, -y);
    }
    
    public Vector2 closureLocal(float xBound, float yBound) {
        x = (x + xBound) % xBound;
        y = (y + yBound) % yBound;
        return this;
    }
    
    public boolean equals(Vector2 v, float eps) {
        return Math.abs(x - v.x) < eps && Math.abs(y - v.y) < eps;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
    public static final Vector2[] TRANSLATION_VECTOR = {
        new Vector2(1F, 0F), 
        new Vector2(0F, 1F),
        new Vector2(-1F, 0F),
        new Vector2(0F, -1F)
    };
    
    public static void setToTranslationVector(Vector2 dest, Direction dir) {
        setToTranslationVector(1.0F, dest, dir);
    }
    
    public static void setToTranslationVector(float scale, Vector2 dest, Direction dir) {
        Vector2 v = TRANSLATION_VECTOR[dir.id];
        dest.x = v.x * scale;
        dest.y = v.y * scale;
    }
    
    public static Direction getDirection(Vector2 from, Vector2 to) {
        float xSub = from.x - to.x;
        float ySub = from.y - to.y;
        if (Math.abs(xSub) > 0.01F && Math.abs(ySub) > 0.01F)
            throw new RuntimeException();
        else if (Math.abs(xSub) > 0.01F) {
            if (xSub > 0) 
                return Direction.LEFT;
            else 
                return Direction.RIGHT;
        } else if (Math.abs(ySub) > 0.01F){
            if (ySub > 0)
                return Direction.TOP;
            else
                return Direction.BOTTOM;
        } else return Direction.NONE;
    }
    
}
