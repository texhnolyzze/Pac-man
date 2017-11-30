package pacman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pacman.utils.Sound;

/**
 *
 * @author Texhnolyze
 */
public class App extends Application {

    public static final int TILE_SIZE           = 8;
    public static final int DRAWING_TILE_SIZE   = 16;
    
    public static final float DTS_DIV_TS = (float) DRAWING_TILE_SIZE / TILE_SIZE;
    
    public static final int CANVAS_INIT_WIDTH       = 400;
    public static final int CANVAS_INIT_HEIGHT      = 400;
    
    private static final int WINDOW_WIDTH_OFFSET    = 6;
    private static final int WINDOW_HEIGHT_OFFSET   = 25;
    
    private static String IMAGES_DIRECTORY;
    private static String SOUNDS_DIRECTORY;
    
    private static Font RETRO_FONT;
    
    static {
        try {
            IMAGES_DIRECTORY = new File("resources/img").getAbsolutePath();
            SOUNDS_DIRECTORY = new File("resources/sounds").getAbsolutePath();
            RETRO_FONT = Font.loadFont(new FileInputStream(new File("resources/font/8bit16.TTF").getAbsolutePath()), 1.0D);
        } catch (FileNotFoundException ex) {
            System.exit(0);
        }
        
    }
    
    private static Stage stage;
    private static Canvas canvas;
    
    public static Image getImage(String name) {
        return getImage("", name);
    }
    
    public static Image getImage(String subDirectory, String name) {
        String s = new File(IMAGES_DIRECTORY + "/" + subDirectory + "/" + name + ".png").toURI().toString();
        Image src = new Image(s);
        double w = src.getWidth() * DTS_DIV_TS;
        double h = src.getHeight() * DTS_DIV_TS;
        Image scaled = new Image(src.impl_getUrl(), w, h, false, false);
        return scaled;
    }
    
    public static Sound getSound(String name) {
        return getSound(name, 1.0D);
    }
    
    public static Sound getSound(String name, double vol) {
        return new Sound(SOUNDS_DIRECTORY + "/" + name + ".wav", vol);
    } 
    
    public static Font getRetroFontOf(double size) {
        return new Font(RETRO_FONT.getName(), size);
    }
    
    public static final int IN_THE_CENTER            = 1;
    public static final int IN_THE_LEFT_SIDE         = 2;
    public static final int IN_THE_RIGHT_SIDE        = 3;
    
    public static int centerText(String str, Font f, int type) {
        return centerText(str, f, type, W);
    }
    
    public static int centerText(String str, Font f, int type, int w) {
        Text text = new Text(str);
        text.setFont(f);
        Bounds b = text.getBoundsInLocal();
        int x_offset = (int) (b.getMaxX() / 2);
        int base = (int) (w / (type == IN_THE_CENTER ? 2 
                : type == IN_THE_LEFT_SIDE ? 4 : (4D / 3D)));
        return base - x_offset;
    }
    
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        App.stage = stage;
        Group root = new Group();
        stage.setWidth(CANVAS_INIT_WIDTH + WINDOW_WIDTH_OFFSET);
        stage.setHeight(CANVAS_INIT_HEIGHT + WINDOW_HEIGHT_OFFSET);
        stage.setResizable(false);
        stage.setTitle("Pacman");
        Canvas canvas = new Canvas(CANVAS_INIT_WIDTH, CANVAS_INIT_HEIGHT);
        App.canvas = canvas;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        GameController controller = new GameController(gc);
        root.setOnKeyPressed(controller);
        stage.setScene(new Scene(root, Color.BLACK));
        stage.show();
        root.requestFocus();
        controller.start();
    }
    
    public static int W = CANVAS_INIT_WIDTH, H = CANVAS_INIT_HEIGHT;
    
    public static void resize(int w, int h) {
        stage.setWidth(w + WINDOW_WIDTH_OFFSET);
        stage.setHeight(h + WINDOW_HEIGHT_OFFSET);
        canvas.setWidth(w);
        canvas.setHeight(h);
        W = w;
        H = h;
    }
    
}