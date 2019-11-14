package hero.graphics.restructure;

import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.Vec3d;

public class Material {
    public Vec3d diffuse = new Vec3d(0, 0, 0);
    public Vec3d specular = new Vec3d(0, 0, 0);
    public double opacity = 1;
    public double shininess = 2;
    public Texture texture;

    public Vec3d getColor() {
        return diffuse.add(specular);
    }

    public double getMetallic() {
        return specular.length() / Math.sqrt(3);
    }

    public double getRoughness() {
        return Math.pow(2 / (shininess + 2), .25);
//        return .5;
    }

    @Override
    public String toString() {
        return "Material{" +
                "diffuse=" + diffuse +
                ", specular=" + specular +
                ", opacity=" + opacity +
                ", shininess=" + shininess +
                ", texture=" + texture +
                '}';
    }
}
