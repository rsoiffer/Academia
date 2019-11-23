package hero.graphics.loading;

import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import hero.graphics.Mesh;
import hero.graphics.VertexAttrib;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static hero.graphics.VertexAttrib.*;
import static hero.graphics.loading.ConversionUtils.*;

public class RawMeshBuilder {

    private final Vec3d randomDir = MathUtils.randomInSphere(new Random());

    private static List<Float> toFloats(Object o) {
        if (o instanceof Vec3d) {
            var v = (Vec3d) o;
            return Arrays.asList((float) v.x, (float) v.y, (float) v.z);
        } else if (o instanceof Vec2d) {
            var v = (Vec2d) o;
            return Arrays.asList((float) v.x, (float) v.y);
        } else if (o instanceof float[]) {
            return toFloatList((float[]) o);
        }
        throw new IllegalArgumentException("Cannot parse object of type " + o.getClass());
    }

    private final VertexAttrib[] names;
    private int numIndices, numVerts;
    private final Map<VertexAttrib, List<Float>> attribs = new EnumMap<>(VertexAttrib.class);
    private final List<Integer> indices = new ArrayList<>();

    public RawMeshBuilder() {
        this(POSITIONS, TEX_COORDS, NORMALS, TANGENTS, BITANGENTS);
    }

    public RawMeshBuilder(VertexAttrib... names) {
        this.names = names;
        for (var name : names) {
            attribs.put(name, new ArrayList<>());
        }
    }

    public void addCylinderUV(Vec3d p, Vec3d dir, double radius, int detail, double texW, double texH0, double texH1) {
        Vec3d dir1 = dir.cross(randomDir).normalize();
        Vec3d dir2 = dir.cross(dir1).normalize();
        for (int i = 0; i < detail; i++) {
            double angle0 = i * 2 * Math.PI / detail, angle1 = (i + 1) * 2 * Math.PI / detail;
            Vec3d v0 = p.add(dir1.mul(Math.cos(angle0) * radius)).add(dir2.mul(Math.sin(angle0) * radius));
            Vec3d v1 = p.add(dir1.mul(Math.cos(angle1) * radius)).add(dir2.mul(Math.sin(angle1) * radius));
            addRectangleUV(v0, v1.sub(v0), dir, new Vec2d(texW * i / detail, texH0), new Vec2d(texW / detail, 0), new Vec2d(0, texH1 - texH0));
        }
    }

    public RawMeshBuilder addIndices(int... vals) {
        for (int i : vals) {
            indices.add(i);
        }
        numIndices += vals.length;
        return this;
    }

    public RawMeshBuilder addRectangle(Vec3d p, Vec3d edge1, Vec3d edge2) {
        addTriangle(p, p.add(edge1), p.add(edge1).add(edge2));
        addTriangle(p, p.add(edge1).add(edge2), p.add(edge2));
        return this;
    }

    public RawMeshBuilder addRectangleUV(Vec3d p, Vec3d edge1, Vec3d edge2, Vec2d uv, Vec2d uvd1, Vec2d uvd2) {
        addTriangleUV(p, uv, p.add(edge1), uv.add(uvd1), p.add(edge1).add(edge2), uv.add(uvd1).add(uvd2));
        addTriangleUV(p, uv, p.add(edge1).add(edge2), uv.add(uvd1).add(uvd2), p.add(edge2), uv.add(uvd2));
        return this;
    }

    public RawMeshBuilder addTriangle(Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d edge1 = p2.sub(p1), edge2 = p3.sub(p1);
        Vec3d normal = edge1.cross(edge2).normalize();
        addIndices(numVerts, numVerts + 1, numVerts + 2);
        addVertex(p1, normal);
        addVertex(p2, normal);
        addVertex(p3, normal);
        return this;
    }

    public RawMeshBuilder addTriangleUV(Vec3d p1, Vec2d uv1, Vec3d p2, Vec2d uv2, Vec3d p3, Vec2d uv3) {
        Vec3d edge1 = p2.sub(p1), edge2 = p3.sub(p1);
        Vec2d duv1 = uv2.sub(uv1), duv2 = uv3.sub(uv1);
        Vec3d normal = edge1.cross(edge2).normalize();
        Vec3d tangent = edge1.mul(duv2.y).add(edge2.mul(-duv1.y)).normalize();
        Vec3d bitangent = edge1.mul(-duv2.x).add(edge2.mul(duv1.x)).normalize();
        addIndices(numVerts, numVerts + 1, numVerts + 2);
        addVertex(p1, uv1, normal, tangent, bitangent);
        addVertex(p2, uv2, normal, tangent, bitangent);
        addVertex(p3, uv3, normal, tangent, bitangent);
        return this;
    }

    public RawMeshBuilder addVertex(Object... data) {
        if (data.length != names.length) {
            throw new IllegalArgumentException("data.length must equal names.length");
        }
        for (int i = 0; i < data.length; i++) {
            var f = toFloats(data[i]);
            if (f.size() != names[i].size) {
                throw new IllegalArgumentException("Input data must match attribute size");
            }
            for (var f2 : f) {
                if (!Float.isFinite(f2)) {
                    throw new IllegalArgumentException("Illegal data value: " + f2);
                }
            }
            attribs.get(names[i]).addAll(f);
        }
        numVerts += 1;
        return this;
    }

    public void smoothVertexNormals() {
        // TODO - implement

//        HashMap<Vec3d, Vec3d> normals = new HashMap();
//        for (VertexPBR v : vertices) {
//            normals.compute(v.position, (key, val) -> val == null ? v.normal : val.add(v.normal));
//        }
//        for (int i = 0; i < vertices.size(); i++) {
//            VertexPBR v = vertices.get(i);
//            VertexPBR v2 = new VertexPBR(v.position, v.texCoord, normals.get(v.position).normalize(), v.tangent, v.bitangent);
//            vertices.set(i, v2);
//        }
    }

    public Mesh toMesh() {
        if (numIndices == 0) {
            return null;
        }
        var attribs2 = Arrays.asList(names);
        var data2 = new EnumMap<VertexAttrib, float[]>(VertexAttrib.class);
        for (var a : attribs2) {
            data2.put(a, toFloatArray(attribs.get(a)));
        }
        var indices2 = toIntArray(indices);
        return new Mesh(attribs2, data2, indices2);
    }
}
