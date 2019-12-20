package beige_engine.core;

import java.util.*;

public abstract class AbstractComponent {

    private static final Map<Class, Set<AbstractComponent>> ALL_COMPONENTS = new HashMap();

    public static <T extends AbstractComponent> Collection<T> getAll(Class<T> c) {
        return ALL_COMPONENTS.getOrDefault(c, new HashSet());
    }

    public final AbstractEntity entity;

    public AbstractComponent(AbstractEntity entity) {
        this.entity = entity;
        if (!ALL_COMPONENTS.containsKey(getClass())) {
            ALL_COMPONENTS.put(getClass(), new HashSet());
        }
        ALL_COMPONENTS.get(getClass()).add(this);
    }

    protected void onDestroy() {
    }

    protected <T extends AbstractComponent> T require(Class<T> c) {
        return entity.getComponent(c);
    }
}
