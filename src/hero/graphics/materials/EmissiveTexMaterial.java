package hero.graphics.materials;

import engine.graphics.Color;
import engine.graphics.opengl.Texture;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;
import hero.graphics.drawables.DrawableSupplier;

import java.util.Arrays;
import java.util.List;

import static engine.graphics.opengl.GLObject.bindAll;
import static hero.graphics.VertexAttrib.POSITIONS;
import static hero.graphics.VertexAttrib.TEX_COORDS;
import static hero.graphics.passes.LightingPass.SHADER_EMISSIVE_TEX;

public class EmissiveTexMaterial extends Material<DrawableSupplier> {

    public Texture tex;
    public Color color = Color.WHITE;

    public Renderable buildRenderable(DrawableSupplier mesh) {
        return new EmissiveRenderable(mesh);
    }

    public class EmissiveRenderable extends BasicRenderable {

        public EmissiveRenderable(DrawableSupplier mesh) {
            super(mesh);
        }

        @Override
        public List<VertexAttrib> attribs() {
            return Arrays.asList(POSITIONS, TEX_COORDS);
        }

        @Override
        public void renderEmissive() {
            bindAll(SHADER_EMISSIVE_TEX, tex);
            SHADER_EMISSIVE_TEX.setUniform("color", color);
            drawModel();
        }
    }
}
