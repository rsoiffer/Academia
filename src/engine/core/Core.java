package engine.core;

import engine.graphics.Window;
import engine.graphics.opengl.Framebuffer;

public final class Core extends AbstractSystem {

    public static final Core ROOT = new Core();

    public static double dt() {
        return ROOT.dt;
    }

    public static void init() {
        Window.initGLFW();
        Input.init();
    }

    public static void run() {
        while (!ROOT.shouldClose && !(Settings.CLOSE_ON_X && Window.window.shouldClose())) {
            ROOT.step();
        }
        Window.cleanupGLFW();
        System.exit(0);
    }

    public static void stopGame() {
        ROOT.shouldClose = true;
    }

    private long prevTime;
    private double dt;
    private boolean shouldClose;

    private Core() {
    }

    @Override
    protected void onStep() {
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
    }
}
