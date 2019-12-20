package engine.physics;

import static engine.physics.OdeUtils.toDVector3;
import engine.util.math.Vec3d;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

public interface DynamicShape {

    DGeom build(DMass mass);

    public static DynamicShape box(Vec3d size, double totalMass) {
        return mass -> {
            mass.setBoxTotal(totalMass, size.x, size.y, size.z);
            return OdeHelper.createBox(toDVector3(size));
        };
    }

    public static DynamicShape sphere(double radius, double totalMass) {
        return mass -> {
            mass.setSphereTotal(totalMass, radius);
            return OdeHelper.createSphere(radius);
        };
    }
}
