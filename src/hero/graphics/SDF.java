package hero.graphics;

import beige_engine.util.math.Vec3d;

public interface SDF {

    static SDF cone(Vec3d pos, Vec3d dir, double width) {
        Vec3d dir2 = dir.normalize();
        double d1 = width / Math.sqrt(1 + width * width);
        double d2 = 1 / Math.sqrt(1 + width * width);
        return v -> {
            double q = v.sub(pos).dot(dir2);
            return q * d1 - v.sub(pos).sub(dir2.mul(q)).length() * d2;
        };
    }

    static SDF cylinder(Vec3d pos, Vec3d dir, double radius) {
        Vec3d dir2 = dir.normalize();
        return v -> radius - v.sub(pos).sub(dir2.mul(v.sub(pos).dot(dir2))).length();
    }

    static SDF cylinder(Vec3d pos, Vec3d dir, double radius, double height) {
        return intersection(cylinder(pos, dir, radius), halfSpace(pos.add(dir.mul(-height)), dir), halfSpace(pos.add(dir.mul(height)), dir.mul(-1)));
    }

    static SDF halfSpace(Vec3d pos, Vec3d dir) {
        Vec3d dir2 = dir.normalize();
        return v -> v.sub(pos).dot(dir2);
    }

    static SDF intersection(SDF... a) {
        return v -> {
            double d = Double.MAX_VALUE;
            for (SDF s : a) {
                d = Math.min(d, s.value(v));
            }
            return d;
        };
    }

    static SDF intersectionSmooth(double k, SDF... a) {
        return v -> {
            double d = 0;
            for (SDF s : a) {
                d += Math.exp(-k * s.value(v));
            }
            return -Math.log(d) / k;
        };
    }

    static SDF sphere(Vec3d pos, double size) {
        return v -> size - v.sub(pos).length();
    }

    static SDF union(SDF... a) {
        return v -> {
            double d = Double.MIN_VALUE;
            for (SDF s : a) {
                d = Math.max(d, s.value(v));
            }
            return d;
        };
    }

    double value(Vec3d v);

    default SDF invert() {
        return v -> -value(v);
    }

    default SDF scale(double scale) {
        return v -> scale * value(v.mul(scale));
    }

    default SDF translate(Vec3d t) {
        return v -> value(v.sub(t));
    }
}
