package hero.graphics.restructure;

import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec3d;

public class Material {
    public Vec3d ambient, diffuse, specular;
    public double opacity, shininess;
    public Texture texture;

    public Vec3d getColor() {
        return diffuse.add(specular);
    }

    public double getMetallic() {
        return specular.length() / Math.sqrt(3);
    }

    public double getRoughness() {
        return Math.pow(2 / (shininess + 2), .25);
//        return .8;
    }
}
