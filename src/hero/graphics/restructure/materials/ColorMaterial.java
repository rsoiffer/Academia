package hero.graphics.restructure.materials;

import beige_engine.util.math.Vec3d;
import hero.graphics.restructure.RawMesh;
import hero.graphics.restructure.VertexAttrib;
import hero.graphics.restructure.Strategy;
import hero.graphics.restructure.Strategy.BasicStrategy;

import java.util.Arrays;
import java.util.List;

import static hero.graphics.passes.GeometryPass.SHADER_COLOR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.restructure.VertexAttrib.NORMALS;
import static hero.graphics.restructure.VertexAttrib.POSITIONS;

public class ColorMaterial extends Material {

    public Vec3d color = new Vec3d(1, 0, 1);
    public double metallic = 0;
    public double roughness = .5;

    public Strategy buildStrategy(RawMesh rawMesh) {
        return new ColorStrategy(rawMesh, this);
    }

    public class ColorStrategy extends BasicStrategy {

        public ColorStrategy(RawMesh rawMesh, Material material) {
            super(rawMesh, material);
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
            drawModel();
        }

        @Override
        public void renderShadow() {
            SHADER_SHADOW.bind();
            drawModel();
        }
    }
}
