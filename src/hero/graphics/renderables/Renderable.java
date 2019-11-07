package hero.graphics.renderables;

import beige_engine.graphics.opengl.GLState;
import org.joml.Matrix4d;
import beige_engine.util.math.Transformation;

public abstract class Renderable {

    Renderable() {
    }

    public abstract void renderGeom();

    public abstract void renderShadow();

    void setTransform(Matrix4d m) {
        GLState.getShaderProgram().setUniform("model", m);
    }

    void setTransform(Transformation t) {
        setTransform(t.matrix());
    }
}
