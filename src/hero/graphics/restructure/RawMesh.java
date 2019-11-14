package hero.graphics.restructure;

import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.VertexArrayObject;
import hero.graphics.models.Model;

import java.util.HashMap;
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

public class RawMesh {

    public final int numFaces, numVerts;
    private int[] indices;
    private final Map<String, float[]> attribs = new HashMap<>();

    public RawMesh(int numFaces, int numVerts) {
        this.numFaces = numFaces;
        this.numVerts = numVerts;
    }

    public Model buildModel(String... names) {
        int[] attribSizes = getAttribSizes(names).toArray();
        var vbo = new BufferObject(GL_ARRAY_BUFFER, getMergedAttribs(names));
        var ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, getIndices());

        var vao = VertexArrayObject.createVAO(() -> {
            vbo.bind();
            ebo.bind();
            int pos = 0;
            for (int i = 0; i < attribSizes.length; i++) {
                glVertexAttribPointer(i, attribSizes[i], GL_FLOAT, false, 0, pos);
                glEnableVertexAttribArray(i);
                pos += attribSizes[i] * numVerts * 4;
            }
        });

        return () -> {
            vao.bind();
            ebo.bind();
            glDrawElements(GL_TRIANGLES, numFaces * 3, GL_UNSIGNED_INT, 0);
        };
    }

    public float[] getAttrib(String name) {
        if (!attribs.containsKey(name)) {
            throw new IllegalArgumentException("Unknown attribute " + name);
        }
        return attribs.get(name);
    }

    public IntStream getAttribSizes(String... names) {
        return Stream.of(names).mapToInt(s -> getAttrib(s).length / numVerts);
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getMergedAttribs(String...names) {
        int totalSize = Stream.of(names).mapToInt(s -> getAttrib(s).length).sum();
        float[] data = new float[totalSize];
        int pos = 0;
        for (var name : names) {
            float[] attrib = getAttrib(name);
            System.arraycopy(attrib, 0, data, pos, attrib.length);
            pos += attrib.length;
        }
        return data;
    }

    public void setAttrib(String name, float[] values) {
        if (values.length % numVerts != 0) {
            throw new IllegalArgumentException("The values array is the wrong length");
        }
        attribs.put(name, values);
    }

    public void setAttrib(String name, List<Float> values) {
        float[] f = new float[values.size()];
        int pos = 0;
        for (float v : values) {
            f[pos++] = v;
        }
        setAttrib(name, f);
    }

    public void setAttrib(String name, Stream<Float> values) {
        setAttrib(name, values.collect(Collectors.toList()));
    }

    public void setIndices(int[] values) {
        if (values.length != numFaces * 3) {
            throw new IllegalArgumentException("The values array is the wrong length");
        }
        indices = values;
    }

    public void setIndices(IntStream values) {
        setIndices(values.toArray());
    }

    public void setIndices(Stream<Integer> values) {
        setIndices(values.mapToInt(i -> i));
    }
}