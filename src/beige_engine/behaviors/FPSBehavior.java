package beige_engine.behaviors;

import beige_engine.engine.Behavior;
import beige_engine.graphics.Window;

import java.util.LinkedList;
import java.util.Queue;

import static beige_engine.engine.Core.dt;

public class FPSBehavior extends Behavior {

    private final Queue<Double> tList = new LinkedList();
    public double fps;
    private double timeElapsed;

    @Override
    public void step() {
        double t = System.nanoTime() / 1e9;
        tList.add(t);
        while (t - tList.peek() > 5) {
            tList.poll();
        }
        fps = tList.size() / 5;

        timeElapsed += dt();
        if (timeElapsed > .25) {
            timeElapsed -= .25;
            Window.window.setTitle("FPS: " + Math.round(fps));
        }
    }
}
