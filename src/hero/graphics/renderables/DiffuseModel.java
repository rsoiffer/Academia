package hero.graphics.renderables;

import hero.graphics.models.Model;
import static beige_engine.graphics.opengl.GLObject.bindAll;
import beige_engine.graphics.opengl.Texture;
import static hero.graphics.passes.GeometryPass.SHADER_DIFFUSE;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import beige_engine.util.math.Transformation;

public class DiffuseModel extends Renderable {

    public Model model;
    public Texture tex;
    public Transformation t = Transformation.IDENTITY;
    public double metallic = 0;
    public double roughness = .8;
    public boolean enableShadows = true;

    public DiffuseModel(Model model, Texture tex) {
        this.model = model;
        this.tex = tex;
    }

    @Override
    public void renderGeom() {
        bindAll(SHADER_DIFFUSE, tex);
        SHADER_DIFFUSE.setUniform("metallic", (float) metallic);
        SHADER_DIFFUSE.setUniform("roughness", (float) roughness);
        setTransform(t);
        model.render();
    }

    @Override
    public void renderShadow() {
        if (enableShadows) {
            SHADER_SHADOW.bind();
            setTransform(t);
            model.render();
        }
    }
}
