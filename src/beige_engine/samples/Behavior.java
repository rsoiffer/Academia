package beige_engine.samples;

import beige_engine.core.AbstractEntity;
import beige_engine.core.AbstractSystem;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public abstract class Behavior extends AbstractEntity {

    private static final Set<Class<? extends Behavior>> BEHAVIOR_CLASSES = new HashSet();

    public static final AbstractSystem BEHAVIOR_SYSTEM = AbstractSystem.of(() -> {
        for (var c : new LinkedList<>(BEHAVIOR_CLASSES)) {
            for (var e : AbstractEntity.getAll(c)) {
                e.onStep();
            }
        }
    });

    public Behavior() {
        BEHAVIOR_CLASSES.add(getClass());
    }

    public abstract void onStep();
}
