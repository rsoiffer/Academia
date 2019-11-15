package hero.graphics.renderables;

import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.models.Model;

import static hero.graphics.passes.GeometryPass.SHADER_COLOR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;

public class ColorModel extends Renderable {

    public Model model;
    public Vec3d color = new Vec3d(1, 1, 1);
    public double metallic = 0;
    public double roughness = .8;

    public ColorModel(Model model) {
        this.model = model;
    }

    @Override
    public void renderGeomInner(Transformation t) {
        SHADER_COLOR.bind();
        SHADER_COLOR.setUniform("color", color);
        SHADER_COLOR.setUniform("metallic", (float) metallic);
        SHADER_COLOR.setUniform("roughness", (float) roughness);
        setTransform(t);
        model.render();
    }

    @Override
    public void renderShadowInner(Transformation t) {
        SHADER_SHADOW.bind();
        setTransform(t);
        model.render();
    }
}
