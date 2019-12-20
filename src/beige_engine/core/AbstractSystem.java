package beige_engine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractSystem {

    public static AbstractSystem empty() {
        return new AbstractSystem() {
            protected void onStep() {
            }
        };
    }

    public static AbstractSystem of(Runnable r) {
        return new AbstractSystem() {
            protected void onStep() {
                r.run();
            }
        };
    }

    public static <T extends AbstractComponent> AbstractSystem perComponent(Class<T> type, Consumer<T> onEach) {
        return new AbstractSystem() {
            protected void onStep() {
                for (var c : AbstractComponent.getAll(type)) {
                    onEach.accept(c);
                }
            }
        };
    }

    public static <T extends AbstractEntity> AbstractSystem perEntity(Class<T> type, Consumer<T> onEach) {
        return new AbstractSystem() {
            protected void onStep() {
                for (var e : AbstractEntity.getAll(type)) {
                    onEach.accept(e);
                }
            }
        };
    }

    private final List<AbstractSystem> subsystems = new ArrayList();

    public final void add(AbstractSystem s) {
        subsystems.add(s);
    }

    protected abstract void onStep();

    public final void step() {
        onStep();
        for (var s : subsystems) {
            s.step();
        }
    }
}
