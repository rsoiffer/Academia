package engine.physics;

import engine.core.AbstractComponent;
import engine.core.AbstractEntity;
import static engine.physics.OdeUtils.*;
import engine.util.math.Quaternion;
import engine.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

public class PhysicsComponent extends AbstractComponent {

    public final PoseComponent pose = require(PoseComponent.class);

    public final PhysicsManager manager;

    public List<PhysicsComponent> ignore = new ArrayList();
    public double drag = .02;
    public boolean onGround = false;
    public boolean allowRotation = false;
    public List<DGeom> hit = new ArrayList();
    public Vec3d lastClearPos;
    public Runnable onPhysicsStep = null;

    private DBody body;
    private DMass mass;
    private DGeom geom;
    private Vec3d totalForce = new Vec3d(0, 0, 0);

    public void applyForce(Vec3d force) {
        totalForce = totalForce.add(force);
    }

    public PhysicsComponent(AbstractEntity entity, PhysicsManager manager, DynamicShape shape) {
        super(entity);
        this.manager = manager;
        body = manager.newBody();
        mass = OdeHelper.createMass();
        geom = shape.build(mass);
        body.setMass(mass);
        geom.setBody(body);
        geom.setData(this);
        manager.addDynamic(geom);

        setPosition(pose.position);
        lastClearPos = pose.position;
    }

    @Override
    public void onDestroy() {
        body.destroy();
        geom.destroy();
    }

    public DBody getBody() {
        return body;
    }

    public double getMass() {
        return mass.getMass();
    }

    void onPrePhysicsStep() {
        hit.clear();
    }

    void onPhysicsStep1() {
        if (hit.isEmpty()) {
            lastClearPos = pose.position;
        }

        double airResistanceForce = drag * velocity().lengthSquared();
        if (airResistanceForce > 1e-12) {
            applyForce(velocity().setLength(-airResistanceForce));
        }
        body.addForce(toDVector3(totalForce));
        if (onPhysicsStep != null) {
            onPhysicsStep.run();
        }
    }

    void onPhysicsStep2() {
        pose.position = toVec3d(body.getPosition());
        if (!allowRotation) {
            body.setQuaternion(toDQuaternion(Quaternion.IDENTITY));
            body.setAngularVel(0, 0, 0);
        } else {
            pose.rotation = toQuaternion(body.getQuaternion());
        }
    }

    void onPostPhysicsStep() {
        totalForce = new Vec3d(0, 0, 0);
    }

    public void setPosition(Vec3d pos) {
        pose.position = pos;
        body.setPosition(toDVector3(pos));
    }

    public void setVelocity(Vec3d vel) {
        body.setLinearVel(toDVector3(vel));
    }

    public Vec3d velocity() {
        return toVec3d(body.getLinearVel());
    }
}
