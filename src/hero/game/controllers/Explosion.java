package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import hero.game.controllers.Thruster.Particle;
import hero.graphics.ModelNode;
import hero.graphics.loading.VoxelModelLoader;
import hero.graphics.materials.ColorParticlesMaterial;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static beige_engine.engine.Core.dt;
import static beige_engine.vr.Vive.TRIGGER;
import static hero.game.Player.POSTPHYSICS;

public class Explosion extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public double charge;
    public List<Particle> particles = new LinkedList();
    public ColorParticlesMaterial material;

    @Override
    public void createInner() {
        material = new ColorParticlesMaterial();
        material.color = new Vec3d(1, 0, 0);
        material.hasShadows = false;
        var node = new ModelNode(material.buildRenderable(VoxelModelLoader.load("fireball.vox").mesh));
        controller.modelNode.node.addChild(node);
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
        for (Particle p : particles) {
            p.time += dt();
        }

        charge += dt();
        charge = Math.min(charge, 1.5);
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            Vec3d pullDir = controller.sideways();
            if (controller.controller == Vive.LEFT) {
                pullDir = pullDir.mul(-1);
            }

//            double exag = controller.player.velocity.velocity.dot(pullDir);
//            exag = Math.log(1 + Math.exp(.01 * exag));
//            Vec3d impulse = pullDir.mul(charge * -15 * exag);
//            controller.player.velocity.velocity = controller.player.velocity.velocity.add(impulse);
            controller.player.physics.applyImpulse(pullDir.mul(charge * -1000), controller.pos());

            for (int i = 0; i < 1000 * charge; i++) {
                particles.add(new Particle(controller.pos(), controller.player.physics.velocity.add(
                        pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5))));
            }
            charge = 0;
        }

        particles.removeIf(p -> Math.random() < 5 * dt());
        material.particles = particles.stream().map(Particle::transform).collect(Collectors.toList());
    }
}
