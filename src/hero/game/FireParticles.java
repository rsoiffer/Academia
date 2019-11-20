package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.graphics.Camera;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.loading.RawMeshBuilder;
import hero.graphics.materials.ColorParticlesMaterial;
import hero.graphics.materials.EmissiveMaterial;
import hero.graphics.materials.EmissiveParticlesMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static beige_engine.engine.Core.dt;
import static hero.graphics.VertexAttrib.NORMALS;
import static hero.graphics.VertexAttrib.POSITIONS;

public class FireParticles extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);

    public List<Particle> particles = new ArrayList<>();
    public double startupTime = .1;
    public double fadeTime = .1;
    public EmissiveParticlesMaterial material;

    public void createInner() {
        material = new EmissiveParticlesMaterial();
        material.color = new Vec3d(2, .2, .1);
        var square = new RawMeshBuilder(POSITIONS, NORMALS)
                .addRectangle(new Vec3d(-.5, -.5, 0), new Vec3d(0, 1, 0), new Vec3d(0, 0, 1))
                .toMesh();
        model.node.addChild(material.buildRenderable(square));
    }

    public void step() {
        startupTime -= dt();
        particles.forEach(p -> p.time += dt());
        if (startupTime < 0) {
            particles.removeIf(p -> Math.random() < dt() / fadeTime);
        }
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
            var pos = position.add(velocity.mul(time));
            var dir = Camera.camera3d.position.sub(pos);
            var quat = Quaternion.fromXYAxes(dir, new Vec3d(0, 0, 1));
            return Transformation.create(pos, quat, .25 / (1 + 4 * time));
        }
    }
}
