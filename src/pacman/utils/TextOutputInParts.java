package pacman.utils;

/**
 *
 * @author Texhnolyze
 */
public class TextOutputInParts {

    private String part = "";
    private final String text;
    
    public TextOutputInParts(String text) {
        this.text = text;
    }
    
    private int len = 0;
    
    public String getPart() {
        return part;
    }
    
    public String getSrc() {
        return text;
    }
    
    public boolean isFullyDisplayed() {
        return part.length() == text.length();
    }
    
    public void next() {
        if (!isFullyDisplayed()) {
            len++;
            part = text.substring(0, len);
        }
    }
    
    public void reset() {
        len = 0;
        part = "";
    }
    
    public static void main(String[] args) {
        TextOutputInParts t = new TextOutputInParts("ABCDEFG");
        for (int i = 0; i < 100; i++) {
            System.out.println(t.getPart());
            System.out.println(t.isFullyDisplayed());
            t.next();
        }
    }
    
}
