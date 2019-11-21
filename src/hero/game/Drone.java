package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.util.Noise;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.game.particles.ParticleTypes;
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

    public double maxHealth = 100;
    public double health = 10;

    private Noise noise = new Noise(new Random());
    private double time = 0;

    @Override
    public void createInner() {
        physics.acceleration = new Vec3d(0, 0, 2);
        modelNode.node.addChild(AssimpLoader.load("drone model/05.fbx").rootNode);
        var rot = Quaternion.fromEulerAngles(-Math.PI / 2, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, 0), rot, .01);
        modelNode.beforeRender = () -> modelNode.node.transform = pose.getTransform().mul(trans);
    }

    @Override
    public void destroyInner() {
        ParticleTypes.explosion(pose.position, physics.velocity, 1000);
    }

    public void step() {
        time += dt();
        var dir = camera3d.position.sub(pose.position);
        physics.applyForce(dir.setLength(1000 * (noise.noise2d(time, 0, 1) + 1)), physics.centerOfMass.get());
        pose.rotation = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1).cross(dir));

        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));

        health = Math.min(maxHealth, health + dt());
        health -= physics.collisionVel.div(1).lengthSquared();
        if (health <= 0) {
            destroy();
        }

//        int numParts = floor(physics.collisionVel.lengthSquared());
//        if (numParts > 0) {
//            var pb = new ParticleBurst();
//            var r = new Random();
//            for (int i = 0; i < numParts; i++) {
//                var pos = pose.position;
//                var vel = physics.velocity.add(MathUtils.randomInSphere(r).mul(10 + Math.random() * 10));
//                pb.particles.add(new Particle(pos, vel));
//            }
//            pb.create();
//            pb.material.color = new Vec3d(.8, .8, .8);
//            pb.material.emissive = new Vec3d(0, 0, 0);
//        }
    }
}
