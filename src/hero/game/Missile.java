package hero.game;

import engine.core.AbstractEntity;
import static engine.core.Core.dt;
import engine.samples.Behavior;
import engine.util.math.MathUtils;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import hero.game.particles.ParticleTypes;
import static hero.game.particles.ParticleTypes.FIRE;
import static hero.game.particles.ParticleTypes.SMOKE;
import hero.graphics.loading.AssimpLoader;
import hero.physics.PhysicsBehavior;
import hero.physics.PhysicsManager;
import hero.physics.PoseBehavior;
import java.util.Random;
import java.util.function.Function;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class Missile extends Behavior {

    public final PoseBehavior pose;
    public final PhysicsBehavior physics;
    public final ModelBehavior model;

    public final Function<Missile, Vec3d> targeter;

    public Vec3d pointing;
    public double time;
    public boolean isFriendly;
    public double lifetime;

    public Missile(Vec3d position, PhysicsManager manager, Function<Missile, Vec3d> targeter) {
        pose = add(new PoseBehavior(this, position));
        physics = add(new PhysicsBehavior(this, manager));
        model = add(new ModelBehavior(this));

        this.targeter = targeter;

        var mass = new DxMass();
        mass.setSphereTotal(5, .2);
        physics.setMass(mass);

        var geom = dCreateSphere(physics.manager.space, .2);
        physics.setGeom(geom);

        model.node.addChild(AssimpLoader.load("bomb/mk83.obj").rootNode);
        lifetime = 30;

        pointing = targeter.apply(this).normalize();
    }

    @Override
    public void onDestroy() {
//        ParticleTypes.explosion(pose.position, physics.velocity().div(2), 1000);
        ParticleTypes.explosion(pose.position, new Vec3d(0, 0, 0), 1000);
    }

    @Override
    public void onStep() {
        var rot = Quaternion.fromEulerAngles(Math.PI, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, 0), rot, .05);
        model.node.transform = pose.getTransform().mul(trans);

        time += dt();
        if (time > lifetime) {
            destroy();
            return;
        }

        var force = 200;
        if (targeter.apply(this) != null) {
            var dir = targeter.apply(this).normalize().lerp(physics.velocity().normalize(), -1).normalize();

            var angle = Math.acos(pointing.dot(dir));
            if (angle > 1e-6) {
                var quat = Quaternion.fromAngleAxis(Math.min(angle, 2 * dt()), pointing.cross(dir));
                pointing = quat.applyTo(pointing).normalize();
            }
            force *= .5 + Math.max(0, pointing.dot(dir));
        }

        physics.applyForce(new Vec3d(0, 0, 9.81 * physics.getMass()));
        physics.applyForce(pointing.mul(force));
        pose.rotation = Quaternion.fromXYAxes(pointing, new Vec3d(0, 0, 1).cross(pointing));

//        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
        if (!physics.hit.isEmpty()) {
            destroy();
            return;
        }

        if (time > .05) {
            if (isFriendly) {
                for (var d : AbstractEntity.getAll(Drone.class)) {
                    if (d.pose.position.sub(pose.position).length() < 5) {
                        d.destroy();
                        destroy();
                        return;
                    }
                }
            } else {
                for (var p : AbstractEntity.getAll(Player.class)) {
                    if (p.pose.position.sub(pose.position).length() < 2) {
                        destroy();
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < dt() * force - Math.random(); i++) {
            var timeInPast = Math.random() * dt();
            var p = FIRE.addParticle();
            p.position = pose.position.sub(physics.velocity().mul(timeInPast));
            p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + 4 * p.time)));
            p.velocity = physics.velocity().add(pointing.mul(-10).add(MathUtils.randomInSphere(new Random())).mul(5));
            p.update(timeInPast);
        }
        for (int i = 0; i < dt() * force / 40 - Math.random(); i++) {
            var timeInPast = Math.random() * dt();
            var p = SMOKE.addParticle();
            p.position = pose.position.sub(physics.velocity().mul(timeInPast));
            p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + .4 * p.time)));
            p.velocity = physics.velocity().add(pointing.mul(-10).add(MathUtils.randomInSphere(new Random())).mul(5));
            p.update(timeInPast);
        }
    }
}
