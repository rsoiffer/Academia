package hero.graphics;

import beige_engine.util.math.Transformation;

public interface Renderable {

    void render(Transformation t, int pass);
}
