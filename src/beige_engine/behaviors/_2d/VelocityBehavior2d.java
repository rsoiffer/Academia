package beige_engine.behaviors._2d;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.util.math.Vec2d;

public class VelocityBehavior2d extends Behavior {

    public final PositionBehavior2d position = require(PositionBehavior2d.class);

    public Vec2d velocity = new Vec2d(0, 0);

    @Override
    public void step() {
        position.position = position.position.add(velocity.mul(dt()));
    }
}
