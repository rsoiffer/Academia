package beige_engine.engine;

import beige_engine.graphics.Window;
import beige_engine.graphics.opengl.Framebuffer;

import java.util.Collection;
import java.util.LinkedList;

import static beige_engine.engine.Layer.ALL_LAYERS;

public abstract class Core {

    private static final Collection<Runnable> TO_RUN = new LinkedList();
    public static Thread MAIN_THREAD;
    private static long prevTime;
    private static double dt;
    private static boolean shouldClose;

    private static Collection<Runnable> clearToRun() {
        synchronized (TO_RUN) {
            Collection<Runnable> r = new LinkedList<>(TO_RUN);
            TO_RUN.clear();
            return r;
        }
    }

    public static double dt() {
        return dt;
    }

    public static void init() {
        MAIN_THREAD = Thread.currentThread();
        Window.initGLFW();
        Input.init();
    }

    public static void onMainThread(Runnable toRun) {
        if (toRun == null) {
            throw new RuntimeException("toRun cannot be null");
        }
        if (Thread.currentThread() != MAIN_THREAD) {
            synchronized (TO_RUN) {
                TO_RUN.add(toRun);
            }
        } else {
            toRun.run();
        }
    }

    public static void run() {
        while (!shouldClose && !(Settings.CLOSE_ON_X && Window.window.shouldClose())) {
            Input.nextFrame();
            Window.window.nextFrame();
            Framebuffer.clearWindow(Settings.BACKGROUND_COLOR);

            long time = System.nanoTime();
            dt = Math.min((time - prevTime) / 1e9, Settings.MAX_FRAME_TIME);
            while (dt < Settings.MIN_FRAME_TIME) {
                try {
                    Thread.sleep(0, 100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                time = System.nanoTime();
                dt = Math.min((time - prevTime) / 1e9, Settings.MAX_FRAME_TIME);
            }
            prevTime = time;

            clearToRun().forEach(r -> r.run());
            new LinkedList<>(ALL_LAYERS).forEach(Layer::stepAll);
        }
        Window.cleanupGLFW();
        System.exit(0);
    }

    public static void stopGame() {
        shouldClose = true;
    }
}
