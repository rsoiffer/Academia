package beige_engine.util.rlestorage;

import java.util.HashMap;
import java.util.stream.Stream;

public class RLEMapStorage<T> extends RLEStorage<T> {

    private final HashMap<IntPos, RLEColumn<T>> columnMap = new HashMap();
    private final IntConverter<T> ic;

    public RLEMapStorage(IntConverter<T> ic) {
        this.ic = ic;
    }

    @Override
    public Stream<RLEColumn<T>> allColumns() {
        return columnMap.values().stream();
    }

    @Override
    public RLEColumn<T> columnAt(int x, int y) {
        columnMap.putIfAbsent(new IntPos(x, y), new RLEColumn(x, y, ic));
        return columnMap.get(new IntPos(x, y));
    }

    private static class IntPos {

        public final int x, y;

        public IntPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IntPos other = (IntPos) obj;
            if (this.x != other.x) {
                return false;
            }
            return this.y == other.y;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + this.x;
            hash = 73 * hash + this.y;
            return hash;
        }
    }
}
