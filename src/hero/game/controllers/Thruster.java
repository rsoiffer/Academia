package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;

import java.util.Random;

import static beige_engine.engine.Core.dt;
import static hero.game.Player.POSTPHYSICS;
import static hero.game.particles.ParticleTypes.FIRE;
import static hero.game.particles.ParticleTypes.SMOKE;

public class Thruster extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
        double t = controller.controller.trigger();
        if (t > .1) {
            Vec3d pullDir = controller.sideways();
            if (controller.controller == Vive.LEFT) {
                pullDir = pullDir.mul(-1);
            }
//            controller.player.applyForce(pullDir.mul(t * -10), .03);
            controller.player.physics.applyForce(pullDir.mul(t * -1000), controller.pos());
//            double pullStrength = Math.exp(.02 * pullDir.dot(controller.player.velocity.velocity));
//            controller.player.velocity.velocity = controller.player.velocity.velocity.add(pullDir.mul(dt() * t * pullStrength * -10));
            for (int i = 0; i < 1000 * t * dt(); i++) {
                var timeInPast = Math.random() * dt();
                var p = FIRE.addParticle();
                p.position = controller.pos().sub(controller.player.physics.velocity.mul(timeInPast));
                p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + 4 * p.time)));
                p.velocity = controller.player.physics.velocity.add(pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5));
                p.update(timeInPast);
            }
            for (int i = 0; i < 1000 * t * dt() / 40; i++) {
                var timeInPast = Math.random() * dt();
                var p = SMOKE.addParticle();
                p.position = controller.pos().sub(controller.player.physics.velocity.mul(timeInPast));
                p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + .4 * p.time)));
                p.velocity = controller.player.physics.velocity.add(pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5));
                p.update(timeInPast);
            }
        }
    }
}
