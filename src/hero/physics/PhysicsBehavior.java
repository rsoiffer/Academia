package hero.physics;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Vec3d;
import static hero.physics.OdeUtils.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.internal.DxBody;
import static org.ode4j.ode.internal.DxBody.dBodyCreate;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxMass;

public class PhysicsBehavior extends Behavior {

    public static Collection<PhysicsBehavior> ALL = track(PhysicsBehavior.class);

    public final PoseBehavior pose = require(PoseBehavior.class);

    public PhysicsManager manager;

    public List<PhysicsBehavior> ignore = new ArrayList();
    public double drag = .02;
    public boolean onGround = false;
    public boolean allowRotation = false;
    public List<DGeom> hit = new ArrayList();
    public Vec3d lastClearPos;
    public Runnable onPhysicsStep = null;

    private DxBody body;
    private DxMass mass;
    private DxGeom geom;
    private Vec3d totalForce = new Vec3d(0, 0, 0);

    public void applyForce(Vec3d force) {
        totalForce = totalForce.add(force);
    }

    @Override
    public void createInner() {
        body = dBodyCreate(manager.world);
        setPosition(pose.position);
        lastClearPos = pose.position;
    }

    @Override
    public void destroyInner() {
        body.destroy();
        geom.destroy();
    }

    public DxBody getBody() {
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

    public void setGeom(DxGeom geom) {
        if (this.geom != null) {
            this.geom.destroy();
        }
        geom.setBody(body);
        geom.setData(this);
        this.geom = geom;
    }

    public void setMass(DxMass mass) {
        body.setMass(mass);
        this.mass = mass;
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
