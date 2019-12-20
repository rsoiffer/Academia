package beige_engine.core;

import java.util.*;

public abstract class AbstractEntity {

    static final Map<Class, Set<AbstractEntity>> ALL_ENTITIES = new HashMap();

    public static <T extends AbstractEntity> Collection<T> getAll(Class<T> c) {
        return new LinkedList(ALL_ENTITIES.getOrDefault(c, new HashSet()));
    }

    private final Map<Class, AbstractComponent> components = new HashMap();
    private boolean isDestroyed = false;

    public AbstractEntity() {
        if (!ALL_ENTITIES.containsKey(getClass())) {
            ALL_ENTITIES.put(getClass(), new HashSet());
        }
        ALL_ENTITIES.get(getClass()).add(this);
    }

    protected final <T extends AbstractComponent> T add(T c) {
        if (components.containsKey(c.getClass())) {
            throw new IllegalArgumentException("Entity " + this + " already has a component of type " + c.getClass());
        }
        components.put(c.getClass(), c);
        return c;
    }

    protected final void addAll(AbstractComponent... ca) {
        for (var c : ca) {
            add(c);
        }
    }

    public final void destroy() {
        if (isDestroyed) {
            throw new IllegalStateException("Entity " + this + " has already been destroyed");
        }
        isDestroyed = true;
        ALL_ENTITIES.get(getClass()).remove(this);
        onDestroy();
        for (var c : components.values()) {
            c.onDestroy();
        }
    }

    public final <T extends AbstractComponent> T getComponent(Class<T> c) {
        if (!components.containsKey(c)) {
            throw new IllegalArgumentException("Entity " + this + " doesn't have component of type " + c);
        }
        return (T) components.get(c);
    }

    public final boolean hasComponent(Class<? extends AbstractComponent> c) {
        return components.containsKey(c);
    }

    public final boolean isDestroyed() {
        return isDestroyed;
    }

    protected void onDestroy() {
    }
}
