package hero.game.particles;

import static beige_engine.core.Core.dt;
import beige_engine.samples.Behavior;
import hero.game.ModelBehavior;
import hero.graphics.Platonics;
import hero.graphics.drawables.DrawableSupplier;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.materials.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParticleEmitter extends Behavior {

    public final ModelBehavior model = new ModelBehavior(this);

    public Material<DrawableSupplier> material;
    public Consumer<Particle> archetype;

    private final List<Particle> particles = new ArrayList<>();

    public Particle addParticle() {
        var p = new Particle();
        archetype.accept(p);
        particles.add(p);
        return p;
    }

    public ParticleEmitter() {
        var pds = new ParticlesDS(Platonics.square, () -> particles.stream().map(Particle::getTransform));
        model.node.addChild(material.buildRenderable(pds));
    }

    @Override
    public void onStep() {
        particles.forEach(p -> p.update(dt()));
        particles.removeIf(p -> !p.alive);
    }
}
