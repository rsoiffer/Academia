package beige_engine.engine;

import beige_engine.graphics.Color;

public class Settings {

    public static int WINDOW_WIDTH = 1600;
    public static int WINDOW_HEIGHT = 900;
    
    public static int MIN_WINDOW_WIDTH = 0;
    public static int MIN_WINDOW_HEIGHT = 0;
    
    public static int WINDOW_WIDTH_DIVISOR = 1;
    public static int WINDOW_HEIGHT_DIVISOR = 1;
    
    public static boolean CLOSE_ON_X = true;

    public static Color BACKGROUND_COLOR = Color.BLACK;
    public static int ANTI_ALIASING = 1; // Scale from 1 - 16
    public static boolean ENABLE_VSYNC = true;
    public static boolean RESIZEABLE_WINDOW = true;
    public static boolean SHOW_CURSOR = true;

    public static boolean SHOW_OPENGL_DEBUG_INFO = true;
    public static boolean MULTITHREADED_OPENGL = false;

    public static double MIN_FRAME_TIME = .001;
    public static double MAX_FRAME_TIME = .1;
    
    public static String TEXTURE_LOAD_FOLDER = "sprites/";
    public static String FONT_LOAD_FOLDER = "fonts/";
}
