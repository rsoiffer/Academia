package hero.physics;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Vec3d;
import static hero.physics.OdeUtils.*;
import java.util.Collection;
import org.ode4j.ode.internal.DxBody;
import static org.ode4j.ode.internal.DxBody.dBodyCreate;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class PhysicsBehavior extends Behavior {

    public static Collection<PhysicsBehavior> ALL = track(PhysicsBehavior.class);
    public static final Layer POST_PHYSICS = new Layer(6);

    public final PoseBehavior pose = require(PoseBehavior.class);

    public PhysicsManager manager;
    public DxBody body;
    public DxMass mass;
    public DxGeom geom;

    public double drag = .1;
    public Vec3d prevVel;
    public Vec3d collisionVel = new Vec3d(0, 0, 0);
    public boolean onGround = false;
    public boolean allowRotation = true;

    private Vec3d totalForce = new Vec3d(0, 0, 0);

    public void applyForce(Vec3d force) {
        totalForce = totalForce.add(force);
    }

    @Override
    public void createInner() {
        body = dBodyCreate(manager.world);
        body.setPosition(pose.position.x, pose.position.y, pose.position.z);

        mass = new DxMass();
        mass.setSphereTotal(100, 1);
        body.setMass(mass);

        geom = dCreateSphere(manager.space, 1);
        geom.setBody(body);
    }

    @Override
    public Layer layer() {
        return POST_PHYSICS;
    }

    public void physicsStep() {
        if (!allowRotation) {
            body.setQuaternion(toDQuaternion(Quaternion.IDENTITY));
            body.setAngularVel(0, 0, 0);
        }

        pose.position = toVec3d(body.getPosition());
        pose.rotation = toQuaternion(body.getQuaternion());
//        double airResistanceForce = drag * velocity().lengthSquared();
//        if (airResistanceForce > 1e-12) {
//            applyForce(velocity().setLength(-airResistanceForce));
//        }
        body.addForce(toDVector3(totalForce));
    }

    public void setVelocity(Vec3d vel) {
        body.setLinearVel(vel.x, vel.y, vel.z);
    }

    @Override
    public void step() {
        if (prevVel != null) {
            collisionVel = velocity().sub(prevVel);
        }
        prevVel = velocity();
        totalForce = new Vec3d(0, 0, 0);
    }

    public Vec3d velocity() {
        return toVec3d(body.getLinearVel());
    }
}
