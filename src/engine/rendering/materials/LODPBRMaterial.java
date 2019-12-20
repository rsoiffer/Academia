package engine.rendering.materials;

import static engine.graphics.opengl.GLObject.bindAll;
import engine.rendering.Mesh;
import engine.rendering.Renderable;
import engine.rendering.VertexAttrib;
import static engine.rendering.VertexAttrib.*;
import static engine.rendering.passes.GeometryPass.SHADER_PBR;
import static engine.rendering.passes.ShadowPass.SHADER_SHADOW;
import static engine.rendering.passes.ShadowPass.SHADER_SHADOW_ALPHA;
import engine.rendering.utils.PBRTexture;
import java.util.Arrays;
import java.util.List;

public class LODPBRMaterial extends Material<Mesh> {

    public PBRTexture tex = null;
    public boolean hasShadows = true;

    public static LODPBRMaterial load(String folder) {
        var m = new LODPBRMaterial();
        m.tex = PBRTexture.loadFromFolder(folder);
        return m;
    }

    @Override
    public Renderable buildRenderable(Mesh mesh) {
        return new LODPBRRenderable(mesh);
    }

    public class LODPBRRenderable extends LODRenderable {

        public LODPBRRenderable(Mesh mesh) {
            super(mesh);
        }

        @Override
        protected List<VertexAttrib> attribs() {
            return Arrays.asList(POSITIONS, TEX_COORDS, NORMALS, TANGENTS, BITANGENTS);
        }

        @Override
        public void renderGeom() {
            bindAll(SHADER_PBR, tex);
            drawModel();
        }

        @Override
        public void renderShadow() {
            if (hasShadows) {
                if (tex.hasAlpha()) {
                    bindAll(SHADER_SHADOW_ALPHA, tex);
                } else {
                    bindAll(SHADER_SHADOW);
                }
                drawModel();
            }
        }
    }
}
