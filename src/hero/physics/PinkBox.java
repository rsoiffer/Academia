package hero.physics;

import beige_engine.samples.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.game.ModelBehavior;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.materials.ColorMaterial;
import static org.ode4j.ode.internal.DxBox.dCreateBox;
import org.ode4j.ode.internal.DxMass;

public class PinkBox extends Behavior {

    public final PoseBehavior pose;
    public final PhysicsBehavior physics;
    public final ModelBehavior model;

    public PinkBox(Vec3d position, PhysicsManager manager) {
        pose = new PoseBehavior(this, position);
        physics = new PhysicsBehavior(this, manager);
        model = new ModelBehavior(this);

        var mass = new DxMass();
        mass.setBoxTotal(50, 2, 2, 2);
        physics.setMass(mass);

        var geom = dCreateBox(physics.manager.space, 2, 2, 2);
        physics.setGeom(geom);

        physics.allowRotation = true;

        var material = new ColorMaterial();
        var node = new ModelNode(material.buildRenderable(Platonics.cube));
        node.transform = Transformation.create(new Vec3d(-1, -1, -1), Quaternion.IDENTITY, 2);
        model.node.addChild(node);
    }

    @Override
    public void onStep() {
        model.node.transform = pose.getTransform();
    }
}
