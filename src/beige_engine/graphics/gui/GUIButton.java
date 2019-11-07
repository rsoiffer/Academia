package beige_engine.graphics.gui;

import beige_engine.graphics.Color;

public class GUIButton extends GUIRectangle {

    public GUIText text;
    public Runnable onClick;

    public GUIButton(String s) {
        this(s, null);
    }

    public GUIButton(String s, Runnable onClick) {
        if (s != null && !s.isEmpty()) {
            text = new GUIText(s);
            add(text);
        }
        this.onClick = onClick;
    }

    @Override
    public void onClick() {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    public void onHoverStart() {
        color = new Color(.6, .6, .6, 1);
    }

    @Override
    public void onHoverStop() {
        color = new Color(.4, .4, .4, 1);
    }
}
