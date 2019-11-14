package hero.graphics.renderables;

import hero.graphics.models.Model;
import static hero.graphics.passes.GeometryPass.SHADER_COLOR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import java.util.LinkedList;
import java.util.List;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;

public class ColorModelParticles extends Renderable {

    public Model model;
    public List<Transformation> transforms = new LinkedList();
    public Vec3d color = new Vec3d(1, 1, 1);
    public double metallic = 0;
    public double roughness = .8;
    public boolean renderShadow = false;

    public ColorModelParticles(Model model) {
        this.model = model;
    }

    @Override
    public void renderGeomInner(Transformation t) {
        SHADER_COLOR.bind();
        SHADER_COLOR.setUniform("color", color);
        SHADER_COLOR.setUniform("metallic", (float) metallic);
        SHADER_COLOR.setUniform("roughness", (float) roughness);
        for (Transformation t2 : transforms) {
            setTransform(t.mul(t2));
            model.render();
        }
    }

    @Override
    public void renderShadowInner(Transformation t) {
        if (renderShadow) {
            SHADER_SHADOW.bind();
            for (Transformation t2 : transforms) {
                setTransform(t.mul(t2));
                model.render();
            }
        }
    }
}
