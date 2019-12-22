package engine.rendering.surfaces;

import engine.rendering.Mesh;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import java.util.stream.Stream;

public abstract class Surface {

    protected static final double MIN = -1, BOUNDARY = 0, MAX = 1;

    public final Transformation transform;

    public Surface(Transformation transform) {
        this.transform = transform;
    }

    public abstract double get(int x, int y, int z);

    public double getInterp(int x, int y, int z, Vec3d frac) {
        double r = 0;
        for (int i = 0; i < 8; i++) {
            int x2 = i / 4, y2 = i / 2 % 2, z2 = i % 2;
            var d = get(x + x2, y + y2, z + z2);
            r += d * Math.abs((x2 + frac.x - 1) * (y2 + frac.y - 1) * (z2 + frac.z - 1));
        }
        return r;
    }

    public abstract Stream<Mesh> meshes();

    public abstract void set(int x, int y, int z, double value);
}
