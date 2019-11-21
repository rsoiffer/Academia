package hero.graphics.drawables;

import hero.graphics.VertexAttrib;

import java.util.List;

public interface DrawableSupplier {

    Drawable getDrawable(List<VertexAttrib> attribs);
}
