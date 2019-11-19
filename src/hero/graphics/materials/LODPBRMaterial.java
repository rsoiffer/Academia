package hero.graphics.materials;

import hero.graphics.utils.PBRTexture;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.Renderable.LODRenderable;
import hero.graphics.VertexAttrib;

import java.util.Arrays;
import java.util.List;

import static beige_engine.graphics.opengl.GLObject.bindAll;
import static hero.graphics.passes.GeometryPass.SHADER_PBR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW_ALPHA;
import static hero.graphics.VertexAttrib.*;

public class LODPBRMaterial extends Material {

    public PBRTexture tex = null;

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
