package hero.graphics;

import engine.util.math.Vec2d;
import engine.util.math.Vec3d;
import hero.graphics.loading.RawMeshBuilder;
import hero.graphics.loading.VoxelModelLoader;

public class Platonics {

    public static final Mesh square, cube;

    static {
        square = new RawMeshBuilder()
                .addRectangleUV(new Vec3d(-.5, -.5, 0), new Vec3d(1, 0, 0), new Vec3d(0, 1, 0),
                        new Vec2d(0, 0), new Vec2d(1, 0), new Vec2d(0, 1))
                .toMesh();
        cube = VoxelModelLoader.load("singlevoxel.vox").mesh;
    }
}
