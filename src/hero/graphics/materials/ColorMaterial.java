package hero.graphics.materials;

import beige_engine.util.math.Vec3d;
import hero.graphics.Mesh;
import hero.graphics.VertexAttrib;
import hero.graphics.Renderable;

import java.util.Arrays;
import java.util.List;

import static hero.graphics.passes.GeometryPass.SHADER_COLOR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.VertexAttrib.NORMALS;
import static hero.graphics.VertexAttrib.POSITIONS;

public class ColorMaterial extends Material {

    public Vec3d color = new Vec3d(1, 0, 1);
    public double metallic = 0;
    public double roughness = .8;
    public double ao = 1;
    public Vec3d emissive = new Vec3d(0, 0, 0);
    public boolean hasShadows = true;

    public Renderable buildRenderable(Mesh mesh) {
        return new ColorRenderable(mesh);
    }

    public class ColorRenderable extends BasicRenderable {

        public ColorRenderable(Mesh mesh) {
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
