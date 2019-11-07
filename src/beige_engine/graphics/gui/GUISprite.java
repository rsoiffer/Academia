package beige_engine.graphics.gui;

import beige_engine.graphics.Color;
import beige_engine.graphics.sprites.Sprite;

public class GUISprite extends GUIItem {

    public Sprite sprite;
    public double rotation;
    public double scale = 1;
    public Color color = Color.WHITE;

    public GUISprite(String fileName) {
        sprite = Sprite.load(fileName);
    }

    @Override
    protected void render() {
        sprite.draw(transformationCenter(), color);
    }
}
