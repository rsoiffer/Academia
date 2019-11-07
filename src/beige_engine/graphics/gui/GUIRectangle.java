package beige_engine.graphics.gui;

import beige_engine.graphics.Color;
import beige_engine.graphics.Graphics;

public class GUIRectangle extends GUIItem {

    public Color color = new Color(.4, .4, .4, 1);
    public Color borderColor = Color.BLACK;

    @Override
    protected void render() {
        if (color != null) {
            Graphics.drawRectangle(transformationLL(), color);
        }
        if (borderColor != null) {
            Graphics.drawRectangleOutline(transformationLL(), borderColor);
        }
    }
}
