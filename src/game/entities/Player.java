package game.entities;

import engine.graphics.Camera;
import static engine.physics.DynamicShape.sphere;
import engine.physics.PhysicsComponent;
import engine.physics.PhysicsManager;
import engine.physics.PoseComponent;
import engine.samples.Behavior;
import engine.util.math.Vec3d;
import engine.vr.VrCore;

public class Player extends Behavior {

    public final PoseComponent pose;
    public final PhysicsComponent physics;

    public Vec3d cameraOffset = new Vec3d(0, 0, .8);
    public Vec3d prevVelocity = new Vec3d(0, 0, 0);

    public Player(Vec3d position, PhysicsManager manager) {
        pose = add(new PoseComponent(this, position));
        physics = add(new PhysicsComponent(this, manager, sphere(1, 100)));

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
