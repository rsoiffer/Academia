package beige_engine.util.rlestorage;

import java.util.Arrays;
import java.util.stream.Stream;

public class RLEArrayStorage<T> extends RLEStorage<T> {

    private final int size;
    private final RLEColumn<T>[] columns;

    public RLEArrayStorage(int size, IntConverter<T> ic) {
        this.size = size;
        columns = new RLEColumn[size * size];
        for (int i = 0; i < size * size; i++) {
            columns[i] = new RLEColumn(i / size, i % size, ic);
        }
    }

    @Override
    public Stream<RLEColumn<T>> allColumns() {
        return Arrays.stream(columns);
    }

    @Override
    public RLEColumn<T> columnAt(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            return null;
        }
        return columns[size * x + y];
    }
}
