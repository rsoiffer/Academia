package hero.graphics.renderables;

import beige_engine.graphics.Camera;
import hero.graphics.PBRTexture;
import hero.graphics.models.CustomModel;
import hero.graphics.models.ModelSimplifier3;
import static beige_engine.graphics.opengl.GLObject.bindAll;
import static hero.graphics.passes.GeometryPass.SHADER_PBR;
import hero.graphics.passes.RenderPipeline;
import hero.graphics.passes.ShadowPass;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW_ALPHA;
import java.util.ArrayList;
import java.util.List;
import static beige_engine.util.math.MathUtils.ceil;
import static beige_engine.util.math.MathUtils.clamp;
import static beige_engine.util.math.MathUtils.floor;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;

public class LODPBRModel extends Renderable {

    public int numLOD;
    public List<CustomModel> modelLODS;
    public PBRTexture tex;
    public boolean castShadow = true;

    public LODPBRModel(CustomModel model, PBRTexture tex, int numLOD) {
        this.numLOD = numLOD;
        modelLODS = new ArrayList();
        for (int i = 0; i < numLOD; i++) {
            if (i > 0) {
                model = ModelSimplifier3.simplify(model, .1 * Math.pow(2, i));
                model.createVAO();
            }
            modelLODS.add(model);
        }
        this.tex = tex;
    }

    public LODPBRModel(List<CustomModel> modelLODS, PBRTexture tex) {
        numLOD = modelLODS.size();
        this.modelLODS = modelLODS;
        this.tex = tex;
    }

    public LODPBRModel(LODPBRModel other) {
        numLOD = other.numLOD;
        modelLODS = other.modelLODS;
        tex = other.tex;
    }

    private double getLOD(Transformation t) {
        double minLOD = 0;
        if (RenderPipeline.currentPass instanceof ShadowPass) {
            minLOD = Math.log(1 - ((ShadowPass) RenderPipeline.currentPass).zMax) / Math.log(.2) - 3;
        }
        double estimatedDist = t.apply(new Vec3d(0, 0, 0)).sub(Camera.camera3d.position).length();
        return clamp(Math.log(estimatedDist) / Math.log(2) - 5, Math.max(0, minLOD), numLOD);
    }

    private double getLODFrac(Transformation t) {
        double lod = getLOD(t);
        return Math.max((lod - floor(lod)) * 10 - 9, 0);
    }

    @Override
    public void renderGeomInner(Transformation t) {
        double lod = getLOD(t), lodFrac = getLODFrac(t);
        if (lod < numLOD) {
            bindAll(SHADER_PBR, tex);
            SHADER_PBR.setUniform("lod", (float) lodFrac);
            setTransform(t);
            modelLODS.get(floor(lod)).render();
            if (lodFrac > 0 && ceil(lod) < numLOD) {
                SHADER_PBR.setUniform("lod", (float) lodFrac - 1);
                modelLODS.get(ceil(lod)).render();
            }
        }
    }

    @Override
    public void renderShadowInner(Transformation t) {
        if (castShadow) {
            double lod = getLOD(t), lodFrac = getLODFrac(t);
            if (lod < numLOD) {
                if (tex.hasAlpha()) {
                    bindAll(SHADER_SHADOW_ALPHA, tex);
                    SHADER_SHADOW_ALPHA.setUniform("lod", (float) lodFrac);
                    setTransform(t);
                    modelLODS.get(floor(lod)).render();
                    if (lodFrac > 0 && ceil(lod) < numLOD) {
                        SHADER_SHADOW_ALPHA.setUniform("lod", (float) lodFrac - 1);
                        modelLODS.get(ceil(lod)).render();
                    }
                } else {
                    bindAll(SHADER_SHADOW);
                    setTransform(t);
                    modelLODS.get(floor(lod)).render();
                }
            }
        }
    }
}
