package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.graphics.Camera;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;
import java.util.Collection;

public class Player extends Behavior {

    public static final Collection<Player> ALL = track(Player.class);

    public static final Layer POSTPHYSICS = new Layer(6);

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);

    public Vec3d cameraOffset = new Vec3d(0, 0, .8);
    public Vec3d prevVelocity = new Vec3d(0, 0, 0);

    @Override
    public void createInner() {
        // physics.centerOfMass = () -> Vive.footTransform.get().position().lerp(EyeCamera.headPose().position(), .5);
        Vive.footTransform = () -> pose.getTransform().translate(new Vec3d(0, 0, -1));
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
        if (cameraOffset != null) {
            Camera.camera3d.position = pose.position.add(cameraOffset);
        }
        if (Vive.running) {
            if (physics.velocity().sub(prevVelocity).lengthSquared() > 50) {
                Vive.LEFT.hapticPulse(5);
                Vive.RIGHT.hapticPulse(5);
            }
        }
        prevVelocity = physics.velocity();
    }
}
