package beige_engine.behaviors;

import beige_engine.engine.Behavior;

import static beige_engine.engine.Core.dt;

public class LifetimeBehavior extends Behavior {

    public double lifetime = 0;

    @Override
    public void step() {
        lifetime -= dt();
        if (lifetime < 0) {
            getRoot().destroy();
        }
    }
}
