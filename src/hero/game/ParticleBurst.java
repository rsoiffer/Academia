package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.loading.VoxelModelLoader;
import hero.graphics.materials.ColorParticlesMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static beige_engine.engine.Core.dt;

public class ParticleBurst extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);

    public List<Particle> particles = new ArrayList<>();
    public double fadeTime = .1;
    public ColorParticlesMaterial material;

    public void createInner() {
        material = new ColorParticlesMaterial();
        material.color = new Vec3d(1, 0, 0);
        material.hasShadows = false;
        var renderable = material.buildRenderable(VoxelModelLoader.load("fireball.vox").mesh);
        model.node.addChild(renderable);
    }

    public void step() {
        particles.forEach(p -> p.time += dt());
        particles.removeIf(p -> Math.random() < dt() / fadeTime);
        material.particles = particles.stream().map(Particle::transform).collect(Collectors.toList());
        if (particles.isEmpty()) {
            destroy();
        }
    }

    public static class Particle {

        private final Vec3d position, velocity;
        public double time = 0;

        public Particle(Vec3d position, Vec3d velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        public Transformation transform() {
            double scale = 1 / 32.;
            return Transformation.create(position.add(velocity.mul(time)).sub(4 * scale), Quaternion.IDENTITY, scale);
        }
    }
}
