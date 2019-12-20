package beige_engine.core;

import static beige_engine.core.AbstractEntity.ALL_ENTITIES;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AbstractComponent {

    public static <T extends AbstractComponent> Collection<T> getAll(Class<T> c) {
        return ALL_ENTITIES.values().stream().flatMap(x -> x.stream()).filter(e -> e.hasComponent(c))
                .map(e -> e.getComponent(c)).collect(Collectors.toList());
    }

    public final AbstractEntity entity;

    public AbstractComponent(AbstractEntity entity) {
        this.entity = entity;
    }

    protected void onDestroy() {
    }

    protected <T extends AbstractComponent> T require(Class<T> c) {
        return entity.getComponent(c);
    }
}
