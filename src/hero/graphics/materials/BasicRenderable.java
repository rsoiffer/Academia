package hero.graphics.materials;

import beige_engine.util.math.Transformation;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.drawables.Drawable;
import hero.graphics.VertexAttrib;
import hero.graphics.drawables.DrawableSupplier;

import java.util.List;

public abstract class BasicRenderable implements Renderable {

    private final Drawable d;
    private Transformation t;

    public BasicRenderable(DrawableSupplier ds) {
        d = ds.getDrawable(attribs());
    }

    protected abstract List<VertexAttrib> attribs();

    public void drawModel() {
        d.draw(t);
    }

    public void render(Transformation t, int pass) {
        this.t = t;
        switch (pass) {
            case 0:
                renderGeom();
                break;
            case 1:
                renderShadow();
                break;
            case 2:
                renderEmissive();
                break;
        }
    }

    public void renderGeom() {}
    public void renderShadow() {}
    public void renderEmissive() {}
}
