package beige_engine.behaviors._2d;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import static beige_engine.engine.Layer.POSTUPDATE;
import beige_engine.util.math.Vec2d;

public class PreviousPositionBehavior2d extends Behavior {

    public final PositionBehavior2d position = require(PositionBehavior2d.class);

    public Vec2d prevPos;

    @Override
    public void createInner() {
        prevPos = position.position;
    }

    @Override
    public Layer layer() {
        return POSTUPDATE;
    }

    @Override
    public void step() {
        prevPos = position.position;
    }
}
