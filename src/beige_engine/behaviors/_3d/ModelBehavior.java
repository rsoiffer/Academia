package beige_engine.behaviors._3d;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import static beige_engine.engine.Layer.RENDER3D;
import beige_engine.graphics.Color;
import beige_engine.graphics.voxels.VoxelModel;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;

public class ModelBehavior extends Behavior {

    public final PositionBehavior3d position = require(PositionBehavior3d.class);

    public VoxelModel model;
    public double rotation = 0;
    public double scale = 1 / 16.;
    public Color color = Color.WHITE;
    public boolean useOriginalSize;

    @Override
    public Layer layer() {
        return RENDER3D;
    }

    @Override
    public void step() {
        Transformation t = Transformation.create(position.position, Quaternion.fromAngleAxis(rotation, new Vec3d(0, 0, 1)), scale);
        // model.render(t.translate(useOriginalSize ? model.originalSize().mul(-.5) : model.center().mul(-1)), color);
        model.render(t.translate(model.originalSize().mul(-.5)), color);
    }
}
