package hero.graphics.utils;

import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import hero.graphics.Mesh;
import hero.graphics.loading.RawMeshBuilder;

import static hero.graphics.VertexAttrib.POSITIONS;
import static hero.graphics.VertexAttrib.TEX_COORDS;

public class MeshSimplifier {

    private static Vec2d asVec2d(float[] f) {
        if (f.length != 2) {
            throw new IllegalArgumentException("Wrong input length");
        }
        return new Vec2d(f[0], f[1]);
    }

    private static Vec3d asVec3d(float[] f) {
        if (f.length != 3) {
            throw new IllegalArgumentException("Wrong input length");
        }
        return new Vec3d(f[0], f[1], f[2]);
    }

    public static Mesh simplify(Mesh originalModel, double mod) {
        RawMeshBuilder newModel = new RawMeshBuilder();
        for (int i = 0; i < originalModel.numFaces; i++) {
            var v1 = originalModel.getVertex(originalModel.getIndex(3 * i));
            var v2 = originalModel.getVertex(originalModel.getIndex(3 * i + 1));
            var v3 = originalModel.getVertex(originalModel.getIndex(3 * i + 2));
            var newP1 = asVec3d(v1.get(POSITIONS)).div(mod).floor().mul(mod);
            var newP2 = asVec3d(v2.get(POSITIONS)).div(mod).floor().mul(mod);
            var newP3 = asVec3d(v3.get(POSITIONS)).div(mod).floor().mul(mod);
            if (newP1.equals(newP2)) continue;
            if (newP1.equals(newP3)) continue;
            if (newP2.equals(newP3)) continue;
            newModel.addTriangleUV(
                    newP1, asVec2d(v1.get(TEX_COORDS)),
                    newP2, asVec2d(v2.get(TEX_COORDS)),
                    newP3, asVec2d(v3.get(TEX_COORDS)));
        }
        return newModel.toMesh();
    }
}