package hero.graphics;

import engine.util.math.Transformation;

public interface Renderable {

    void render(Transformation t, int pass);
}
