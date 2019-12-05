package hero.ode_physics;

import beige_engine.engine.Behavior;
import org.ode4j.ode.DContactBuffer;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxSpace;
import org.ode4j.ode.internal.DxWorld;
import org.ode4j.ode.internal.joints.DxJointGroup;

import static beige_engine.engine.Core.dt;
import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.createContactJoint;
import static org.ode4j.ode.internal.DxGeom.NUMC_MASK;
import static org.ode4j.ode.internal.DxGeom.dCollide;
import static org.ode4j.ode.internal.DxHashSpace.dHashSpaceCreate;
import static org.ode4j.ode.internal.DxPlane.dCreatePlane;
import static org.ode4j.ode.internal.DxWorld.dWorldCreate;
import static org.ode4j.ode.internal.OdeInit.dInitODE;
import static org.ode4j.ode.internal.joints.DxJointGroup.dJointGroupCreate;

public class OdePhysicsManager extends Behavior {

    static {
        dInitODE();
    }

    public static final double stepSize = .001;

    public double time;
    public DxWorld world;
    public DxSpace space;
    public DxJointGroup contactGroup;

    @Override
    public void createInner() {
        world = dWorldCreate();
        world.setGravity(0, 0, -9.81);
        world.setContactSurfaceLayer(.001);

        space = dHashSpaceCreate(null);

        contactGroup = dJointGroupCreate(0);

        dCreatePlane(space, 0, 0, 1, 0);
    }

    @Override
    public void destroyInner() {
        contactGroup.destroy();
        space.destroy();
        world.destroy();
    }

    @Override
    public void step() {
        space.collide(null, (data, o1, o2) -> {
            var b1 = o1.getBody();
            var b2 = o2.getBody();
            int maxContacts = 10;
            var contact = new DContactBuffer(maxContacts);
            for (int i = 0; i < maxContacts; i++) {
                var surface = contact.get(i).surface;
                surface.mode = dContactBounce | dContactSoftCFM;
                surface.mu = dInfinity;
                surface.mu2 = 0;
                surface.bounce = 0.01;
                surface.bounce_vel = 0.1;
                surface.soft_cfm = 0.01;
            }
            int numc = dCollide((DxGeom) o1, (DxGeom) o2, NUMC_MASK, contact.getGeomBuffer(), 0);
            for (int i = 0; i < numc; i++) {
                var c = createContactJoint(world, contactGroup, contact.get(i));
                c.attach(b1, b2);
            }
        });
        time += dt();
        while (time > stepSize) {
            time -= stepSize;
            world.quickStep(stepSize);
        }
        contactGroup.empty();
    }
}
