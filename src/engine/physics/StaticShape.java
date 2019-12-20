package engine.physics;

import static engine.physics.OdeUtils.toDVector3;
import engine.util.math.Vec3d;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.OdeHelper;

public interface StaticShape {

    DGeom build();

    public static StaticShape box(Vec3d size) {
        return () -> OdeHelper.createBox(toDVector3(size));
    }
}
