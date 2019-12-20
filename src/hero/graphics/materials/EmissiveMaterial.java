package hero.graphics.materials;

import engine.util.math.Vec3d;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;
import hero.graphics.drawables.DrawableSupplier;

import java.util.Arrays;
import java.util.List;

import static hero.graphics.VertexAttrib.POSITIONS;
import static hero.graphics.passes.LightingPass.SHADER_EMISSIVE_FLAT;

public class EmissiveMaterial extends Material<DrawableSupplier> {

    public Vec3d color;

    public EmissiveMaterial(Vec3d color) {
        this.color = color;
    }

    public Renderable buildRenderable(DrawableSupplier mesh) {
        return new EmissiveRenderable(mesh);
    }

    public class EmissiveRenderable extends BasicRenderable {

        public EmissiveRenderable(DrawableSupplier mesh) {
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
            drawModel();
        }
    }
}
