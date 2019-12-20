package engine.rendering.materials;

import engine.util.math.Vec3d;
import engine.rendering.VertexAttrib;
import engine.rendering.Renderable;
import engine.rendering.drawables.DrawableSupplier;

import java.util.Arrays;
import java.util.List;

import static engine.rendering.passes.GeometryPass.SHADER_COLOR;
import static engine.rendering.passes.ShadowPass.SHADER_SHADOW;
import static engine.rendering.VertexAttrib.NORMALS;
import static engine.rendering.VertexAttrib.POSITIONS;

public class ColorMaterial extends Material<DrawableSupplier> {

    public Vec3d color = new Vec3d(1, 0, 1);
    public double metallic = 0;
    public double roughness = .8;
    public double ao = 1;
    public Vec3d emissive = new Vec3d(0, 0, 0);
    public boolean hasShadows = true;

    public Renderable buildRenderable(DrawableSupplier mesh) {
        return new ColorRenderable(mesh);
    }

    public class ColorRenderable extends BasicRenderable {

        public ColorRenderable(DrawableSupplier mesh) {
            super(mesh);
        }

        @Override
        public List<VertexAttrib> attribs() {
            return Arrays.asList(POSITIONS, NORMALS);
        }

        @Override
        public void renderGeom() {
            SHADER_COLOR.bind();
            SHADER_COLOR.setUniform("color", color);
            SHADER_COLOR.setUniform("metallic", (float) metallic);
            SHADER_COLOR.setUniform("roughness", (float) roughness);
            SHADER_COLOR.setUniform("ao", (float) ao);
            SHADER_COLOR.setUniform("emissive", emissive);
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
