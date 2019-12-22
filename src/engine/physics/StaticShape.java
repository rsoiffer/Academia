package engine.physics;

import static engine.physics.OdeUtils.toDVector3;
import engine.rendering.Mesh;
import static engine.rendering.VertexAttrib.POSITIONS;
import engine.util.math.Vec3d;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.OdeHelper;
import static org.ode4j.ode.OdeHelper.createTriMesh;
import static org.ode4j.ode.OdeHelper.createTriMeshData;

public interface StaticShape {

    DGeom build();

    public static StaticShape box(Vec3d size) {
        return () -> OdeHelper.createBox(toDVector3(size));
    }

    public static StaticShape triMesh(Mesh mesh) {
        return () -> {
            var t = createTriMeshData();
            t.build(mesh.data.get(POSITIONS), mesh.indices);
            t.preprocess();
            return createTriMesh(null, t, null, null, null);
        };
    }
}
