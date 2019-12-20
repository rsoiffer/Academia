package hero.graphics.materials;

import static engine.graphics.opengl.GLObject.bindAll;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;
import static hero.graphics.VertexAttrib.*;
import hero.graphics.drawables.DrawableSupplier;
import static hero.graphics.passes.GeometryPass.SHADER_PBR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW_ALPHA;
import hero.graphics.utils.PBRTexture;
import java.util.Arrays;
import java.util.List;

public class PBRMaterial extends Material {

    public PBRTexture tex = null;

    public static PBRMaterial load(String folder) {
        var m = new PBRMaterial();
        m.tex = PBRTexture.loadFromFolder(folder);
        return m;
    }

    @Override
    public Renderable buildRenderable(DrawableSupplier mesh) {
        return new PBRRenderable(mesh);
    }

    public class PBRRenderable extends BasicRenderable {

        public PBRRenderable(DrawableSupplier mesh) {
            super(mesh);
        }

        @Override
        protected List<VertexAttrib> attribs() {
            return Arrays.asList(POSITIONS, TEX_COORDS, NORMALS, TANGENTS, BITANGENTS);
        }

        @Override
        public void renderGeom() {
            bindAll(SHADER_PBR, tex);
            SHADER_PBR.setUniform("lod", 0f);
            drawModel();
        }

        @Override
        public void renderShadow() {
            if (tex.hasAlpha()) {
                bindAll(SHADER_SHADOW_ALPHA, tex);
                SHADER_SHADOW_ALPHA.setUniform("lod", 0f);
            } else {
                bindAll(SHADER_SHADOW);
            }
            drawModel();
        }
    }
}
