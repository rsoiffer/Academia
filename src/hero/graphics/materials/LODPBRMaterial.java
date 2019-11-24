package hero.graphics.materials;

import static beige_engine.graphics.opengl.GLObject.bindAll;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;
import static hero.graphics.VertexAttrib.*;
import static hero.graphics.passes.GeometryPass.SHADER_PBR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW_ALPHA;
import hero.graphics.utils.PBRTexture;
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
