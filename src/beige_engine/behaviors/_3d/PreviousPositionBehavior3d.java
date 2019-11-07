package beige_engine.behaviors._3d;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import static beige_engine.engine.Layer.POSTUPDATE;
import beige_engine.util.math.Vec3d;

public class PreviousPositionBehavior3d extends Behavior {

    public final PositionBehavior3d position = require(PositionBehavior3d.class);

    public Vec3d prevPos;

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
