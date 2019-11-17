package hero.graphics.restructure;

import beige_engine.util.math.Transformation;
import hero.graphics.restructure.materials.Material;

public class Mesh {

    private final Strategy strategy;

    public Mesh(RawMesh rawMesh, Material material) {
        strategy = material.buildStrategy(rawMesh);
    }

    public void render(Transformation t, int pass) {
        if (strategy != null) {
            strategy.render(t, pass);
        }
    }
}
