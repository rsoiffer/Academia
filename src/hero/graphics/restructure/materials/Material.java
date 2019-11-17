package hero.graphics.restructure.materials;

import hero.graphics.restructure.RawMesh;
import hero.graphics.restructure.Strategy;

public abstract class Material {

    public abstract Strategy buildStrategy(RawMesh rawMesh);
}
