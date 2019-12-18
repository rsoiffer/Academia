package hero.physics.shapes;

import beige_engine.util.math.Vec3d;
import java.util.List;

public class AABB {

    public final Vec3d lower, upper;

    public AABB(Vec3d lower, Vec3d upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public static AABB boundingBox(List<Vec3d> points) {
        Vec3d lower = points.get(0);
        Vec3d upper = points.get(0);
        for (Vec3d p : points) {
            lower = new Vec3d(Math.min(p.x, lower.x), Math.min(p.y, lower.y), Math.min(p.z, lower.z));
            upper = new Vec3d(Math.max(p.x, upper.x), Math.max(p.y, upper.y), Math.max(p.z, upper.z));
        }
        return new AABB(lower, upper);
    }

    public Vec3d center() {
        return lower.lerp(upper, .5);
    }

    public boolean contains(Vec3d point) {
        return lower.x < point.x && point.x < upper.x
                && lower.y < point.y && point.y < upper.y
                && lower.z < point.z && point.z < upper.z;
    }

    public AABB expand(double amt) {
        return new AABB(lower.sub(amt), upper.add(amt));
    }

    public Vec3d size() {
        return upper.sub(lower);
    }

    @Override
    public String toString() {
        return "AABB{" + "lower=" + lower + ", upper=" + upper + '}';
    }

    public AABB translate(Vec3d pos) {
        return new AABB(lower.add(pos), upper.add(pos));
    }
}
