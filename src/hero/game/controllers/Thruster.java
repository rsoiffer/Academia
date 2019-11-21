package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import hero.game.FireParticles;
import hero.game.FireParticles.Particle;
import hero.graphics.ModelNode;
import hero.graphics.loading.VoxelModelLoader;
import hero.graphics.materials.ColorParticlesMaterial;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static beige_engine.engine.Core.dt;
import static hero.game.Player.POSTPHYSICS;

public class Thruster extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);
    public final FireParticles fireParticles = require(FireParticles.class);

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
            for (int i = 0; i < 10000 * t * dt(); i++) {
                var timeInPast = Math.random() * dt();
                var vel = pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5);
                var p = new Particle(
                        controller.pos().sub(controller.player.physics.velocity.mul(timeInPast)),
                        controller.player.physics.velocity.add(vel));
                p.time = timeInPast;
                fireParticles.particles.add(p);
            }
        }
    }
}
