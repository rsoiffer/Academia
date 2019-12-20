package game.entities;

import engine.graphics.Camera;
import engine.samples.Behavior;
import engine.util.math.Vec3d;
import engine.vr.VrCore;
import engine.physics.PhysicsComponent;
import engine.physics.PhysicsManager;
import engine.physics.PoseComponent;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class Player extends Behavior {

    public final PoseComponent pose;
    public final PhysicsComponent physics;

    public Vec3d cameraOffset = new Vec3d(0, 0, .8);
    public Vec3d prevVelocity = new Vec3d(0, 0, 0);

    public Player(Vec3d position, PhysicsManager manager) {
        pose = add(new PoseComponent(this, position));
        physics = add(new PhysicsComponent(this, manager));

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
