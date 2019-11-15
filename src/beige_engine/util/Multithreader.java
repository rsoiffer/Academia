package beige_engine.util;

import beige_engine.graphics.Window;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static beige_engine.engine.Settings.MULTITHREADED_OPENGL;

public abstract class Multithreader {

    private static final int NUM_THREADS = 3;
    private static final int TIMEOUT = 60;
    private static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue(), r -> {
        if (MULTITHREADED_OPENGL) {
            Window w = new Window(false);
            Thread t = new Thread(() -> {
                w.createContext();
                r.run();
            });
            return t;
        } else {
            return new Thread(r);
        }
    });

    static {
        THREAD_POOL.allowCoreThreadTimeOut(true);
    }

    public static boolean isFree() {
        return queuedTasks() == 0;
    }

    public static void run(Runnable r) {
        THREAD_POOL.execute(r);
    }

    public static int queuedTasks() {
        return THREAD_POOL.getQueue().size();
    }
}
