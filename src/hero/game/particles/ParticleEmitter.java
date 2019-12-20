package hero.game.particles;

import static engine.core.Core.dt;
import engine.samples.Behavior;
import hero.game.ModelBehavior;
import hero.graphics.Platonics;
import hero.graphics.drawables.DrawableSupplier;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.materials.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParticleEmitter extends Behavior {

    public final ModelBehavior model = add(new ModelBehavior(this));

    public Consumer<Particle> archetype;

    private final List<Particle> particles = new ArrayList<>();

    public ParticleEmitter(Material<DrawableSupplier> material) {
        var pds = new ParticlesDS(Platonics.square, () -> particles.stream().map(Particle::getTransform));
        model.node.addChild(material.buildRenderable(pds));
    }

    public Particle addParticle() {
        var p = new Particle();
        archetype.accept(p);
        particles.add(p);
        return p;
    }

    @Override
    public void onStep() {
        particles.forEach(p -> p.update(dt()));
        particles.removeIf(p -> !p.alive);
    }
}
