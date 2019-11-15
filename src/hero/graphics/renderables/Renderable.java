package hero.graphics.renderables;

import beige_engine.graphics.opengl.GLState;
import beige_engine.util.math.Transformation;
import org.joml.Matrix4d;

public abstract class Renderable {

    public Transformation t = Transformation.IDENTITY;

    Renderable() {
    }

    public void renderGeom(Transformation t) {
        renderGeomInner(t.mul(this.t));
    }

    public abstract void renderGeomInner(Transformation t);

    public void renderShadow(Transformation t) {
        renderShadowInner(t.mul(this.t));
    }

    public abstract void renderShadowInner(Transformation t);

    void setTransform(Matrix4d m) {
        GLState.getShaderProgram().setUniform("model", m);
    }

    void setTransform(Transformation t) {
        setTransform(t.matrix());
    }
}
