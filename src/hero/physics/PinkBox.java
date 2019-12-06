package hero.physics;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.game.ModelBehavior;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.materials.ColorMaterial;
import static org.ode4j.ode.internal.DxBox.dCreateBox;

public class PinkBox extends Behavior {

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);

    @Override
    public void createInner() {
        physics.mass.setBoxTotal(50, 2, 2, 2);
        physics.body.setMass(physics.mass);
        physics.geom = dCreateBox(physics.manager.space, 2, 2, 2);
        physics.geom.setBody(physics.body);

        var material = new ColorMaterial();
        var node = new ModelNode(material.buildRenderable(Platonics.cube));
        node.transform = Transformation.create(new Vec3d(-1, -1, -1), Quaternion.IDENTITY, 2);
        model.node.addChild(node);
    }

    @Override
    public void step() {
        model.node.transform = pose.getTransform();
    }
}
