package engine.physics;

import engine.core.AbstractComponent;
import engine.core.AbstractEntity;
import engine.core.AbstractSystem;
import static engine.physics.OdeUtils.*;
import static engine.physics.PhysicsManager.STEP_SIZE;
import engine.util.math.Quaternion;
import engine.util.math.SplineAnimation;
import engine.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

public class PhysicsComponent extends AbstractComponent {

    static final AbstractSystem BEFORE_STEP = AbstractSystem.perComponent(PhysicsComponent.class, physics -> {
        physics.hit.clear();
    });
    static final AbstractSystem AFTER_STEP = AbstractSystem.perComponent(PhysicsComponent.class, physics -> {
        physics.totalForce = new Vec3d(0, 0, 0);

        physics.pose.position = SplineAnimation.cubicInterp(1 + physics.manager.timeSinceLastStep() / -STEP_SIZE,
                physics.prevPos1, physics.prevVel1.mul(-STEP_SIZE), physics.prevPos2, physics.prevVel2.mul(-STEP_SIZE));
        physics.velocity = SplineAnimation.cubicInterpDerivative(1 + physics.manager.timeSinceLastStep() / -STEP_SIZE,
                physics.prevPos1, physics.prevVel1.mul(-STEP_SIZE), physics.prevPos2, physics.prevVel2.mul(-STEP_SIZE))
                .div(-STEP_SIZE);
    });

    static final AbstractSystem BEFORE_PHYSICS_STEP = AbstractSystem.perComponent(PhysicsComponent.class, physics -> {
        if (physics.hit.isEmpty()) {
            physics.lastClearPos = physics.pose.position;
        }

        double airResistanceForce = physics.drag * physics.velocity().lengthSquared();
        if (airResistanceForce > 1e-12) {
            physics.applyForce(physics.velocity().setLength(-airResistanceForce));
        }
        physics.body.addForce(toDVector3(physics.totalForce));
        if (physics.onPhysicsStep != null) {
            physics.onPhysicsStep.run();
        }
    });
    static final AbstractSystem AFTER_PHYSICS_STEP = AbstractSystem.perComponent(PhysicsComponent.class, physics -> {
        physics.prevPos2 = physics.prevPos1;
        physics.prevVel2 = physics.prevVel1;
        physics.prevPos1 = toVec3d(physics.body.getPosition());
        physics.prevVel1 = toVec3d(physics.body.getLinearVel());

        if (!physics.allowRotation) {
            physics.body.setQuaternion(toDQuaternion(Quaternion.IDENTITY));
            physics.body.setAngularVel(0, 0, 0);
        } else {
            physics.pose.rotation = toQuaternion(physics.body.getQuaternion());
        }
    });

    public final PoseComponent pose = require(PoseComponent.class);

    public final PhysicsManager manager;

    public Set<PhysicsComponent> ignore = new HashSet();
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

    private Vec3d prevPos1, prevPos2, prevVel1, prevVel2;
    private Vec3d velocity;

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
        setVelocity(new Vec3d(0, 0, 0));
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

    public void setPosition(Vec3d pos) {
        pose.position = prevPos1 = prevPos2 = pos;
        body.setPosition(toDVector3(pos));
    }

    public void setVelocity(Vec3d vel) {
        velocity = prevVel1 = prevVel2 = vel;
        body.setLinearVel(toDVector3(vel));
    }

    public Vec3d velocity() {
        return velocity;
    }
}
