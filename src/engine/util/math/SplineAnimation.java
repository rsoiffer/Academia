package engine.util.math;

import java.util.ArrayList;
import java.util.List;

public class SplineAnimation {

    private final List<Double> keyframeTimes = new ArrayList();
    private final List<Vec3d> keyframePositions = new ArrayList();
    private final List<Vec3d> keyframeVelocities = new ArrayList();

    public static Vec3d cubicInterp(double t, Vec3d p1, Vec3d v1, Vec3d p2, Vec3d v2) {
        Vec3d a = v1.sub(p2.sub(p1));
        Vec3d b = v2.mul(-1).add(p2.sub(p1));
        return p1.lerp(p2, t).add(a.lerp(b, t).mul(t * (1 - t)));
    }

    public static Vec3d cubicInterpDerivative(double t, Vec3d p1, Vec3d v1, Vec3d p2, Vec3d v2) {
        Vec3d a = v1.sub(p2.sub(p1));
        Vec3d b = v2.mul(-1).add(p2.sub(p1));
        return p2.sub(p1).add(a.lerp(b, t).mul(1 - 2 * t)).add(b.sub(a).mul(t * (1 - t)));
    }

    public void addKeyframe(double time, Vec3d position, Vec3d velocity) {
        keyframeTimes.add(time);
        keyframePositions.add(position);
        keyframeVelocities.add(velocity);
    }

    public void clearKeyframesAfter(double t) {
        for (int i = 0; i < keyframeTimes.size();) {
            if (keyframeTimes.get(i) <= t) {
                i++;
            } else {
                keyframeTimes.remove(i);
                keyframePositions.remove(i);
                keyframeVelocities.remove(i);
            }
        }
    }

    public Vec3d getPosition(double time) {
        int i = 0;
        while (i < keyframeTimes.size() - 1 && keyframeTimes.get(i + 1) < time) {
            i++;
        }
        if (i == keyframeTimes.size() - 1) {
            return keyframePositions.get(i);
        }
        double dt = keyframeTimes.get(i + 1) - keyframeTimes.get(i);
        double t = (time - keyframeTimes.get(i)) / dt;
        return cubicInterp(t, keyframePositions.get(i), keyframeVelocities.get(i).mul(dt),
                keyframePositions.get(i + 1), keyframeVelocities.get(i + 1).mul(dt));
    }

    public Vec3d getVelocity(double time) {
        int i = 0;
        while (i < keyframeTimes.size() - 1 && keyframeTimes.get(i + 1) < time) {
            i++;
        }
        if (i == keyframeTimes.size() - 1) {
            return keyframeVelocities.get(i);
        }
        double dt = keyframeTimes.get(i + 1) - keyframeTimes.get(i);
        double t = (time - keyframeTimes.get(i)) / dt;
        return cubicInterpDerivative(t, keyframePositions.get(i), keyframeVelocities.get(i).mul(dt),
                keyframePositions.get(i + 1), keyframeVelocities.get(i + 1).mul(dt)).div(dt);
    }
}
