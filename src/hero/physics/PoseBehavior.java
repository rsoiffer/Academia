package hero.physics;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import org.joml.Matrix4d;

public class PoseBehavior extends Behavior {

    public Vec3d position = new Vec3d(0, 0, 0);
    public Quaternion rotation = Quaternion.IDENTITY;

    public Matrix4d getMatrix() {
        return getTransform().matrix();
    }

    public Transformation getTransform() {
        return Transformation.create(position, rotation, 1);
    }

    public void rotate(Quaternion q) {
        rotation = q.mul(rotation);
    }

    public void translate(Vec3d v) {
        position = position.add(v);
    }
}
