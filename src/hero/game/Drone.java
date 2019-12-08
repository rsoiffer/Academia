package hero.game;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import static beige_engine.graphics.Camera.camera3d;
import beige_engine.util.Noise;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.game.particles.ParticleTypes;
import hero.graphics.loading.AssimpLoader;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;
import java.util.Collection;
import java.util.Random;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class Drone extends Behavior {

    public static final Collection<Drone> ALL = track(Drone.class);

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);

    public double missileTimer = 4;

    private Noise noise = new Noise(new Random());
    private double time = 0;

    @Override
    public void createInner() {
        var mass = new DxMass();
        mass.setSphereTotal(100, 1);
        physics.setMass(mass);

        var geom = dCreateSphere(physics.manager.space, 1);
        physics.setGeom(geom);

        model.node.addChild(AssimpLoader.load("drone model/optimized.fbx").rootNode);
        var rot = Quaternion.fromEulerAngles(-Math.PI / 2, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, 0), rot, .02);
        model.beforeRender = () -> model.node.transform = pose.getTransform().mul(trans);
    }

    @Override
    public void destroyInner() {
        ParticleTypes.explosion(pose.position, physics.velocity().div(2), 1000);
    }

    @Override
    public void step() {
        time += dt();
        var dir = camera3d.position.sub(pose.position);
        physics.applyForce(new Vec3d(0, 0, 9.81 * physics.getMass()));
        physics.applyForce(dir.setLength(100 * (noise.noise2d(time, 0, 1) + 1)));
        pose.rotation = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1).cross(dir));

//        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
        missileTimer -= dt();
        if (missileTimer < 0) {
            missileTimer = 1 + 2 * Math.random();

            Missile m = new Missile();
            m.pose.position = pose.position;
            m.physics.manager = physics.manager;
            m.physics.ignore.add(physics);
            m.targetDir = () -> camera3d.position.sub(m.pose.position);
            m.create();
            m.physics.setVelocity(physics.velocity());
        }
    }
}
