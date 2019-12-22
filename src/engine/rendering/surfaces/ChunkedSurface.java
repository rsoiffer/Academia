package engine.rendering.surfaces;

import engine.rendering.Mesh;
import static engine.util.math.MathUtils.mod;
import engine.util.math.Transformation;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class ChunkedSurface extends Surface {

    private final int chunkSize, overlapNeg, overlapPos;
    private final Function<Key3i, Surface> constructor;
    private final HashMap<Key3i, Surface> chunks = new HashMap();

    public ChunkedSurface(Transformation transform, int chunkSize, int overlapNeg, int overlapPos, Function<Key3i, Surface> constructor) {
        super(transform);
        this.chunkSize = chunkSize;
        this.overlapNeg = overlapNeg;
        this.overlapPos = overlapPos;
        this.constructor = constructor;
    }

    @Override
    public double get(int x, int y, int z) {
        var chunk = chunks.get(toKey(x, y, z));
        return chunk == null ? -1 : chunk.get(mod(x, chunkSize), mod(y, chunkSize), mod(z, chunkSize));
    }

    private Surface getChunk(Key3i key) {
        return chunks.computeIfAbsent(key, constructor);
    }

    @Override
    public Stream<Mesh> meshes() {
        return chunks.values().stream().flatMap(Surface::meshes);
    }

    @Override
    public void set(int x, int y, int z, double value) {
        int xm = mod(x, chunkSize), ym = mod(y, chunkSize), zm = mod(z, chunkSize);
        var key = toKey(x, y, z);
        var key0 = toKey(x - overlapPos, y - overlapPos, z - overlapPos);
        var key1 = toKey(x + overlapNeg, y + overlapNeg, z + overlapNeg);
        for (int i = key0.x; i <= key1.x; i++) {
            for (int j = key0.y; j <= key1.y; j++) {
                for (int k = key0.z; k <= key1.z; k++) {
                    getChunk(new Key3i(i, j, k)).set(xm + (key.x - i) * chunkSize,
                            ym + (key.y - j) * chunkSize, zm + (key.z - k) * chunkSize, value);
                }
            }
        }
    }

    private Key3i toKey(int x, int y, int z) {
        return new Key3i(Math.floorDiv(x, chunkSize), Math.floorDiv(y, chunkSize), Math.floorDiv(z, chunkSize));
    }
}
