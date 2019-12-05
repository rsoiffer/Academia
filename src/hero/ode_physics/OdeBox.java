package hero.ode_physics;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.game.ModelBehavior;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.materials.ColorMaterial;
import hero.physics.PoseBehavior;
import org.ode4j.math.DMatrix3;
import org.ode4j.ode.DMatrix;
import org.ode4j.ode.internal.DxBody;
import org.ode4j.ode.internal.DxGeom;
import org.ode4j.ode.internal.DxMass;

import static org.ode4j.ode.internal.DxBody.dBodyCreate;
import static org.ode4j.ode.internal.DxBox.dCreateBox;
import static org.ode4j.ode.internal.Rotation.dRFromAxisAndAngle;

public class OdeBox extends Behavior {

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);

    public OdePhysicsManager manager;
    public DxBody body;
    public DxMass mass;
    public DxGeom geom;

    @Override
    public void createInner() {
        body = dBodyCreate(manager.world);
        body.setPosition(pose.position.x, pose.position.y, pose.position.z);
        var r = new DMatrix3();
        dRFromAxisAndAngle(r,
                Math.random() * 2 - 1,
                Math.random() * 2 - 1,
                Math.random() * 2 - 1,
                Math.random() * 10 - 5);
        body.setRotation(r);

        mass = new DxMass();
        mass.setBox(1, 2, 2, 2);
        body.setMass(mass);
        geom = dCreateBox(manager.space, 2, 2, 2);
        geom.setBody(body);

        var material = new ColorMaterial();
        var node = new ModelNode(material.buildRenderable(Platonics.cube));
        node.transform = Transformation.create(new Vec3d(-1, -1, -1), Quaternion.IDENTITY, 2);
        model.node.addChild(node);
    }

    @Override
    public void step() {
        var pos = body.getPosition();
        pose.position = new Vec3d(pos.get0(), pos.get1(), pos.get2());
        var quat = body.getQuaternion();
        pose.rotation = new Quaternion(quat.get0(), quat.get1(), quat.get2(), quat.get3());
        model.node.transform = pose.getTransform();
    }
}
