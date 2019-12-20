package hero.game;

import static engine.core.Core.dt;
import static engine.graphics.Camera.camera3d;
import engine.samples.Behavior;
import engine.util.Noise;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import hero.game.particles.ParticleTypes;
import hero.graphics.loading.AssimpLoader;
import hero.physics.PhysicsBehavior;
import hero.physics.PhysicsManager;
import hero.physics.PoseBehavior;
import java.util.Random;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class Drone extends Behavior {

    public final PoseBehavior pose;
    public final PhysicsBehavior physics;
    public final ModelBehavior model;

    public double missileTimer = 4;

    private Noise noise = new Noise(new Random());
    private double time = 0;

    public Drone(Vec3d position, PhysicsManager manager) {
        pose = add(new PoseBehavior(this, position));
        physics = add(new PhysicsBehavior(this, manager));
        model = add(new ModelBehavior(this));

        var mass = new DxMass();
        mass.setSphereTotal(100, 1);
        physics.setMass(mass);

        var geom = dCreateSphere(physics.manager.space, 1);
        physics.setGeom(geom);

        model.node.addChild(AssimpLoader.load("drone model/optimized.fbx").rootNode);
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
