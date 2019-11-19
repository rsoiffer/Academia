package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.util.Noise;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.loading.AssimpLoader;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;

import java.util.Random;

import static beige_engine.engine.Core.dt;
import static beige_engine.graphics.Camera.camera3d;

public class Drone extends Behavior {

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final ModelBehavior modelNode = require(ModelBehavior.class);

    private Noise noise = new Noise(new Random());
    private double time = 0;

    @Override
    public void createInner() {
        physics.acceleration = new Vec3d(0, 0, .1);
        modelNode.node.addChild(AssimpLoader.load("drone model/05.fbx").rootNode);
        var rot = Quaternion.fromEulerAngles(-Math.PI / 2, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, 0), rot, .01);
        modelNode.beforeRender = () -> modelNode.node.transform = pose.getTransform().mul(trans);
    }

    public void step() {
        time += dt();
        var dir = camera3d.position.sub(pose.position);
        physics.applyForce(dir.setLength(1000 * (noise.noise2d(time, 0, 1) + 1)), physics.centerOfMass.get());
        pose.rotation = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1).cross(dir));

        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
    }
}
