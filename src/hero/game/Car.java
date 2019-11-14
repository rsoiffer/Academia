package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.util.Noise;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.restructure.AssimpLoader;
import hero.graphics.renderables.ColorModel;
import hero.graphics.renderables.DiffuseModel;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;

import java.util.Random;

import static beige_engine.engine.Core.dt;
import static beige_engine.graphics.Camera.camera3d;

public class Car extends Behavior {

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);

    @Override
    public void createInner() {
        physics.acceleration = new Vec3d(0, 0, -10);
        var carModel = AssimpLoader.load("volkswagen/volkswagen-touareg.obj");
        var rot = Quaternion.fromEulerAngles(Math.PI, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, -1), rot, .001);
//        var carModel = AssimpLoader.load("lamborghini/lamborghini-aventador-pbribl.obj");
//        var rot = Quaternion.fromEulerAngles(Math.PI, 0, Math.PI / 2);
//        var trans = Transformation.create(new Vec3d(0, 0, -1), rot, 1);
        for (var m : carModel.meshes) {
            var car = m.buildRenderable();
            if (car == null) continue;
            var carRB = RenderableBehavior.createRB(car);
            if (m.material.texture == null) {
                 carRB.beforeRender = () -> ((ColorModel) car).t = pose.getTransform().mul(trans);
            } else {
                 carRB.beforeRender = () -> ((DiffuseModel) car).t = pose.getTransform().mul(trans);
            }
        }
    }

    private Noise noise = new Noise(new Random());
    private double time = 0;

    public void step() {
        time += dt();
        var dir = camera3d.position.sub(pose.position).setZ(0);
        physics.applyForce(dir.setLength(1000 * (noise.noise2d(time, 0, 1) + 1)), physics.centerOfMass.get());
        pose.rotation = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1).cross(dir));

        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
    }
}
