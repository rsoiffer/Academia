package beige_engine.graphics.voxels;

import beige_engine.engine.Core;
import beige_engine.engine.Settings;
import beige_engine.graphics.Camera;
import beige_engine.graphics.Color;
import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.Shader;
import beige_engine.graphics.opengl.VertexArrayObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static beige_engine.util.math.MathUtils.floor;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.util.rlestorage.RLEColumn;

public class VoxelRenderer<T> {

    public static final List<Vec3d> DIRS = Arrays.asList(
            new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, -1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 0, -1), new Vec3d(0, 0, 1));

    private final VoxelRendererParams<T> params;
    private final int vertexSize;
    private final Map<Vec3d, Integer> numQuadsMap = new HashMap();
    private final Map<Vec3d, VertexArrayObject> vaoMap = new HashMap();
    private final Map<Vec3d, BufferObject> vboMap = new HashMap();
    private final Vec3d max, min;
    private int vboMapsFinished;

    public VoxelRenderer(VoxelRendererParams<T> params) {
        this.params = params;
        vertexSize = params.vertexAttribSizes.stream().mapToInt(i -> i).sum();

        // Define quads map
        Map<Vec3d, List<VoxelFaceInfo>> quads = new HashMap();
        for (Vec3d dir : DIRS) {
            quads.put(dir, new ArrayList());
        }
        // Find all quads to render
        for (Vec2d v : params.columnsToDraw) {
            for (Vec3d dir : DIRS.subList(0, 4)) {
                generateExposedSideFaces(floor(v.x), floor(v.y), dir, quads.get(dir));
            }
            generateExposedFaces(floor(v.x), floor(v.y), quads.get(new Vec3d(0, 0, -1)), quads.get(new Vec3d(0, 0, 1)));
        }
        max = new Vec3d(quads.values().stream().flatMap(List::stream).mapToInt(vfi -> vfi.x).max().getAsInt() + 1,
                quads.values().stream().flatMap(List::stream).mapToInt(vfi -> vfi.y).max().getAsInt() + 1,
                quads.values().stream().flatMap(List::stream).mapToInt(vfi -> vfi.z).max().getAsInt() + 1);
        min = new Vec3d(quads.values().stream().flatMap(List::stream).mapToInt(vfi -> vfi.x).min().getAsInt(),
                quads.values().stream().flatMap(List::stream).mapToInt(vfi -> vfi.y).min().getAsInt(),
                quads.values().stream().flatMap(List::stream).mapToInt(vfi -> vfi.z).min().getAsInt());

        for (Vec3d dir : DIRS) {
            numQuadsMap.put(dir, quads.get(dir).size());

            // Copy quad data to vertex array
            float[] vertices = new float[vertexSize * quads.get(dir).size()];
            for (int i = 0; i < quads.get(dir).size(); i++) {
                float[] data = params.voxelFaceToData.apply(quads.get(dir).get(i), dir);
                System.arraycopy(data, 0, vertices, vertexSize * i, vertexSize);
            }

            // Create VBOs
            if (Settings.MULTITHREADED_OPENGL) {
                if (!vboMap.containsKey(dir)) {
                    vboMap.put(dir, new BufferObject(GL_ARRAY_BUFFER));
                }
                vboMap.get(dir).bind();
                vboMap.get(dir).putData(vertices);
                vboMapsFinished++;
            } else {
                // Workaround for threading issues
                Core.onMainThread(() -> {
                    if (!vboMap.containsKey(dir)) {
                        vboMap.put(dir, new BufferObject(GL_ARRAY_BUFFER));
                    }
                    vboMap.get(dir).bind();
                    vboMap.get(dir).putData(vertices);
                    vboMapsFinished++;
                });
            }
        }
    }

    public void cleanup() {
        for (VertexArrayObject vao : vaoMap.values()) {
            vao.destroy();
        }
    }

    private Iterator<Entry<Integer, T>> columnIterator(int x, int y) {
        RLEColumn<T> r = params.columnAt.apply(x, y);
        return (r == null ? new ArrayList() : r).iterator();
    }

    private void generateExposedFaces(int x, int y, List<VoxelFaceInfo> quads1, List<VoxelFaceInfo> quads2) {
        Iterator<Entry<Integer, T>> i = columnIterator(x, y);
        if (!i.hasNext()) {
            return;
        }
        Entry<Integer, T> e = i.next();
        while (i.hasNext()) {
            Entry<Integer, T> ne = i.next();
            if (ne.getValue() == null && e.getValue() != null) {
                quads2.add(new VoxelFaceInfo(x, y, e.getKey(), e.getValue()));
            } else if (ne.getValue() != null && e.getValue() == null) {
                quads1.add(new VoxelFaceInfo(x, y, e.getKey() + 1, ne.getValue()));
            }
            e = ne;
        }
        quads2.add(new VoxelFaceInfo(x, y, e.getKey(), e.getValue()));
    }

    private void generateExposedSideFaces(int x, int y, Vec3d dir, List<VoxelFaceInfo> quads) {
        Iterator<Entry<Integer, T>> i1 = columnIterator(x, y);
        Iterator<Entry<Integer, T>> i2 = columnIterator(x + (int) dir.x, y + (int) dir.y);
        Entry<Integer, T> e1 = i1.hasNext() ? i1.next() : null;
        Entry<Integer, T> e2 = i2.hasNext() ? i2.next() : null;
        if (e1 == null) {
            return;
        }
        int pos = Math.min(e1.getKey(), e2 == null ? Integer.MAX_VALUE : e2.getKey());
        while (true) {
            if (e2 != null && e2.getKey() < e1.getKey()) {
                Entry<Integer, T> next_e2 = i2.hasNext() ? i2.next() : null;
                int next_pos = Math.min(e1.getKey(), next_e2 == null ? Integer.MAX_VALUE : next_e2.getKey());
                if (e1.getValue() != null && (next_e2 == null || next_e2.getValue() == null)) {
                    for (int z = pos; z < next_pos; z++) {
                        quads.add(new VoxelFaceInfo(x, y, z + 1, e1.getValue()));
                    }
                }
                e2 = next_e2;
                pos = next_pos;
                continue;
            } else if (i1.hasNext()) {
                Entry<Integer, T> next_e1 = i1.next();
                int next_pos = Math.min(next_e1.getKey(), e2 == null ? Integer.MAX_VALUE : e2.getKey());
                if (next_e1.getValue() != null && (e2 == null || e2.getValue() == null)) {
                    for (int z = pos; z < next_pos; z++) {
                        quads.add(new VoxelFaceInfo(x, y, z + 1, next_e1.getValue()));
                    }
                }
                e1 = next_e1;
                pos = next_pos;
                continue;
            }
            break;
        }
    }

    private boolean intersectsFrustum() {
        return true;
//        return Camera.camera3d.getViewFrustum().testAab((float) min.x, (float) min.y, (float) min.z, (float) max.x, (float) max.y, (float) max.z);
    }

    public void render(Transformation t, Color color) {
        if (vboMapsFinished == 6) {
            if (vaoMap.isEmpty()) {
                for (Vec3d dir : DIRS) {
                    vaoMap.put(dir, VertexArrayObject.createVAO(() -> {
                        vboMap.get(dir).bind();
                        int total = 0;
                        for (int i = 0; i < params.vertexAttribSizes.size(); i++) {
                            glVertexAttribPointer(i, params.vertexAttribSizes.get(i), GL_FLOAT, false, vertexSize * 4, total * 4);
                            glEnableVertexAttribArray(i);
                            total += params.vertexAttribSizes.get(i);
                        }
                    }));
                }
            }

            if (intersectsFrustum()) {
                Matrix4d worldMat = Camera.current.viewMatrix().mul(t.matrix());
                params.shader.setMVP(t);
                params.shader.setUniform("color", color);
                for (Vec3d dir : DIRS) {
                    Vector4d newDir = new Vector4d(dir.x, dir.y, dir.z, 0).mul(worldMat);
                    boolean check = new Vector4d(min.x, min.y, min.z, 1).mul(worldMat).dot(newDir) < 0
                            || new Vector4d(max.x, max.y, max.z, 1).mul(worldMat).dot(newDir) < 0;
                    if (check || true) {
                        params.shader.setUniform("normal", DIRS.indexOf(dir));
                        vaoMap.get(dir).bind();
                        glDrawArrays(GL_POINTS, 0, numQuadsMap.get(dir));
                    }
                }
            }
        }
    }

    public static class VoxelFaceInfo<T> {

        public static final Vec3d[] NORMAL_TO_DIR1 = {
            new Vec3d(0., 1., 0.),
            new Vec3d(0., 1., 0.),
            new Vec3d(1., 0., 0.),
            new Vec3d(1., 0., 0.),
            new Vec3d(1., 0., 0.),
            new Vec3d(1., 0., 0.)
        };
        public static final Vec3d[] NORMAL_TO_DIR2 = {
            new Vec3d(0., 0., 1.),
            new Vec3d(0., 0., 1.),
            new Vec3d(0., 0., 1.),
            new Vec3d(0., 0., 1.),
            new Vec3d(0., 1., 0.),
            new Vec3d(0., 1., 0.)
        };

        public final int x, y, z;
        public final T voxel;

        public VoxelFaceInfo(int x, int y, int z, T voxel) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.voxel = voxel;
        }

        public List<Vec3d> corners(Vec3d dir) {
            int normal = DIRS.indexOf(dir);
            Vec3d pos = normal % 2 == 0 ? new Vec3d(x, y, z) : new Vec3d(x, y, z).add(dir);
            return Arrays.asList(pos, pos.add(NORMAL_TO_DIR1[normal]), pos.add(NORMAL_TO_DIR1[normal]).add(NORMAL_TO_DIR2[normal]), pos.add(NORMAL_TO_DIR2[normal]));
        }
    }

    public static class VoxelRendererParams<T> {

        public List<Vec2d> columnsToDraw;
        public Shader shader;
        public List<Integer> vertexAttribSizes;
        public BiFunction<Integer, Integer, RLEColumn<T>> columnAt;
        public BiFunction<VoxelFaceInfo<T>, Vec3d, float[]> voxelFaceToData;
    }
}
