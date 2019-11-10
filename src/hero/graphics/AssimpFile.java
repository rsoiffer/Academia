package hero.graphics;

import beige_engine.graphics.Color;
import beige_engine.graphics.opengl.BufferObject;
import beige_engine.graphics.opengl.VertexArrayObject;
import beige_engine.graphics.sprites.Sprite;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import hero.graphics.models.Model;
import hero.graphics.models.Vertex;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;

public class AssimpFile {

    private static final Map<String, AssimpFile> MODEL_CACHE = new HashMap();

    public static AssimpFile load(String fileName) {
        if (!MODEL_CACHE.containsKey(fileName)) {
            AssimpFile f = new AssimpFile(fileName);
            MODEL_CACHE.put(fileName, f);
        }
        return MODEL_CACHE.get(fileName);
    }


    private static Color loadMaterialColor(AIMaterial aiMaterial, String key) {
        AIColor4D aiColor = AIColor4D.create();
        return aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, aiColor) == 0
                ? new Color(aiColor.r(), aiColor.g(), aiColor.b(), aiColor.a()) : null;
    }

    private static Stream<Integer> streamBuf(IntBuffer buf) {
        return Stream.generate(buf::get).limit(buf.remaining());
    }

    private static Stream<Long> streamBuf(PointerBuffer buf) {
        return Stream.generate(buf::get).limit(buf.remaining());
    }
    private static <T extends Struct> Stream<T> streamBuf(StructBuffer<T, ?> buf) {
        return Stream.generate(buf::get).limit(buf.remaining());
    }

    private static Vec2d toVec2d(AIVector3D v) {
        return new Vec2d(v.x(), v.y());
    }

    private static Vec3d toVec3d(AIVector3D v) {
        return new Vec3d(v.x(), v.y(), v.z());
    }


    private final String texturesDir;
    public final List<Material> materials;
    public final List<Mesh> meshes;

    private AssimpFile(String filename) {
        texturesDir = "../models/" + filename.substring(0, filename.lastIndexOf('/'));

        var flags = aiProcess_JoinIdenticalVertices
                | aiProcess_Triangulate
                | aiProcess_FixInfacingNormals
                | aiProcess_CalcTangentSpace;
        var aiScene = aiImportFile("models/" + filename, flags);
        if (aiScene == null) {
            throw new RuntimeException("Error loading model: " + filename);
        }

        materials = streamBuf(aiScene.mMaterials()).map(AIMaterial::create).map(this::toMaterial).collect(Collectors.toList());
        meshes = streamBuf(aiScene.mMeshes()).map(AIMesh::create).map(this::toMesh).collect(Collectors.toList());
    }

    private Material toMaterial(AIMaterial aiMaterial) {
        var ambient = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT);
        var diffuse = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE);
        var specular = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR);

        float[] res = new float[1];
        aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_OPACITY, aiTextureType_NONE, 0, res, new int[] {1});
        var dissolve = res[0];
        if (dissolve != 1) return null;

        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path,
                (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        var texture = (textPath != null && textPath.length() > 0) ? Sprite.load(texturesDir + "/" + textPath) : null;

        return new Material(ambient, diffuse, specular, texture);
    }

    private Mesh toMesh(AIMesh aiMesh) {
        var positions = streamBuf(aiMesh.mVertices()).map(AssimpFile::toVec3d).collect(Collectors.toList());
        var normals = streamBuf(aiMesh.mNormals()).map(AssimpFile::toVec3d).collect(Collectors.toList());
        var indices = streamBuf(aiMesh.mFaces()).flatMap(aiFace -> streamBuf(aiFace.mIndices())).collect(Collectors.toList());

        var material = materials.get(aiMesh.mMaterialIndex());
        if (material == null) return null;

        if (material.texture == null) {
            var vertices = new ArrayList<Vertex.VertexColor>();
            for (int i = 0; i < positions.size(); i++) {
                vertices.add(new Vertex.VertexColor(
                        positions.get(i), new Vec3d(1, 1, 1), normals.get(i)
                ));
            }

            var num = indices.size();
            var vao = Vertex.createVAO(vertices, new int[]{3, 3, 3});
            var ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indices.stream().mapToInt(i -> i).toArray());

            return new Mesh(material, num, vao, ebo);
        }
        else {
            var texCoords = streamBuf(aiMesh.mTextureCoords(0)).map(AssimpFile::toVec2d).collect(Collectors.toList());
            var tangents = streamBuf(aiMesh.mTangents()).map(AssimpFile::toVec3d).collect(Collectors.toList());
            var bitangents = streamBuf(aiMesh.mBitangents()).map(AssimpFile::toVec3d).collect(Collectors.toList());

            var vertices = new ArrayList<Vertex.VertexPBR>();
            for (int i = 0; i < positions.size(); i++) {
                vertices.add(new Vertex.VertexPBR(
                        positions.get(i), texCoords.get(i), normals.get(i), tangents.get(i), bitangents.get(i)
                ));
            }

            var num = indices.size();
            var vao = Vertex.createVAO(vertices, new int[]{3, 2, 3, 3, 3});
            var ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indices.stream().mapToInt(i -> i).toArray());

            return new Mesh(material, num, vao, ebo);
        }
    }

    public static class Material {
        public Color ambient, diffuse, specular;
        public Sprite texture;

        public Material(Color ambient, Color diffuse, Color specular, Sprite texture) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
            this.texture = texture;
        }

        @Override
        public String toString() {
            return "Material{" +
                    "ambient=" + ambient +
                    ", diffuse=" + diffuse +
                    ", specular=" + specular +
                    ", texture=" + texture +
                    '}';
        }
    }

    public static class Mesh implements Model {
        public Material material;
        public int num;
        public VertexArrayObject vao;
        public BufferObject ebo;

        public Mesh(Material material, int num, VertexArrayObject vao, BufferObject ebo) {
            this.material = material;
            this.num = num;
            this.vao = vao;
            this.ebo = ebo;
        }

        @Override
        public void render() {
            vao.bind();
            glDrawElements(GL_TRIANGLES, num, GL_UNSIGNED_INT, 0);
        }

        @Override
        public String toString() {
            return "Mesh{" +
                    "material=" + material +
                    ", num=" + num +
                    ", vao=" + vao +
                    ", ebo=" + ebo +
                    '}';
        }
    }
}
