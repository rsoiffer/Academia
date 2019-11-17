package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import hero.graphics.renderables.ColorModelParticles;
import hero.graphics.restructure.Mesh;
import hero.graphics.restructure.ModelNode;
import hero.graphics.restructure.loading.VoxelModelLoader;
import hero.graphics.restructure.materials.ColorParticlesMaterial;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static beige_engine.engine.Core.dt;
import static hero.game.Player.POSTPHYSICS;

public class Thruster extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public List<Particle> particles = new LinkedList();
    public ColorParticlesMaterial material;

    @Override
    public void createInner() {
        material = new ColorParticlesMaterial();
        material.color = new Vec3d(1, 0, 0);
        material.hasShadows = false;
        var node = new ModelNode(new Mesh(VoxelModelLoader.load("fireball.vox").rawMesh, material));
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
                Vec3d vel = pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5);
                particles.add(new Particle(
                        controller.pos().add(vel.mul(.002 + dt() * Math.random())),
                        controller.player.physics.velocity.add(vel)));
            }
        }

        particles.removeIf(p -> p.time > .2);
        material.particles = particles.stream().map(Particle::transform).collect(Collectors.toList());
    }

    public static class Particle {

        public final Vec3d position, velocity;
        public double time = 0;

        public Particle(Vec3d position, Vec3d velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        public Transformation transform() {
            return Transformation.create(position.add(velocity.mul(time)).sub(1 / 8.), Quaternion.IDENTITY, 1 / 32.);
        }
    }
}
