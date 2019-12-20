package engine.rendering.materials;

import engine.graphics.Color;
import engine.graphics.opengl.Texture;
import engine.rendering.Renderable;
import engine.rendering.VertexAttrib;
import engine.rendering.drawables.DrawableSupplier;

import java.util.Arrays;
import java.util.List;

import static engine.graphics.opengl.GLObject.bindAll;
import static engine.rendering.VertexAttrib.POSITIONS;
import static engine.rendering.VertexAttrib.TEX_COORDS;
import static engine.rendering.passes.LightingPass.SHADER_EMISSIVE_TEX;

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
