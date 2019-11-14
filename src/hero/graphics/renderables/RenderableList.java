package hero.graphics.renderables;

import beige_engine.util.math.Transformation;

import java.util.Arrays;

public class RenderableList extends Renderable {

    private final Iterable<Renderable> renderables;

    public RenderableList(Iterable<Renderable> renderables) {
        this.renderables = renderables;
    }

    public RenderableList(Renderable... renderables) {
        this(Arrays.asList(renderables));
    }

    @Override
    public void renderGeomInner(Transformation t) {
        renderables.forEach(r -> r.renderGeom(t));
    }

    @Override
    public void renderShadowInner(Transformation t) {
        renderables.forEach(r -> r.renderShadow(t));
    }
}
