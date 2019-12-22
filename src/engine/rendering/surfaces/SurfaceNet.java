package engine.rendering.surfaces;

import engine.rendering.Mesh;
import engine.rendering.loading.TriMeshBuilder;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SurfaceNet extends Surface {

    private static final Vec3d ZERO = new Vec3d(0, 0, 0);

    private final int size;

    private final double[][][] data;
    private final Set<Edge> surfaceEdges;
    private final Vec3d[][][] points;
    private final int[][][] pointCounts;

    private boolean changed;
    private Mesh mesh;

    public SurfaceNet(Transformation transform, int size) {
        super(transform);
        this.size = size;

        data = new double[size + 2][size + 2][size + 2];
        surfaceEdges = new HashSet();
        points = new Vec3d[size + 1][size + 1][size + 1];
        pointCounts = new int[size + 1][size + 1][size + 1];
        for (int x = 0; x < size + 2; x++) {
            for (int y = 0; y < size + 2; y++) {
                for (int z = 0; z < size + 2; z++) {
                    data[x][y][z] = MIN;
                    if (x < size + 1 && y < size + 1 && z < size + 1) {
                        points[x][y][z] = ZERO;
                    }
                }
            }
        }
    }

    public static Surface chunked(Transformation transform) {
        int size = 32;
        return new ChunkedSurface(transform, size, 1, 1, k -> {
            return new SurfaceNet(transform.mul(Transformation.create(new Vec3d(k.x, k.y, k.z).mul(size), Quaternion.IDENTITY, 1)), size);
        });
    }

    @Override
    public double get(int x, int y, int z) {
        return data[x + 1][y + 1][z + 1];
    }

    @Override
    public Stream<Mesh> meshes() {
        if (changed) {
            changed = false;

            var builder = new TriMeshBuilder();
            for (Edge e : surfaceEdges) {
                if (e.x0 > 0 && e.x0 <= size && e.y0 > 0 && e.y0 <= size && e.z0 > 0 && e.z0 <= size) {
                    e.addToModel(builder);
                }
            }
            builder.smoothVertexNormals();
            mesh = builder.toMesh();
        }
        if (mesh == null) {
            return Stream.empty();
        }
        return Stream.of(mesh);
    }

    @Override
    public void set(int x, int y, int z, double d) {
        x += 1;
        y += 1;
        z += 1;
        if (x > 0) {
            updateEdge(x, y, z, x - 1, y, z, d);
        }
        if (x < size + 1) {
            updateEdge(x, y, z, x + 1, y, z, d);
        }
        if (y > 0) {
            updateEdge(x, y, z, x, y - 1, z, d);
        }
        if (y < size + 1) {
            updateEdge(x, y, z, x, y + 1, z, d);
        }
        if (z > 0) {
            updateEdge(x, y, z, x, y, z - 1, d);
        }
        if (z < size + 1) {
            updateEdge(x, y, z, x, y, z + 1, d);
        }
        data[x][y][z] = d;
        changed = true;
    }

    private void updateEdge(int x0, int y0, int z0, int x1, int y1, int z1, double newD0) {
        var oldD0 = data[x0][y0][z0];
        var d1 = data[x1][y1][z1];
        var oldSurface = oldD0 > BOUNDARY != d1 > BOUNDARY;
        var newSurface = newD0 > BOUNDARY != d1 > BOUNDARY;

        if (oldSurface || newSurface) {
            var oldCrossing = oldSurface ? new Vec3d(x0, y0, z0).lerp(new Vec3d(x1, y1, z1), (BOUNDARY - oldD0) / (d1 - oldD0)) : ZERO;
            var newCrossing = newSurface ? new Vec3d(x0, y0, z0).lerp(new Vec3d(x1, y1, z1), (BOUNDARY - newD0) / (d1 - newD0)) : ZERO;
            var modCrossing = newCrossing.sub(oldCrossing);
            int modCount = (oldSurface ? -1 : 0) + (newSurface ? 1 : 0);

            for (int x = Math.max(x0, x1) - 1; x <= Math.min(x0, x1); x++) {
                for (int y = Math.max(y0, y1) - 1; y <= Math.min(y0, y1); y++) {
                    for (int z = Math.max(z0, z1) - 1; z <= Math.min(z0, z1); z++) {
                        if (x >= 0 && x < size + 1 && y >= 0 && y < size + 1 && z >= 0 && z < size + 1) {
                            points[x][y][z] = points[x][y][z].add(modCrossing);
                            pointCounts[x][y][z] += modCount;
                        }
                    }
                }
            }
            Edge e;
            if (x0 > x1 || (x0 == x1 && y0 > y1) || (x0 == x1 && y0 == y1 && z0 > z1)) {
                e = new Edge(x1, y1, z1, x0, y0, z0, d1, newD0);
            } else {
                e = new Edge(x0, y0, z0, x1, y1, z1, newD0, d1);
            }
            if (!newSurface) {
                surfaceEdges.remove(e);
            }
            if (!oldSurface) {
                surfaceEdges.add(e);
            }
        }
    }

    private class Edge {

        private final int x0, y0, z0, x1, y1, z1;
        private final double d0, d1;

        public Edge(int x0, int y0, int z0, int x1, int y1, int z1, double d0, double d1) {
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.d0 = d0;
            this.d1 = d1;
        }

        private void addToModel(TriMeshBuilder RMB) {
            List<Vec3d> p = new ArrayList(4);
            for (int x = x1 - 1; x <= x0; x++) {
                for (int y = y1 - 1; y <= y0; y++) {
                    for (int z = z1 - 1; z <= z0; z++) {
                        p.add(transform.apply(points[x][y][z].div(pointCounts[x][y][z])));
                    }
                }
            }
            if (y0 == y1 != d0 > BOUNDARY) {
                if (x0 == x1) {
                    RMB.addTriangleUV(p.get(0), new Vec2d(0, 0), p.get(1), new Vec2d(1, 0), p.get(2), new Vec2d(0, 1));
                    RMB.addTriangleUV(p.get(3), new Vec2d(1, 1), p.get(2), new Vec2d(0, 1), p.get(1), new Vec2d(1, 0));
                } else {
                    RMB.addTriangleUV(p.get(0), new Vec2d(0, 0), p.get(1), new Vec2d(0, 1), p.get(2), new Vec2d(1, 0));
                    RMB.addTriangleUV(p.get(3), new Vec2d(1, 1), p.get(2), new Vec2d(1, 0), p.get(1), new Vec2d(0, 1));
                }
            } else {
                if (x0 == x1) {
                    RMB.addTriangleUV(p.get(0), new Vec2d(0, 0), p.get(2), new Vec2d(0, 1), p.get(1), new Vec2d(1, 0));
                    RMB.addTriangleUV(p.get(3), new Vec2d(1, 1), p.get(1), new Vec2d(1, 0), p.get(2), new Vec2d(0, 1));
                } else {
                    RMB.addTriangleUV(p.get(0), new Vec2d(0, 0), p.get(2), new Vec2d(1, 0), p.get(1), new Vec2d(0, 1));
                    RMB.addTriangleUV(p.get(3), new Vec2d(1, 1), p.get(1), new Vec2d(0, 1), p.get(2), new Vec2d(1, 0));
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Edge other = (Edge) obj;
            if (this.x0 != other.x0) {
                return false;
            }
            if (this.y0 != other.y0) {
                return false;
            }
            if (this.z0 != other.z0) {
                return false;
            }
            if (this.x1 != other.x1) {
                return false;
            }
            if (this.y1 != other.y1) {
                return false;
            }
            return this.z1 == other.z1;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.x0;
            hash = 97 * hash + this.y0;
            hash = 97 * hash + this.z0;
            hash = 97 * hash + this.x1;
            hash = 97 * hash + this.y1;
            hash = 97 * hash + this.z1;
            return hash;
        }
    }
}
