package beige_engine.behaviors._3d;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.util.math.Vec3d;

public class AccelerationBehavior3d extends Behavior {

    public final VelocityBehavior3d velocity = require(VelocityBehavior3d.class);

    public Vec3d acceleration = new Vec3d(0, 0, 0);

    @Override
    public void step() {
        velocity.velocity = velocity.velocity.add(acceleration.mul(dt()));
        velocity.position.position = velocity.position.position.add(acceleration.mul(dt() * dt() / 2));
    }
}
