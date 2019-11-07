package beige_engine.engine;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public final class Layer implements Comparable<Layer> {

    static final Set<Layer> ALL_LAYERS = new TreeSet();

    public static final Layer PREUPDATE = new Layer(-10);
    public static final Layer UPDATE = new Layer(0);
    public static final Layer POSTUPDATE = new Layer(10);
    public static final Layer RENDER3D = new Layer(20);
    public static final Layer RENDER2D = new Layer(30);
    public static final Layer POSTRENDER = new Layer(40);

    public Collection<Behavior> behaviors = new HashSet();
    public final double order;

    public Layer(double order) {
        this.order = order;
        ALL_LAYERS.add(this);
    }

    @Override
    public int compareTo(Layer o) {
        return Double.compare(order, o.order);
    }

    public Behavior onStep(Runnable toRun) {
        Layer thisLayer = this;
        return new Behavior() {
            @Override
            public Layer layer() {
                return thisLayer;
            }

            @Override
            public void step() {
                toRun.run();
            }
        }.create();
    }

    public void stepAll() {
        new LinkedList<>(behaviors).forEach(Behavior::step);
    }
}
