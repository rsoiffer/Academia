package hero.graphics.renderables;

import beige_engine.util.math.Transformation;
import hero.graphics.PBRTexture;
import hero.graphics.models.Model;

import static beige_engine.graphics.opengl.GLObject.bindAll;
import static hero.graphics.passes.GeometryPass.SHADER_PBR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW_ALPHA;

public class PBRModel extends Renderable {

    public Model model;
    public PBRTexture tex;

    public PBRModel(Model model, PBRTexture tex) {
        this.model = model;
        this.tex = tex;
    }

    @Override
    public void renderGeomInner(Transformation t) {
        bindAll(SHADER_PBR, tex);
        setTransform(t);
        SHADER_PBR.setUniform("lod", 0f);
        model.render();
    }

    @Override
    public void renderShadowInner(Transformation t) {
        if (tex.hasAlpha()) {
            bindAll(SHADER_SHADOW_ALPHA, tex);
            SHADER_SHADOW_ALPHA.setUniform("lod", 0f);
        } else {
            bindAll(SHADER_SHADOW);
        }
        setTransform(t);
        model.render();
    }
}
