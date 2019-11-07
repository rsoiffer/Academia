package beige_engine.engine;

import beige_engine.graphics.Camera;
import beige_engine.graphics.Camera.Camera2d;
import beige_engine.graphics.Window;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import beige_engine.util.math.Vec2d;

public abstract class Input {

    private static final List<ReactiveInputListener> R_LISTENERS = new ArrayList();

    public static final int MOUSE_IN = 0;
    public static final int KEY_IN = 1;
    public static final int MOUSE_BUTTON_IN = 2;
    public static final int MOUSE_WHEEL_IN = 3;

    static void init() {
        Window.window.setCursorPosCallback((window, xpos, ypos) -> {
            Vec2d nMouse = new Vec2d(xpos / Settings.WINDOW_WIDTH, 1 - ypos / Settings.WINDOW_HEIGHT);
            if (!R_LISTENERS.isEmpty()) {
                R_LISTENERS.forEach(ril -> ril.receiveGeneralInput(MOUSE_IN, nMouse, nMouse.sub(mouse), 0, false, false));
            }
            mouse = nMouse;
        });
        Window.window.setKeyCallback((window, key, scancode, action, mods) -> {
            if (key >= 0) {
                boolean nks = action != GLFW_RELEASE;
                if (!R_LISTENERS.isEmpty()) {
                    R_LISTENERS.forEach(ril -> ril.receiveGeneralInput(KEY_IN, null, null, key, nks, nks != keys.get(key)));
                }
                keys.set(key, action != GLFW_RELEASE);
            }
        });
        Window.window.setMouseButtonCallback((window, button, action, mods) -> {
            boolean nbs = action != GLFW_RELEASE;
            if (!R_LISTENERS.isEmpty()) {
                R_LISTENERS.forEach(ril -> ril.receiveGeneralInput(MOUSE_BUTTON_IN, mouse, null, button, nbs, nbs != buttons.get(button)));
            }
            buttons.set(button, action != GLFW_RELEASE);
        });
        Window.window.setScrollCallback((window, xoffset, yoffset) -> {
            Vec2d nwo = new Vec2d(xoffset, yoffset);
            if (!R_LISTENERS.isEmpty()) {
                R_LISTENERS.forEach(ril -> ril.receiveGeneralInput(MOUSE_WHEEL_IN, nwo, nwo.sub(vecMouseWheel), 0, false, false));
            }
            vecMouseWheel = nwo;
            mouseWheel = yoffset;
        });
    }
    
    public static void addListener(ReactiveInputListener ril){
        R_LISTENERS.add(ril);
    }

    static void nextFrame() {
        prevKeys = (BitSet) keys.clone();
        prevMouse = mouse;
        prevButtons = (BitSet) buttons.clone();
        mouseWheel = 0;
    }

    private static BitSet keys = new BitSet();
    private static BitSet prevKeys = new BitSet();

    private static Vec2d mouse = new Vec2d(0, 0);
    private static Vec2d prevMouse = new Vec2d(0, 0);

    private static BitSet buttons = new BitSet();
    private static BitSet prevButtons = new BitSet();

    private static double mouseWheel;
    private static Vec2d vecMouseWheel = new Vec2d(0, 0);

    public static boolean keyDown(int key) {
        return keys.get(key);
    }

    public static boolean keyJustPressed(int key) {
        return keys.get(key) && !prevKeys.get(key);
    }

    public static boolean keyJustReleased(int key) {
        return !keys.get(key) && prevKeys.get(key);
    }

    public static Vec2d mouse() {
        return mouse(Camera.camera2d);
    }

    public static Vec2d mouse(Camera2d camera) {
        return camera.toWorldCoords(mouse);
    }

    public static Vec2d mouseDelta() {
        return mouseDelta(Camera.camera2d);
    }

    public static Vec2d mouseDelta(Camera2d camera) {
        return camera.toWorldCoords(mouse.sub(prevMouse));
    }

    public static boolean mouseDown(int button) {
        return buttons.get(button);
    }

    public static boolean mouseJustPressed(int button) {
        return buttons.get(button) && !prevButtons.get(button);
    }

    public static boolean mouseJustReleased(int button) {
        return !buttons.get(button) && prevButtons.get(button);
    }

    public static double mouseWheel() {
        return mouseWheel;
    }

    public interface ReactiveInputListener {

        public void receiveGeneralInput(int kind, Vec2d mouse, Vec2d deltaMouse, int key, boolean pressed, boolean changed);
    }
}
