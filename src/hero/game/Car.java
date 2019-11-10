package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.AssimpFile;
import hero.graphics.renderables.ColorModel;
import hero.graphics.renderables.DiffuseModel;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;

import static beige_engine.engine.Core.dt;
import static beige_engine.graphics.Camera.camera3d;

public class Car extends Behavior {

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);

    @Override
    public void createInner() {
        physics.acceleration = new Vec3d(0, 0, -10);
        var carModel = AssimpFile.load("volkswagen/volkswagen-touareg.obj");
        var rot = Quaternion.fromEulerAngles(Math.PI, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, -1), rot, .001);
        for (var m : carModel.meshes) {
            if (m == null || m.material == null) continue;
            if (m.material.texture == null) {
                var car = new ColorModel(m);
                car.color = new Vec3d(m.material.diffuse.r, m.material.diffuse.g, m.material.diffuse.b);
                var carRB = RenderableBehavior.createRB(car);
//                carRB.beforeRender = () -> car.t = trans.mul(pose.getTransform());
                 carRB.beforeRender = () -> car.t = pose.getTransform().mul(trans);
            } else {
                var car = new DiffuseModel(m, m.material.texture.texture);
                var carRB = RenderableBehavior.createRB(car);
//                carRB.beforeRender = () -> car.t = trans.mul(pose.getTransform());
                 carRB.beforeRender = () -> car.t = pose.getTransform().mul(trans);
            }
        }
    }

    public void step() {
        var dir = camera3d.position.sub(pose.position).setZ(0);
        physics.applyForce(dir.setLength(1000), physics.centerOfMass.get());
        pose.rotation = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1).cross(dir));

        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
    }
}
