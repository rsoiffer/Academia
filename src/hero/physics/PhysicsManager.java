package hero.physics;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.engine.Layer;
import beige_engine.util.math.Vec3d;
import static hero.physics.OdeUtils.*;
import java.util.OptionalDouble;
import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.createContactJoint;
import static org.ode4j.ode.internal.DxHashSpace.dHashSpaceCreate;
import static org.ode4j.ode.internal.DxPlane.dCreatePlane;
import static org.ode4j.ode.internal.DxRay.dCreateRay;
import org.ode4j.ode.internal.DxSpace;
import org.ode4j.ode.internal.DxWorld;
import static org.ode4j.ode.internal.DxWorld.dWorldCreate;
import static org.ode4j.ode.internal.OdeInit.dInitODE;
import org.ode4j.ode.internal.joints.DxJointGroup;
import static org.ode4j.ode.internal.joints.DxJointGroup.dJointGroupCreate;

public class PhysicsManager extends Behavior {

    public static final Layer PHYSICS = new Layer(5);

    static {
        dInitODE();
    }

    public static final double STEP_SIZE = .001;

    public double time;
    public DxWorld world;
    public DxSpace space;
    public DxSpace staticSpace;
    public DxJointGroup contactGroup;

    @Override
    public void createInner() {
        world = dWorldCreate();
        world.setGravity(0, 0, -9.81);
        world.setERP(.1);
        world.setCFM(1e-5);
        world.setContactSurfaceLayer(.001);

        var hspace = dHashSpaceCreate(null);
        hspace.setLevels(2, 8);
        space = hspace;

        var hspace2 = dHashSpaceCreate(null);
        hspace2.setLevels(2, 8);
        hspace2.setSublevel(1);
        staticSpace = hspace2;
        space.add(staticSpace);

        contactGroup = dJointGroupCreate(0);

        dCreatePlane(staticSpace, 0, 0, 1, 0);
    }

    @Override
    public void destroyInner() {
        contactGroup.destroy();
        space.destroy();
        world.destroy();
    }

    @Override
    public Layer layer() {
        return PHYSICS;
    }

    private void physicsStep() {
        space.collide(null, (data, o1, o2) -> {
            if (o1 == o2) {
                return;
            }
            var p1 = (PhysicsBehavior) o1.getData();
            var p2 = (PhysicsBehavior) o2.getData();
            if (p1 != null && p2 != null && (p1.ignore.contains(p2) || p2.ignore.contains(p1))) {
                return;
            }
            var b1 = o1.getBody();
            var b2 = o2.getBody();
            var contacts = collide(o1, o2, contact -> {
                var surface = contact.surface;
                surface.mode = dContactApprox1 | dContactFDir1 | dContactBounce;
                surface.mu = .5;
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
        });
        PhysicsBehavior.ALL.forEach(PhysicsBehavior::onPhysicsStep1);
        world.quickStep(STEP_SIZE);
        PhysicsBehavior.ALL.forEach(PhysicsBehavior::onPhysicsStep2);
    }

    public OptionalDouble raycast(Vec3d start, Vec3d dir) {
        var ray = dCreateRay(null, 1000);
        ray.set(toDVector3(start), toDVector3(dir));
        ray.setClosestHit(true);
        return collide(ray, staticSpace).stream().mapToDouble(contact -> contact.geom.depth).min();
    }

    @Override
    public void step() {
        PhysicsBehavior.ALL.forEach(PhysicsBehavior::onPrePhysicsStep);
        time += dt();
        while (time > STEP_SIZE) {
            time -= STEP_SIZE;
            physicsStep();
        }
        contactGroup.empty();
        PhysicsBehavior.ALL.forEach(PhysicsBehavior::onPostPhysicsStep);
    }
}
