package engine.rendering.materials;

import static engine.graphics.opengl.GLObject.bindAll;
import engine.rendering.Renderable;
import engine.rendering.VertexAttrib;
import static engine.rendering.VertexAttrib.*;
import engine.rendering.drawables.DrawableSupplier;
import static engine.rendering.passes.GeometryPass.SHADER_PBR;
import static engine.rendering.passes.ShadowPass.SHADER_SHADOW;
import static engine.rendering.passes.ShadowPass.SHADER_SHADOW_ALPHA;
import engine.rendering.utils.PBRTexture;
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
