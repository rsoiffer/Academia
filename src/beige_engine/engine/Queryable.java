package beige_engine.engine;

import java.util.TreeSet;
import java.util.function.Function;

public class Queryable<T> {

    Property<TreeSet<Modifier>> modifiers;

    public Modifier addModifier(Function<T, T> modFunction) {
        return addModifier(0, modFunction);
    }

    public Modifier addModifier(double depth, Function<T, T> modFunction) {
        Modifier m = new Modifier(depth, modFunction);
        modifiers().value.add(m);
        return m;
    }

    public Property<TreeSet<Modifier>> modifiers() {
        if (modifiers == null) {
            modifiers = new Property(new TreeSet());
        }
        return modifiers;
    }

    public T query(T value) {
        T returnVal = value;
        if (modifiers != null) {
            for (Modifier m : modifiers.get()) {
                returnVal = m.modFunction.apply(returnVal);
            }
        }
        return returnVal;
    }

    private static int maxModifierID;

    public class Modifier implements Comparable<Modifier> {

        private final int id = maxModifierID++;
        private final double depth;
        private final Function<T, T> modFunction;

        private Modifier(double depth, Function<T, T> modFunction) {
            this.depth = depth;
            this.modFunction = modFunction;
        }

        @Override
        public int compareTo(Modifier o) {
            if (Double.compare(depth, o.depth) != 0) {
                return Double.compare(depth, o.depth);
            }
            return Integer.compare(id, o.id);
        }

        public void remove() {
            modifiers.value.remove(this);
            if (modifiers.shouldRemove()) {
                modifiers = null;
            }
        }
    }

    public static class Property<T> extends Queryable<T> {

        private T value;

        public Property(T value) {
            this.value = value;
        }

        public T get() {
            return query(value);
        }

        public T getBaseValue() {
            return value;
        }

        public void setBaseValue(T value) {
            this.value = value;
        }

        private boolean shouldRemove() {
            return (modifiers == null || modifiers.shouldRemove()) && ((TreeSet) value).isEmpty();
        }
    }
}
