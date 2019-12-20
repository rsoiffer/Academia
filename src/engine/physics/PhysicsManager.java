package engine.physics;

import engine.core.AbstractSystem;
import static engine.core.Core.dt;
import static engine.physics.OdeUtils.*;
import engine.util.math.Vec3d;
import java.util.OptionalDouble;
import org.ode4j.math.DVector3;
import static org.ode4j.ode.OdeConstants.*;
import org.ode4j.ode.*;
import static org.ode4j.ode.OdeHelper.createContactJoint;
import static org.ode4j.ode.internal.DxRay.dCreateRay;
import static org.ode4j.ode.internal.OdeInit.dInitODE;
import org.ode4j.ode.internal.joints.DxJointGroup;
import static org.ode4j.ode.internal.joints.DxJointGroup.dJointGroupCreate;

public class PhysicsManager extends AbstractSystem {

    static {
        dInitODE();
    }

    public static final double STEP_SIZE = .02;

    private final DWorld world;
    private final DHashSpace dynamics;
    private final DQuadTreeSpace statics;

    private double timeSinceLastStep;
    private final DxJointGroup contactGroup;

    public PhysicsManager() {
        world = OdeHelper.createWorld();
        world.setGravity(0, 0, -9.81);
        world.setERP(.1);
        world.setCFM(1e-5);
        world.setContactSurfaceLayer(.001);

        dynamics = OdeHelper.createHashSpace();
        dynamics.setLevels(2, 14);

        statics = OdeHelper.createQuadTreeSpace(new DVector3(1000, 1000, 200), new DVector3(2000, 2000, 400), 10);

        contactGroup = dJointGroupCreate(0);

        OdeHelper.createPlane(statics, 0, 0, 1, 0);
    }

    public void addDynamic(DGeom geom) {
        dynamics.add(geom);
    }

    public void addStatic(StaticShape shape, Vec3d position) {
        var geom = shape.build();
        geom.setPosition(toDVector3(position));
        statics.add(geom);
    }

    public void destroy() {
        contactGroup.destroy();
        dynamics.destroy();
        statics.destroy();
        world.destroy();
    }

    private void handleCollision(Object data, DGeom o1, DGeom o2) {
        if (o1 == o2) {
            return;
        }
        var p1 = (PhysicsComponent) o1.getData();
        var p2 = (PhysicsComponent) o2.getData();
        if (p1 == null && p2 == null) {
            return;
        }
        if (p1 != null && p2 != null && (p1.ignore.contains(p2) || p2.ignore.contains(p1))) {
            return;
        }
        var b1 = o1.getBody();
        var b2 = o2.getBody();
        var contacts = collide(o1, o2, contact -> {
            var surface = contact.surface;
            surface.mode = dContactApprox1 | dContactFDir1 | dContactBounce;
            surface.mu = .2;
            surface.bounce = 0.1;
            surface.bounce_vel = 0.001;
        });
        if (!contacts.isEmpty()) {
            if (p1 != null) {
                p1.hit.add(o2);
            }
            if (p2 != null) {
                p2.hit.add(o1);
            }
        }
        for (var contact : contacts) {
            contact.fdir1.set(randomFrictionDir(contact.geom.normal));
            var c = createContactJoint(world, contactGroup, contact);
            c.attach(b1, b2);
        }
    }

    private void handleDynamicsCollision(Object data, DGeom o1, DGeom o2) {
        if (o1 == o2) {
            return;
        }
        var p1 = (PhysicsComponent) o1.getData();
        var p2 = (PhysicsComponent) o2.getData();
        if (p1.ignore.contains(p2) || p2.ignore.contains(p1)) {
            return;
        }
        var b1 = o1.getBody();
        var b2 = o2.getBody();
        var contacts = collide(o1, o2, contact -> {
            var surface = contact.surface;
            surface.mode = dContactApprox1 | dContactFDir1 | dContactBounce;
            surface.mu = .2;
            surface.bounce = 0.1;
            surface.bounce_vel = 0.001;
        });
        if (!contacts.isEmpty()) {
            p1.hit.add(o2);
            p2.hit.add(o1);
        }
        for (var contact : contacts) {
            contact.fdir1.set(randomFrictionDir(contact.geom.normal));
            var c = createContactJoint(world, contactGroup, contact);
            c.attach(b1, b2);
        }
    }

    public DBody newBody() {
        return OdeHelper.createBody(world);
    }

    @Override
    public void onStep() {
        PhysicsComponent.BEFORE_STEP.step();
        timeSinceLastStep += dt();
        while (timeSinceLastStep > STEP_SIZE) {
            timeSinceLastStep -= STEP_SIZE;
            dynamics.collide(null, this::handleDynamicsCollision);
            dynamics.collide2(statics, null, this::handleCollision);
            PhysicsComponent.BEFORE_PHYSICS_STEP.step();
            world.quickStep(STEP_SIZE);
            PhysicsComponent.AFTER_PHYSICS_STEP.step();
        }
        contactGroup.empty();
        PhysicsComponent.AFTER_STEP.step();
    }

    public OptionalDouble raycast(Vec3d start, Vec3d dir) {
        var ray = dCreateRay(null, 1000);
        ray.set(toDVector3(start), toDVector3(dir));
        ray.setClosestHit(true);
        return collide(ray, statics).stream().mapToDouble(contact -> contact.geom.depth).min();
    }

    public double timeSinceLastStep() {
        return timeSinceLastStep;
    }
}
