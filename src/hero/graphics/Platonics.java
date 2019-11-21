package hero.graphics;

import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import hero.graphics.loading.RawMeshBuilder;

public class Platonics {

    public static final Mesh square;

    static {
        square = new RawMeshBuilder()
                .addRectangleUV(new Vec3d(-.5, -.5, 0), new Vec3d(0, 1, 0), new Vec3d(0, 0, 1),
                        new Vec2d(0, 0), new Vec2d(1, 0), new Vec2d(0, 1))
                .toMesh();
    }
}
