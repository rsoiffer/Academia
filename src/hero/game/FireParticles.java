package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.graphics.Camera;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.Platonics;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.materials.EmissiveMaterial;

import java.util.ArrayList;
import java.util.List;

import static beige_engine.engine.Core.dt;

public class FireParticles extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);

    public List<Particle> particles = new ArrayList<>();
    public double startupTime = .1;
    public double fadeTime = .1;
    public boolean destroyOnEmpty = false;

    public void createInner() {
        var material = new EmissiveMaterial();
        material.color = new Vec3d(5, .5, .2);
        var pds = new ParticlesDS(Platonics.square, () -> particles.stream().map(Particle::transform));
        model.node.addChild(material.buildRenderable(pds));
    }

    public void step() {
        startupTime -= dt();
        particles.forEach(p -> p.time += dt());
        if (startupTime < 0) {
            particles.removeIf(p -> Math.random() < dt() / fadeTime);
        }
        if (destroyOnEmpty && particles.isEmpty()) {
            destroy();
        }
    }

    public static class Particle {

        private final Vec3d position, velocity;
        private final double angle = Math.random() * 2 * Math.PI;
        public double time = 0;

        public Particle(Vec3d position, Vec3d velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        public Transformation transform() {
            var pos = position.add(velocity.mul(time));
            var dir = Camera.current.getPos().sub(pos);
            var up = new Vec3d(0, 0, 1);
            var quat = Quaternion.fromXYAxes(up.cross(dir), dir.cross(up.cross(dir)));
            quat = quat.mul(Quaternion.fromAngleAxis(new Vec3d(0, 0, angle)));
            return Transformation.create(pos, quat, Math.min(10 * time, .25 / (1 + 4 * time)));
        }
    }
}
