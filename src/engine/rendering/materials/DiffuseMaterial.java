package engine.rendering.materials;

import static engine.graphics.opengl.GLObject.bindAll;
import engine.graphics.opengl.Texture;
import engine.rendering.Renderable;
import engine.rendering.VertexAttrib;
import static engine.rendering.VertexAttrib.*;
import engine.rendering.drawables.DrawableSupplier;
import engine.util.Resources;

import static engine.rendering.passes.GeometryPass.SHADER_DIFFUSE;
import static engine.rendering.passes.ShadowPass.SHADER_SHADOW;
import java.util.Arrays;
import java.util.List;

public class DiffuseMaterial extends Material {

    public Texture texture = null;
    public double metallic = 0;
    public double roughness = .8;
    public boolean hasShadows = true;

    public static DiffuseMaterial load(String name) {
        var m = new DiffuseMaterial();
        m.texture = Resources.loadTexture(name);
        return m;
    }

    @Override
    public Renderable buildRenderable(DrawableSupplier mesh) {
        return new DiffuseRenderable(mesh);
    }

    public class DiffuseRenderable extends BasicRenderable {

        public DiffuseRenderable(DrawableSupplier mesh) {
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
