package hero.game;

import beige_engine.graphics.Camera;
import beige_engine.samples.Behavior;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrCore;
import hero.physics.PhysicsBehavior;
import hero.physics.PhysicsManager;
import hero.physics.PoseBehavior;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class Player extends Behavior {

    public final PoseBehavior pose;
    public final PhysicsBehavior physics;

    public Vec3d cameraOffset = new Vec3d(0, 0, .8);
    public Vec3d prevVelocity = new Vec3d(0, 0, 0);

    public Player(Vec3d position, PhysicsManager manager) {
        pose = add(new PoseBehavior(this, position));
        physics = add(new PhysicsBehavior(this, manager));

        var mass = new DxMass();
        mass.setSphereTotal(100, 1);
        physics.setMass(mass);

        var geom = dCreateSphere(physics.manager.space, 1);
        physics.setGeom(geom);

        // physics.centerOfMass = () -> Vive.footTransform.get().position().lerp(EyeCamera.headPose().position(), .5);
        VrCore.footTransform = () -> pose.getTransform().translate(new Vec3d(0, 0, -1));
    }

    @Override
    public void onStep() {
        if (cameraOffset != null) {
            Camera.camera3d.position = pose.position.add(cameraOffset);
        }
        if (physics.velocity().sub(prevVelocity).lengthSquared() > 50) {
            VrCore.LEFT.hapticPulse(5);
            VrCore.RIGHT.hapticPulse(5);
        }
        prevVelocity = physics.velocity();
    }
}
