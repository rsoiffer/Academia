package hero.graphics.renderables;

import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.Transformation;
import hero.graphics.models.Model;

import static beige_engine.graphics.opengl.GLObject.bindAll;
import static hero.graphics.passes.GeometryPass.SHADER_DIFFUSE;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;

public class DiffuseModel extends Renderable {

    public Model model;
    public Texture tex;
    public double metallic = 0;
    public double roughness = .8;
    public boolean enableShadows = true;

    public DiffuseModel(Model model, Texture tex) {
        this.model = model;
        this.tex = tex;
    }

    @Override
    public void renderGeomInner(Transformation t) {
        bindAll(SHADER_DIFFUSE, tex);
        SHADER_DIFFUSE.setUniform("metallic", (float) metallic);
        SHADER_DIFFUSE.setUniform("roughness", (float) roughness);
        setTransform(t);
        model.render();
    }

    @Override
    public void renderShadowInner(Transformation t) {
        if (enableShadows) {
            SHADER_SHADOW.bind();
            setTransform(t);
            model.render();
        }
    }
}
