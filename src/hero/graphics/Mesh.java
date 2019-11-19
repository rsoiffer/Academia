package hero.graphics;

import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.GLState;
import beige_engine.graphics.opengl.VertexArrayObject;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Mesh {

    public final int numFaces, numVerts;
    private final Map<VertexAttrib, float[]> data;
    private final int[] indices;

    private final Map<VertexAttrib, Integer> attribPositions;
    private final BufferObject vbo, ebo;

    public Mesh(List<VertexAttrib> attribs, Map<VertexAttrib, float[]> data, int[] indices) {
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

        numFaces = indices.length / 3;
        numVerts = data.get(attribs.get(0)).length / attribs.get(0).size;
        this.data = data;
        this.indices = indices;

        for (var a : attribs) {
            if (data.get(a).length != numVerts * a.size) {
                throw new IllegalArgumentException("data contains array of the wrong size");
            }
        }
        for (int i : indices) {
            if (i < 0 || i >= numVerts) {
                throw new IllegalArgumentException("Index out of bounds");
            }
        }

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

    public final int getIndex(int i) {
        return indices[i];
    }

    public VAOWrapper getVAOW(List<VertexAttrib> attribs) {
        var vao = VertexArrayObject.createVAO(() -> {
            vbo.bind();
            ebo.bind();

            for (int i = 0; i < attribs.size(); i++) {
                var a = attribs.get(i);
                glVertexAttribPointer(i, a.size, GL_FLOAT, false, 0, attribPositions.get(a) * 4);
                glEnableVertexAttribArray(i);
            }
            GLState.bindVertexArrayObject(null);
        });
        return new VAOWrapper(vao, numFaces);
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