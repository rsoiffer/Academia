package hero.graphics.materials;

import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static hero.graphics.VertexAttrib.POSITIONS;
import static hero.graphics.passes.LightingPass.SHADER_EMISSIVE_FLAT;

public class EmissiveParticlesMaterial extends Material {

    public Vec3d color = new Vec3d(1, 0, 1);

    public List<Transformation> particles = new LinkedList<>();

    public Renderable buildRenderable(Mesh mesh) {
        return new EmissiveRenderable(mesh);
    }

    public class EmissiveRenderable extends BasicRenderable {

        public EmissiveRenderable(Mesh mesh) {
            super(mesh);
        }

        @Override
        public List<VertexAttrib> attribs() {
            return Arrays.asList(POSITIONS);
        }

        @Override
        public void renderEmissive() {
            SHADER_EMISSIVE_FLAT.bind();
            SHADER_EMISSIVE_FLAT.setUniform("color", color);
            for (var t2 : particles) {
                drawModelOffset(t2);
            }
        }
    }
}
