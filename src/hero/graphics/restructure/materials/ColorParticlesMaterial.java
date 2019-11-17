package hero.graphics.restructure.materials;

import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.restructure.RawMesh;
import hero.graphics.restructure.Strategy;
import hero.graphics.restructure.Strategy.BasicStrategy;
import hero.graphics.restructure.VertexAttrib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static hero.graphics.passes.GeometryPass.SHADER_COLOR;
import static hero.graphics.passes.ShadowPass.SHADER_SHADOW;
import static hero.graphics.restructure.VertexAttrib.NORMALS;
import static hero.graphics.restructure.VertexAttrib.POSITIONS;

public class ColorParticlesMaterial extends Material {

    public Vec3d color = new Vec3d(1, 0, 1);
    public double metallic = 0;
    public double roughness = .5;
    public boolean hasShadows = true;

    public List<Transformation> particles = new LinkedList<>();

    public Strategy buildStrategy(RawMesh rawMesh) {
        return new ColorParticlesStrategy(rawMesh, this);
    }

    public class ColorParticlesStrategy extends BasicStrategy {

        public ColorParticlesStrategy(RawMesh rawMesh, Material material) {
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
            for (var t2 : particles) {
                drawModelOffset(t2);
            }
        }

        @Override
        public void renderShadow() {
            if (hasShadows) {
                SHADER_SHADOW.bind();
                for (var t2 : particles) {
                    drawModelOffset(t2);
                }
            }
        }
    }
}
