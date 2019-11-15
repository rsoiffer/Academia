package beige_engine.engine;

import java.util.*;

import static beige_engine.engine.Core.MAIN_THREAD;
import static beige_engine.engine.Core.onMainThread;
import static beige_engine.engine.Layer.UPDATE;

public abstract class Behavior {

    private static final Map<Class<? extends Behavior>, Collection<Behavior>> TRACKED_BEHAVIORS = new HashMap();

    private static Behavior currentRoot;
    private final Behavior root;
    private final Map<Class<? extends Behavior>, Behavior> subBehaviors;

    public Behavior() {
        if (currentRoot == null) {
            // This is a root behavior
            root = this;
            subBehaviors = new HashMap();
            subBehaviors.put(getClass(), this);
        } else {
            // This is not a root behavior
            root = currentRoot;
            subBehaviors = null;
            if (root.subBehaviors.put(getClass(), this) != null) {
                throw new RuntimeException("A behavior can only have one subbehavior of each type");
            }
        }
    }

    public static <T extends Behavior> Collection<T> track(Class<T> c) {
        if (!TRACKED_BEHAVIORS.containsKey(c)) {
            TRACKED_BEHAVIORS.put(c, new HashSet());
        }
        return (Collection) TRACKED_BEHAVIORS.get(c);
    }

    // Utility functions
    public final Behavior create() {
        if (Thread.currentThread() != MAIN_THREAD) {
            onMainThread(() -> create());
            return this;
        }
        if (!isRoot()) {
            throw new RuntimeException("Can only create root beige_engine.behaviors");
        }
        for (Behavior b : subBehaviors.values()) {
            b.createActual();
        }
        return this;
    }

    private void createActual() {
        if (TRACKED_BEHAVIORS.containsKey(getClass())) {
            TRACKED_BEHAVIORS.get(getClass()).add(this);
        }
        layer().behaviors.add(this);
        createInner();
    }

    public final void destroy() {
        if (Thread.currentThread() != MAIN_THREAD) {
            onMainThread(() -> destroy());
            return;
        }
        if (!isRoot()) {
            throw new RuntimeException("Can only destroy root beige_engine.behaviors");
        }
        for (Behavior b : subBehaviors.values()) {
            b.destroyActual();
        }
    }

    private void destroyActual() {
        if (TRACKED_BEHAVIORS.containsKey(getClass())) {
            TRACKED_BEHAVIORS.get(getClass()).remove(this);
        }
        layer().behaviors.remove(this);
        destroyInner();
    }

    public final <T extends Behavior> T get(Class<T> c) {
        T t = getOrNull(c);
        if (t != null) {
            return t;
        } else {
            throw new RuntimeException("Behavior not found: " + c.getSimpleName());
        }
    }

    public final <T extends Behavior> T getOrNull(Class<T> c) {
        return (T) root.subBehaviors.get(c);
    }

    public final Behavior getRoot() {
        return root;
    }

    public final Set<Class<? extends Behavior>> getSubBehaviors() {
        if (!isRoot()) {
            throw new RuntimeException("Can only get subbehaviors of root beige_engine.behaviors");
        }
        return subBehaviors.keySet();
    }

    public final boolean hasSubBehavior(Class<? extends Behavior> c) {
        return getOrNull(c) != null;
    }

    public final boolean isRoot() {
        return this == root;
    }

    public final <T extends Behavior> T require(Class<T> c) {
        if (!isRoot()) {
            return root.require(c);
        }
        // Check if the behavior already exists
        try {
            return get(c);
        } catch (RuntimeException e) {
        }
        // Instantiate a new behavior
        try {
            currentRoot = this;
            T r = c.newInstance();
            currentRoot = null;
            return r;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Behavior does not have an empty public constructor: " + c.getSimpleName());
        }
    }

    // Overridable functions
    public void createInner() {
    }

    public void destroyInner() {
    }

    public Layer layer() {
        return UPDATE;
    }

    public void step() {
    }
}
