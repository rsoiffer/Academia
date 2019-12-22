package engine.rendering.loading;

import engine.rendering.Mesh;
import engine.rendering.VertexAttrib;
import static engine.rendering.VertexAttrib.*;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;
import java.util.*;

public class TriMeshBuilder {

    private final List<Vec3d> positions = new ArrayList();
    private final List<Vec2d> texCoords = new ArrayList();
    private final List<Vec3d> normals = new ArrayList();

    public void addTriangleUV(Vec3d p1, Vec2d uv1, Vec3d p2, Vec2d uv2, Vec3d p3, Vec2d uv3) {
        positions.add(p1);
        positions.add(p2);
        positions.add(p3);
        texCoords.add(uv1);
        texCoords.add(uv2);
        texCoords.add(uv3);
        Vec3d edge1 = p2.sub(p1), edge2 = p3.sub(p1);
        var normal = edge1.cross(edge2);
        normals.add(normal);
        normals.add(normal);
        normals.add(normal);
    }

    private static void fillArray(float[] f, Vec2d v, int pos) {
        f[2 * pos] = (float) v.x;
        f[2 * pos + 1] = (float) v.y;
    }

    private static void fillArray(float[] f, Vec3d v, int pos) {
        f[3 * pos] = (float) v.x;
        f[3 * pos + 1] = (float) v.y;
        f[3 * pos + 2] = (float) v.z;
    }

    public void smoothVertexNormals() {
        var normalSums = new HashMap<Vec3d, Vec3d>();
        int numVerts = positions.size();
        for (int i = 0; i < numVerts; i++) {
            Vec3d pos = positions.get(i), normal = normals.get(i);
            normalSums.compute(pos, (k, v) -> normal.add(v == null ? new Vec3d(0, 0, 0) : v));
        }
        for (int i = 0; i < numVerts; i++) {
            normals.set(i, normalSums.get(positions.get(i)));
        }
    }

    public Mesh toMesh() {
        if (positions.isEmpty()) {
            return null;
        }
        int numVerts = positions.size();
        var positionsData = new float[3 * numVerts];
        var texCoordsData = new float[2 * numVerts];
        var normalsData = new float[3 * numVerts];
        var tangentsData = new float[3 * numVerts];
        var cotangentsData = new float[3 * numVerts];

        for (int i = 0; i < numVerts; i += 3) {
            Vec3d p1 = positions.get(i), p2 = positions.get(i + 1), p3 = positions.get(i + 2);
            Vec2d uv1 = texCoords.get(i), uv2 = texCoords.get(i + 1), uv3 = texCoords.get(i + 2);
            Vec3d n1 = normals.get(i).normalize(), n2 = normals.get(i + 1).normalize(), n3 = normals.get(i + 2).normalize();
            Vec3d edge1 = p2.sub(p1), edge2 = p3.sub(p1);
            Vec2d duv1 = uv2.sub(uv1), duv2 = uv3.sub(uv1);
            Vec3d tangent = edge1.mul(duv2.y).add(edge2.mul(-duv1.y)).normalize();
            Vec3d bitangent = edge1.mul(-duv2.x).add(edge2.mul(duv1.x)).normalize();
            fillArray(positionsData, p1, i);
            fillArray(positionsData, p2, i + 1);
            fillArray(positionsData, p3, i + 2);
            fillArray(texCoordsData, uv1, i);
            fillArray(texCoordsData, uv2, i + 1);
            fillArray(texCoordsData, uv3, i + 2);
            fillArray(normalsData, n1, i);
            fillArray(normalsData, n2, i + 1);
            fillArray(normalsData, n3, i + 2);
            fillArray(tangentsData, tangent, i);
            fillArray(tangentsData, tangent, i + 1);
            fillArray(tangentsData, tangent, i + 2);
            fillArray(cotangentsData, bitangent, i);
            fillArray(cotangentsData, bitangent, i + 1);
            fillArray(cotangentsData, bitangent, i + 2);
        }

        var attribs = Arrays.asList(POSITIONS, TEX_COORDS, NORMALS, TANGENTS, BITANGENTS);
        var data = new EnumMap<VertexAttrib, float[]>(VertexAttrib.class);
        data.put(POSITIONS, positionsData);
        data.put(TEX_COORDS, texCoordsData);
        data.put(NORMALS, normalsData);
        data.put(TANGENTS, tangentsData);
        data.put(BITANGENTS, cotangentsData);
        var indices = new int[numVerts];
        for (int i = 0; i < numVerts; i++) {
            indices[i] = i;
        }
        return new Mesh(attribs, data, indices);
    }
}
