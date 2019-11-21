package hero.game.particles;

import beige_engine.graphics.Camera;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;

import java.util.function.Supplier;

public class Particle {

    public boolean alive = true;

    public Vec3d position = null;
    public Quaternion rotation = Quaternion.IDENTITY;
    public Supplier<Vec3d> scale = () -> new Vec3d(1, 1, 1);
    public boolean billboard = true;

    public Vec3d velocity = new Vec3d(0, 0, 0);
    public Vec3d acceleration = new Vec3d(0, 0, 0);
    double friction = 0;

    public double time = 0;
    public double startupTime = 0;
    public double fadeTime = 1;

    private static Quaternion billboardRotation(Vec3d position) {
        var dir = Camera.current.getPos().sub(position);
        var up = new Vec3d(0, 0, 1);
        return Quaternion.fromXYAxes(up.cross(dir), dir.cross(up.cross(dir)));
    }

    public Transformation getTransform() {
        var quat = billboard ? billboardRotation(position).mul(rotation) : rotation;
        return Transformation.create(position, quat, scale.get());
    }

    public void update(double dt) {
        position = position.add(velocity.mul(dt).add(acceleration.mul(dt * dt / 2)));
        velocity = velocity.add(acceleration.mul(dt));
        velocity = velocity.mul(Math.exp(-dt * friction));
        time += dt;
        if (time > startupTime) {
            double cappedDT = Math.min(dt, time - startupTime);
            if (Math.random() > Math.exp(-cappedDT / fadeTime)) {
                alive = false;
            }
        }
    }
}
