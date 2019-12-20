package game.entities;

import static engine.core.Core.dt;
import static engine.graphics.Camera.camera3d;
import static engine.physics.DynamicShape.sphere;
import engine.physics.PhysicsComponent;
import engine.physics.PhysicsManager;
import engine.physics.PoseComponent;
import engine.rendering.ModelComponent;
import engine.samples.Behavior;
import engine.util.Noise;
import engine.util.Resources;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import game.particles.ParticleTypes;
import java.util.Random;

public class Drone extends Behavior {

    public final PoseComponent pose;
    public final PhysicsComponent physics;
    public final ModelComponent model;

    public double missileTimer = 4;

    private Noise noise = new Noise(new Random());
    private double time = 0;

    public Drone(Vec3d position, PhysicsManager manager) {
        pose = add(new PoseComponent(this, position));
        physics = add(new PhysicsComponent(this, manager, sphere(1, 100)));
        model = add(new ModelComponent(this));
        model.node.addChild(Resources.loadAssimpModel("drone model/optimized.fbx"));
    }

    @Override
    public void onDestroy() {
        ParticleTypes.explosion(pose.position, physics.velocity().div(2), 1000);
    }

    @Override
    public void onStep() {
        var rot = Quaternion.fromEulerAngles(-Math.PI / 2, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, 0), rot, .02);
        model.node.transform = pose.getTransform().mul(trans);

        time += dt();
        var dir = camera3d.position.sub(pose.position);
        physics.applyForce(new Vec3d(0, 0, 9.81 * physics.getMass()));
        physics.applyForce(dir.setLength(100 * (noise.noise2d(time, 0, 1) + 1)));
        pose.rotation = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1).cross(dir));

//        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
        missileTimer -= dt();
        if (missileTimer < 0) {
            missileTimer = 1 + 2 * Math.random();

            var m = new Missile(pose.position, physics.manager, m2 -> camera3d.position.sub(m2.pose.position));
            m.physics.ignore.add(physics);
            m.physics.setVelocity(physics.velocity());
        }
    }
}
