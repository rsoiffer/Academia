package hero.graphics.drawables;

import beige_engine.util.math.Transformation;
import hero.graphics.VertexAttrib;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ParticlesDS implements DrawableSupplier {

    private final DrawableSupplier ds;
    private final Supplier<Stream<Transformation>> transforms;

    public ParticlesDS(DrawableSupplier ds, Supplier<Stream<Transformation>> transforms) {
        this.ds = ds;
        this.transforms = transforms;
    }

    @Override
    public Drawable getDrawable(List<VertexAttrib> attribs) {
        var d = ds.getDrawable(attribs);
        return t -> transforms.get().map(t::mul).forEach(d::draw);
    }
}
