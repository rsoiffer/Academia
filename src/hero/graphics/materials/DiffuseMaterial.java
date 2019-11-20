package hero.graphics.materials;

import beige_engine.graphics.opengl.Texture;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;

import java.util.Arrays;
import java.util.List;

import static beige_engine.graphics.opengl.GLObject.bindAll;
import static hero.graphics.passes.GeometryPass.SHADER_DIFFUSE;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.VertexAttrib.*;

public class DiffuseMaterial extends Material {

    public Texture texture = null;
    public double metallic = 0;
    public double roughness = .8;
    public boolean hasShadows = true;

    public static DiffuseMaterial load(String name) {
        var m = new DiffuseMaterial();
        m.texture = Texture.load(name);
        return m;
    }

    @Override
    public Renderable buildRenderable(Mesh mesh) {
        return new DiffuseRenderable(mesh);
    }

    public class DiffuseRenderable extends BasicRenderable {

        public DiffuseRenderable(Mesh mesh) {
            super(mesh);
        }

        @Override
        public List<VertexAttrib> attribs() {
            return Arrays.asList(POSITIONS, TEX_COORDS, NORMALS);
        }

        @Override
        public void renderGeom() {
            bindAll(SHADER_DIFFUSE, texture);
            SHADER_DIFFUSE.setUniform("metallic", (float) metallic);
            SHADER_DIFFUSE.setUniform("roughness", (float) roughness);
            drawModel();
        }

        @Override
        public void renderShadow() {
            if (hasShadows) {
                SHADER_SHADOW.bind();
                drawModel();
            }
        }
    }
}
