package hero.graphics;

import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.VertexArrayObject;

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
    private final Map<VertexAttrib, float[]> attribs = new EnumMap<>(VertexAttrib.class);
    private int[] indices;

    public Mesh(int numFaces, int numVerts) {
        this.numFaces = numFaces;
        this.numVerts = numVerts;
    }

    public VAOWrapper getVAOW(List<VertexAttrib> names) {
        var vao = VertexArrayObject.createVAO(() -> {
            new BufferObject(GL_ARRAY_BUFFER, getMergedAttribs(names));
            new BufferObject(GL_ELEMENT_ARRAY_BUFFER, getIndices());

            int pos = 0;
            int[] attribSizes = getAttribSizes(names).toArray();
            for (int i = 0; i < attribSizes.length; i++) {
                glVertexAttribPointer(i, attribSizes[i], GL_FLOAT, false, 0, pos);
                glEnableVertexAttribArray(i);
                pos += attribSizes[i] * numVerts * 4;
            }
        });
        return new VAOWrapper(vao, numFaces);
    }

    private float[] getAttrib(VertexAttrib name) {
        if (!attribs.containsKey(name)) {
            throw new IllegalArgumentException("Unknown attribute " + name);
        }
        return attribs.get(name);
    }

    private IntStream getAttribSizes(List<VertexAttrib> names) {
        return names.stream().mapToInt(s -> getAttrib(s).length / numVerts);
    }

    private int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] values) {
        if (values.length != numFaces * 3) {
            throw new IllegalArgumentException("The values array is the wrong length");
        }
        for (int i : values) {
            if (i < 0 || i >= numVerts) {
                throw new IllegalArgumentException("Index out of bounds");
            }
        }
        indices = values;
    }

    public void setIndices(IntStream values) {
        setIndices(values.toArray());
    }

    public void setIndices(Stream<Integer> values) {
        setIndices(values.mapToInt(i -> i));
    }

    private float[] getMergedAttribs(List<VertexAttrib> names) {
        int totalSize = names.stream().mapToInt(s -> getAttrib(s).length).sum();
        float[] data = new float[totalSize];
        int pos = 0;
        for (var name : names) {
            float[] attrib = getAttrib(name);
            System.arraycopy(attrib, 0, data, pos, attrib.length);
            pos += attrib.length;
        }
        return data;
    }

    public int getIndex(int i) {
        return indices[i];
    }

    public Map<VertexAttrib, float[]> getVertex(int i) {
        if (i < 0 || i >= numVerts) {
            throw new RuntimeException("Index out of bounds");
        }
        var r = new EnumMap<VertexAttrib, float[]>(VertexAttrib.class);
        for (var v : attribs.keySet()) {
            float[] f = new float[v.size];
            System.arraycopy(attribs.get(v), v.size * i, f, 0, v.size);
            r.put(v, f);
        }
        return r;
    }

    public void setAttrib(VertexAttrib name, float[] values) {
        if (values.length != numVerts * name.size) {
            throw new IllegalArgumentException("The values array is the wrong length");
        }
        attribs.put(name, values);
    }

    public void setAttrib(VertexAttrib name, List<Float> values) {
        float[] f = new float[values.size()];
        int pos = 0;
        for (float v : values) {
            f[pos++] = v;
        }
        setAttrib(name, f);
    }

    public void setAttrib(VertexAttrib name, Stream<Float> values) {
        setAttrib(name, values.collect(Collectors.toList()));
    }
}