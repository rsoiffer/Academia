package beige_engine.util.rlestorage;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Stream;

public abstract class RLEStorage<T> {

    private int maxZ, minZ;
    private boolean shouldRecomputeMinMax = true;

    public abstract Stream<RLEColumn<T>> allColumns();

    public abstract RLEColumn<T> columnAt(int x, int y);

    public Iterator<Entry<Integer, T>> columnIterator(int x, int y) {
        return columnAt(x, y).iterator();
    }

    public void copyTo(RLEStorage<T> other, int x, int y, int z) {
        allColumns().forEach(c -> {
            RLEColumn<T> c2 = other.columnAt(c.x + x, c.y + y);
            if (c2 != null) {
                Iterator<Entry<Integer, T>> iterator = c.iterator();
                Entry<Integer, T> prev = null;
                while (iterator.hasNext()) {
                    Entry<Integer, T> e = iterator.next();
                    if (prev == null) {
                        if (e.getValue() != null) {
                            c2.setRangeInfinite(e.getKey() + z, e.getValue());
                        }
                    } else {
                        c2.setRange(prev.getKey() + z + 1, e.getKey() + z, e.getValue());
                    }
                    prev = e;
                }
            }
        });
    }

    public T get(int x, int y, int z) {
        return columnAt(x, y).get(z);
    }

    public int maxZ() {
        recomputeMinMax();
        return maxZ;
    }

    public int minZ() {
        recomputeMinMax();
        return minZ;
    }

    public boolean rangeEquals(int x, int y, int zMin, int zMax, T t) {
        return columnAt(x, y).rangeEquals(zMin, zMax, t);
    }

    private void recomputeMinMax() {
        if (shouldRecomputeMinMax) {
            minZ = Integer.MAX_VALUE;
            maxZ = Integer.MIN_VALUE;
            allColumns().forEach(c -> {
                if (!c.isEmpty()) {
                    minZ = Math.min(minZ, c.minPos());
                    maxZ = Math.max(maxZ, c.maxPos());
                }
            });
            shouldRecomputeMinMax = false;
        }
    }

    public void set(int x, int y, int z, T t) {
        columnAt(x, y).set(z, t);
        shouldRecomputeMinMax = true;
    }

    public void setRange(int x, int y, int zMin, int zMax, T t) {
        columnAt(x, y).setRange(zMin, zMax, t);
        shouldRecomputeMinMax = true;
    }

    public void setRangeInfinite(int x, int y, int zMax, T t) {
        columnAt(x, y).setRangeInfinite(zMax, t);
        shouldRecomputeMinMax = true;
    }
}
