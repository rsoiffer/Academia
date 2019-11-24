package hero.game;

import beige_engine.behaviors.LifetimeBehavior;
import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.game.particles.ParticleTypes;
import static hero.game.particles.ParticleTypes.FIRE;
import static hero.game.particles.ParticleTypes.SMOKE;
import hero.graphics.loading.AssimpLoader;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;
import java.util.Random;
import java.util.function.Supplier;

public class Missile extends Behavior {

    public final PoseBehavior pose = require(PoseBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final ModelBehavior model = require(ModelBehavior.class);
    public final LifetimeBehavior lifetime = require(LifetimeBehavior.class);

    public Vec3d pointing;
    public Supplier<Vec3d> targetDir;
    public double time;
    public boolean isFriendly;

    @Override
    public void createInner() {
        pointing = targetDir.get().normalize();
        physics.acceleration = new Vec3d(0, 0, -2);
        physics.radius = .2;
        physics.mass = 5;
        model.node.addChild(AssimpLoader.load("bomb/mk83.obj").rootNode);
        var rot = Quaternion.fromEulerAngles(Math.PI, 0, Math.PI / 2);
        var trans = Transformation.create(new Vec3d(0, 0, 0), rot, .05);
        model.beforeRender = () -> model.node.transform = pose.getTransform().mul(trans);
        lifetime.lifetime = 30;
    }

    @Override
    public void destroyInner() {
        ParticleTypes.explosion(pose.position, physics.velocity.div(2), 1000);
    }

    public void step() {
        time += dt();
        var force = 200;
        if (targetDir.get() != null) {
            var dir = targetDir.get().normalize().lerp(physics.velocity.normalize(), -1).normalize();

            var angle = Math.acos(pointing.dot(dir));
            if (angle > 1e-6) {
                var quat = Quaternion.fromAngleAxis(Math.min(angle, 2 * dt()), pointing.cross(dir));
                pointing = quat.applyTo(pointing).normalize();
            }
            force *= .5 + Math.max(0, pointing.dot(dir));
        }

        physics.applyForce(pointing.mul(force), physics.centerOfMass.get());
        pose.rotation = Quaternion.fromXYAxes(pointing, new Vec3d(0, 0, 1).cross(pointing));

//        physics.velocity = physics.velocity.mul(Math.exp(-dt() * .1));
        if (physics.collisionVel.length() > .1) {
            destroy();
        }

        if (time > .05) {
            if (isFriendly) {
                for (var d : Drone.ALL) {
                    if (d.pose.position.sub(pose.position).length() < 5) {
                        d.destroy();
                        destroy();
                        break;
                    }
                }
            } else {
                for (var p : Player.ALL) {
                    if (p.pose.position.sub(pose.position).length() < 2) {
                        destroy();
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < dt() * force - Math.random(); i++) {
            var timeInPast = Math.random() * dt();
            var p = FIRE.addParticle();
            p.position = pose.position.sub(physics.velocity.mul(timeInPast));
            p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + 4 * p.time)));
            p.velocity = physics.velocity.add(pointing.mul(-10).add(MathUtils.randomInSphere(new Random())).mul(5));
            p.update(timeInPast);
        }
        for (int i = 0; i < dt() * force / 40 - Math.random(); i++) {
            var timeInPast = Math.random() * dt();
            var p = SMOKE.addParticle();
            p.position = pose.position.sub(physics.velocity.mul(timeInPast));
            p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + .4 * p.time)));
            p.velocity = physics.velocity.add(pointing.mul(-10).add(MathUtils.randomInSphere(new Random())).mul(5));
            p.update(timeInPast);
        }
    }
}
