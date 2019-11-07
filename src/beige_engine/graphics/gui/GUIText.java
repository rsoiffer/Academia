package beige_engine.graphics.gui;

import beige_engine.graphics.Color;
import beige_engine.graphics.sprites.Font;
import beige_engine.graphics.sprites.Font.FontText;
import java.util.Objects;
import beige_engine.util.math.Transformation;

public class GUIText extends GUIItem {

    private String oldText;
    private FontText text;
    public boolean centered = true;
    public double scale = 1;
    public Color color = new Color(1, 1, 1, 1);
    public Color outlineColor = new Color(0, 0, 0, 1);

    public GUIText(String s) {
        setText(s);
    }

    @Override
    protected void render() {
        if (text != null) {
            if (centered) {
                text.draw2dCentered(Transformation.create(center(), 0, scale), color, outlineColor);
            } else {
                text.draw2d(Transformation.create(center(), 0, scale), color, outlineColor);
            }
        }
    }

    public final void setText(String s) {
        if (!Objects.equals(s, oldText)) {
            if (s != null && !s.isEmpty()) {
                text = Font.load("arial_outline").renderText(s);
            } else {
                text = null;
            }
            oldText = s;
        }
    }
}
