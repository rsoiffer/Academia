package game.entities;

import engine.samples.Behavior;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import engine.rendering.Platonics;
import engine.rendering.materials.ColorMaterial;
import static org.ode4j.ode.internal.DxBox.dCreateBox;

import engine.physics.PhysicsComponent;
import engine.physics.PhysicsManager;
import engine.physics.PoseComponent;
import org.ode4j.ode.internal.DxMass;

public class PinkBox extends Behavior {

    public final PoseComponent pose;
    public final PhysicsComponent physics;
    public final ModelComponent model;

    public PinkBox(Vec3d position, PhysicsManager manager) {
        pose = new PoseComponent(this, position);
        physics = new PhysicsComponent(this, manager);
        model = new ModelComponent(this);

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
