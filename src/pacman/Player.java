package pacman;

/**
 *
 * @author Texhnolyze
 */
public class Player {
    
    private int score;
    private int lifesCount;
    
    public Player(int lifesCount) {
        this.lifesCount = lifesCount;
    }
    
    public void extraLife() {
        lifesCount++;
    }
    
    public void increaseScore(int by) {
        score += by;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getLifesCount() {
        return lifesCount;
    }
    
    public void decreaseLifesCount() {
        lifesCount--;
    }
    
}
