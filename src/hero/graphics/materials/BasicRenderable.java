package hero.graphics.materials;

import beige_engine.graphics.opengl.GLState;
import beige_engine.util.math.Transformation;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.VAOWrapper;
import hero.graphics.VertexAttrib;

import java.util.List;

public abstract class BasicRenderable implements Renderable {

    private final VAOWrapper model;
    private Transformation t;

    public BasicRenderable(Mesh mesh) {
        this.model = mesh.getVAOW(attribs());
    }

    protected abstract List<VertexAttrib> attribs();

    protected void drawModel() {
        GLState.getShaderProgram().setUniform("model", t.matrix());
        model.draw();
    }

    protected void drawModelOffset(Transformation t2) {
        GLState.getShaderProgram().setUniform("model", t.mul(t2).matrix());
        model.draw();
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
