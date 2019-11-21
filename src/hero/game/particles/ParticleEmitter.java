package hero.game.particles;

import beige_engine.engine.Behavior;
import hero.game.ModelBehavior;
import hero.graphics.Platonics;
import hero.graphics.drawables.DrawableSupplier;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.materials.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static beige_engine.engine.Core.dt;

public class ParticleEmitter extends Behavior {

    public final ModelBehavior model = require(ModelBehavior.class);

    public Material<DrawableSupplier> material;
    public Consumer<Particle> archetype;

    private final List<Particle> particles = new ArrayList<>();

    public Particle addParticle() {
        var p = new Particle();
        archetype.accept(p);
        particles.add(p);
        return p;
    }

    @Override
    public void createInner() {
        var pds = new ParticlesDS(Platonics.square, () -> particles.stream().map(Particle::getTransform));
        model.node.addChild(material.buildRenderable(pds));
    }

    @Override
    public void step() {
        particles.forEach(p -> p.update(dt()));
        particles.removeIf(p -> !p.alive);
    }
}
