package beige_engine.behaviors._2d;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.util.math.Vec2d;

public class AccelerationBehavior2d extends Behavior {

    public final VelocityBehavior2d velocity = require(VelocityBehavior2d.class);

    public Vec2d acceleration = new Vec2d(0, 0);

    @Override
    public void step() {
        velocity.velocity = velocity.velocity.add(acceleration.mul(dt()));
        velocity.position.position = velocity.position.position.add(acceleration.mul(dt() * dt() / 2));
    }
}
