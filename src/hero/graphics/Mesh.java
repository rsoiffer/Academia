package hero.graphics;

import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.GLState;
import beige_engine.graphics.opengl.VertexArrayObject;
import beige_engine.util.math.Vec3d;
import static hero.graphics.VertexAttrib.POSITIONS;
import hero.graphics.drawables.Drawable;
import hero.graphics.drawables.DrawableSupplier;
import hero.physics.shapes.AABB;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Mesh implements DrawableSupplier {

    public final int numFaces, numVerts;
    public final AABB aabb;
    public final Map<VertexAttrib, float[]> data;
    public final int[] indices;

    private final Map<VertexAttrib, Integer> attribPositions;
    private final BufferObject vbo, ebo;

    public Mesh(List<VertexAttrib> attribs, Map<VertexAttrib, float[]> data, int[] indices) {
        checkValid(attribs, data, indices);

        numFaces = indices.length / 3;
        numVerts = data.get(attribs.get(0)).length / attribs.get(0).size;
        aabb = createAABB(attribs, data, numVerts);
        this.data = data;
        this.indices = indices;

        attribPositions = new EnumMap<>(VertexAttrib.class);
        int totalSize = attribs.stream().mapToInt(s -> data.get(s).length).sum();
        var bufferData = new float[totalSize];
        int pos = 0;
        for (var a : attribs) {
            attribPositions.put(a, pos);
            float[] f = data.get(a);
            System.arraycopy(f, 0, bufferData, pos, f.length);
            pos += f.length;
        }

        GLState.bindVertexArrayObject(null);
        vbo = new BufferObject(GL_ARRAY_BUFFER, bufferData);
        ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indices);
        GLState.bindBuffer(null, GL_ARRAY_BUFFER);
        GLState.bindBuffer(null, GL_ELEMENT_ARRAY_BUFFER);
    }

    private static void checkValid(List<VertexAttrib> attribs, Map<VertexAttrib, float[]> data, int[] indices) {
        if (attribs.isEmpty()) {
            throw new IllegalArgumentException("attribs cannot be empty");
        }
        if (attribs.size() != data.size()) {
            throw new IllegalArgumentException("attribs and data must be the same size");
        }
        if (indices.length == 0) {
            throw new IllegalArgumentException("indices cannot be empty");
        }
        if (indices.length % 3 != 0) {
            throw new IllegalArgumentException("Number of indices must be a multiple of 3");
        }

        var numVerts = data.get(attribs.get(0)).length / attribs.get(0).size;
        for (var a : attribs) {
            if (data.get(a).length != numVerts * a.size) {
                throw new IllegalArgumentException("data contains array of the wrong size");
            }
            for (float f : data.get(a)) {
                if (!Float.isFinite(f)) {
                    throw new IllegalArgumentException("Illegal data value: " + f);
                }
            }
        }
        for (int i : indices) {
            if (i < 0 || i >= numVerts) {
                throw new IllegalArgumentException("Index out of bounds");
            }
        }
    }

    private static AABB createAABB(List<VertexAttrib> attribs, Map<VertexAttrib, float[]> data, int numVerts) {
        if (attribs.contains(POSITIONS)) {
            var points = new LinkedList<Vec3d>();
            for (int i = 0; i < numVerts; i++) {
                float[] f = data.get(POSITIONS);
                points.add(new Vec3d(f[3 * i], f[3 * i + 1], f[3 * i + 2]));
            }
            return AABB.boundingBox(points);
        } else {
            return null;
        }
    }

    public final int getIndex(int i) {
        return indices[i];
    }

    public Drawable getDrawable(List<VertexAttrib> attribs) {
        for (var a : attribs) {
            if (!attribPositions.containsKey(a)) {
                throw new IllegalArgumentException("Mesh doesn't have attrib " + a);
            }
        }
        var vao = VertexArrayObject.createVAO(() -> {
            vbo.bind();
            ebo.bind();

            for (int i = 0; i < attribs.size(); i++) {
                var a = attribs.get(i);
                glVertexAttribPointer(i, a.size, GL_FLOAT, false, 0, attribPositions.get(a) * 4);
                glEnableVertexAttribArray(i);
            }
            GLState.bindVertexArrayObject(null);
            GLState.bindBuffer(null, GL_ARRAY_BUFFER);
            GLState.bindBuffer(null, GL_ELEMENT_ARRAY_BUFFER);
        });
        return t -> {
            GLState.getShaderProgram().setUniform("model", t.matrix());
            vao.bind();
            glDrawElements(GL_TRIANGLES, numFaces * 3, GL_UNSIGNED_INT, 0);
        };
    }

    public Map<VertexAttrib, float[]> getVertex(int i) {
        if (i < 0 || i >= numVerts) {
            throw new RuntimeException("Index out of bounds");
        }
        var r = new EnumMap<VertexAttrib, float[]>(VertexAttrib.class);
        for (var v : data.keySet()) {
            float[] f = new float[v.size];
            System.arraycopy(data.get(v), v.size * i, f, 0, v.size);
            r.put(v, f);
        }
        return r;
    }
}
