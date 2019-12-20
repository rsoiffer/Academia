package beige_engine.graphics;

import beige_engine.core.Settings;
import beige_engine.graphics.opengl.GLState;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    public static Window window;
    private final long handle;

    public Window(boolean mainWindow) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if (Settings.RESIZEABLE_WINDOW) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        }
        if (Settings.ANTI_ALIASING > 1) {
            glfwWindowHint(GLFW_SAMPLES, Settings.ANTI_ALIASING);
        }

        handle = glfwCreateWindow(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT, "Hello World!", NULL, mainWindow ? NULL : window.handle);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        if (mainWindow) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);
                glfwGetWindowSize(handle, pWidth, pHeight);
                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                glfwSetWindowPos(handle, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
            }
        }

        setCursorEnabled(Settings.SHOW_CURSOR);
    }

    public static void initGLFW() {
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            GLFWErrorCallback.createThrow().set();
            Configuration.DEBUG.set(true);
        }

        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        window = new Window(true);
        window.createContext();
        if (Settings.ENABLE_VSYNC) {
            glfwSwapInterval(1);
        }

        glfwSetFramebufferSizeCallback(window.handle, (w, wd, ht) -> Window.window.resize(wd, ht));

        glfwShowWindow(window.handle);
    }

    public static void cleanupGLFW() {
        window.cleanup();
        glfwTerminate();
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            glfwSetErrorCallback(null).free();
        }
    }

    public void resizeWindow(int width, int height) {
        glfwSetWindowSize(window.handle, width, height);
    }

    private void resize(int width, int height) {
        Settings.WINDOW_WIDTH = width;
        Settings.WINDOW_HEIGHT = height;
        glViewport(0, 0, Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);

        boolean resizeAgain = false;

        if (Settings.MIN_WINDOW_WIDTH > Settings.WINDOW_WIDTH || Settings.MIN_WINDOW_HEIGHT > Settings.WINDOW_HEIGHT) {
            Settings.WINDOW_WIDTH = Math.max(width, Settings.MIN_WINDOW_WIDTH);
            Settings.WINDOW_HEIGHT = Math.max(height, Settings.MIN_WINDOW_HEIGHT);
            resizeAgain = true;
        }

        if (Settings.WINDOW_WIDTH % Settings.WINDOW_WIDTH_DIVISOR != 0 || Settings.WINDOW_HEIGHT % Settings.WINDOW_HEIGHT_DIVISOR != 0) {
            Settings.WINDOW_WIDTH += Settings.WINDOW_WIDTH_DIVISOR - (Settings.WINDOW_WIDTH % Settings.WINDOW_WIDTH_DIVISOR);
            Settings.WINDOW_HEIGHT += Settings.WINDOW_HEIGHT_DIVISOR - (Settings.WINDOW_HEIGHT % Settings.WINDOW_HEIGHT_DIVISOR);
            resizeAgain = true;
        }

        if (resizeAgain) {
            window.resizeWindow(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
        }
    }

    private void cleanup() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
    }

    public void createContext() {
        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwSwapInterval(0);

        GLState.enable(GL_BLEND); // GL_DEPTH_TEST,
        GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            GLUtil.setupDebugMessageCallback();
        }
    }

    public void nextFrame() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    public void setCursorEnabled(boolean enabled) {
        if (enabled) {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
    }

    public void setCursorPosCallback(GLFWCursorPosCallbackI cursorPosCallback) {
        glfwSetCursorPosCallback(handle, cursorPosCallback);
    }

    public void setKeyCallback(GLFWKeyCallbackI keyCallback) {
        glfwSetKeyCallback(handle, keyCallback);
    }

    public void setMouseButtonCallback(GLFWMouseButtonCallbackI mouseButtonCallback) {
        glfwSetMouseButtonCallback(handle, mouseButtonCallback);
    }

    public void setScrollCallback(GLFWScrollCallbackI scrollCallback) {
        glfwSetScrollCallback(handle, scrollCallback);
    }

    public void setTitle(String s) {
        glfwSetWindowTitle(handle, s);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }
}
