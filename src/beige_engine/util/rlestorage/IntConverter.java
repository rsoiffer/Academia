package beige_engine.util.rlestorage;

import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;

import static beige_engine.util.math.MathUtils.clamp;
import static beige_engine.util.math.MathUtils.round;

public interface IntConverter<T> {

    static int pack(int value, int pos, int size) {
        return (value & ((1 << size) - 1)) << pos;
    }

    static int unpack(int value, int pos, int size) {
        return (value >>> pos) & ((1 << size) - 1);
    }

    T fromInt(int i);

    int toInt(T t);

    class IntegerConverter implements IntConverter<Integer> {

        @Override
        public Integer fromInt(int i) {
            if (i == 0) {
                return null;
            }
            return i - 1;
        }

        @Override
        public int toInt(Integer t) {
            if (t == null) {
                return 0;
            }
            return t + 1;
        }
    }

    class Vec2dConverter implements IntConverter<Vec2d> {

        private static final double C = (1 << 16) - 1;

        @Override
        public Vec2d fromInt(int i) {
            if (i == C) {
                return null;
            }
            int x = unpack(i, 0, 16);
            int y = unpack(i, 16, 16);
            return new Vec2d(x / C, y / C);
        }

        @Override
        public int toInt(Vec2d t) {
            if (t == null) {
                return (int) C;
            }
            int x = round(clamp(t.x, 0, 1) * C);
            int y = round(clamp(t.y, 0, 1) * C);
            return pack(x, 0, 16) + pack(y, 16, 16);
        }
    }

    class Vec3dConverter implements IntConverter<Vec3d> {

        private static final double C = (1 << 10) - 1;

        @Override
        public Vec3d fromInt(int i) {
            if (i == -1) {
                return null;
            }
            int x = unpack(i, 0, 10);
            int y = unpack(i, 10, 10);
            int z = unpack(i, 20, 10);
            return new Vec3d(x / C, y / C, z / C);
        }

        @Override
        public int toInt(Vec3d t) {
            if (t == null) {
                return -1;
            }
            int x = round(clamp(t.x, 0, 1) * C);
            int y = round(clamp(t.y, 0, 1) * C);
            int z = round(clamp(t.z, 0, 1) * C);
            return pack(x, 0, 10) + pack(y, 10, 10) + pack(z, 20, 10);
        }
    }
}
