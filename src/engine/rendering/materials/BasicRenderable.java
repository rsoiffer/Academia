package engine.rendering.materials;

import engine.util.math.Transformation;
import engine.rendering.Renderable;
import engine.rendering.drawables.Drawable;
import engine.rendering.VertexAttrib;
import engine.rendering.drawables.DrawableSupplier;

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

    public void renderGeom() {
    }

    public void renderShadow() {
    }

    public void renderEmissive() {
    }
}
