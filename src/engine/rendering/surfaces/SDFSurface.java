package engine.rendering.surfaces;

import engine.physics.AABB;
import engine.rendering.Mesh;
import static engine.rendering.surfaces.Surface.MAX;
import static engine.rendering.surfaces.Surface.MIN;
import engine.rendering.utils.SDF;
import static engine.util.math.MathUtils.ceil;
import static engine.util.math.MathUtils.floor;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import java.util.stream.Stream;

public class SDFSurface {

    private final double scale;
    private final Surface surface;

    public SDFSurface(double scale) {
        this.scale = scale;
//        surface = MarchingCubes.chunked(Transformation.create(new Vec3d(0, 0, 0), Quaternion.IDENTITY, scale));
        surface = SurfaceNet.chunked(Transformation.create(new Vec3d(0, 0, 0), Quaternion.IDENTITY, scale));
    }

    public Stream<Mesh> getMeshes() {
        return surface.meshes();
    }

    public void intersectionSDF(SDF sdf, AABB bounds) {
        sdf = sdf.scale(scale);
        int xMin = floor(bounds.lower.x / scale), yMin = floor(bounds.lower.y / scale), zMin = floor(bounds.lower.z / scale);
        int xMax = ceil(bounds.upper.x / scale), yMax = ceil(bounds.upper.y / scale), zMax = ceil(bounds.upper.z / scale);
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    double d = quantize(sdf.value(new Vec3d(x, y, z)));
                    if (d < MAX && d < surface.get(x, y, z)) {
                        surface.set(x, y, z, d);
                    }
                }
            }
        }
    }

    private static double quantize(double d) {
//        return (floor(d * 32) + .5) / 32;
        return d;
    }

    public void unionSDF(SDF sdf, AABB bounds) {
        sdf = sdf.scale(scale);
        int xMin = floor(bounds.lower.x / scale), yMin = floor(bounds.lower.y / scale), zMin = floor(bounds.lower.z / scale);
        int xMax = ceil(bounds.upper.x / scale), yMax = ceil(bounds.upper.y / scale), zMax = ceil(bounds.upper.z / scale);
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    double d = quantize(sdf.value(new Vec3d(x, y, z)));
                    if (d > MIN && d > surface.get(x, y, z)) {
                        surface.set(x, y, z, d);
                    }
                }
            }
        }
    }
}
