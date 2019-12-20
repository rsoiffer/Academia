package engine.rendering.drawables;

import engine.rendering.VertexAttrib;

import java.util.List;

public interface DrawableSupplier {

    Drawable getDrawable(List<VertexAttrib> attribs);
}
