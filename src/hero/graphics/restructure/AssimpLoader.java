package hero.graphics.restructure;

import beige_engine.graphics.opengl.Texture;
import beige_engine.graphics.sprites.Sprite;
import beige_engine.util.math.Vec3d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.assimp.Assimp.*;

public class AssimpLoader {

    private static final Map<String, AssimpLoader> MODEL_CACHE = new HashMap();

    public static AssimpLoader load(String fileName) {
        if (!MODEL_CACHE.containsKey(fileName)) {
            AssimpLoader f = new AssimpLoader(fileName);
            MODEL_CACHE.put(fileName, f);
        }
        return MODEL_CACHE.get(fileName);
    }


    private static Vec3d loadMaterialColor(AIMaterial aiMaterial, String key) {
        AIColor4D aiColor = AIColor4D.create();
        return aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, aiColor) == 0
                ? new Vec3d(aiColor.r(), aiColor.g(), aiColor.b()) : null;
    }

    private static double loadMaterialFloat(AIMaterial aiMaterial, String key) {
        float[] res = new float[1];
        aiGetMaterialFloatArray(aiMaterial, key, aiTextureType_NONE, 0, res, new int[] {1});
        return res[0];
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

    private static Stream<Float> streamVec(AIVector3D v) {
        return Stream.of(v.x(), v.y(), v.z());
    }


    private final String texturesDir;
    public final List<Material> materials;
    public final List<Mesh> meshes;

    private AssimpLoader(String filename) {
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

    private Texture loadMaterialTexture(AIMaterial aiMaterial, int type) {
        AIString aiPath = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, type, 0, aiPath,
                (IntBuffer) null, null, null, null, null, null);
        String path = aiPath.dataString();
        return (path != null && path.length() > 0) ? Sprite.load(texturesDir + "/" + path).texture : null;
    }

    private Material toMaterial(AIMaterial aiMaterial) {
        var m = new Material();
        m.ambient = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT);
        m.diffuse = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE);
        m.specular = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR);
        m.opacity = loadMaterialFloat(aiMaterial, AI_MATKEY_OPACITY);
        m.shininess = loadMaterialFloat(aiMaterial, AI_MATKEY_SHININESS);
        m.texture = loadMaterialTexture(aiMaterial, aiTextureType_DIFFUSE);
        return m;
    }

    private void possiblySetAttrib(RawMesh myMesh, String name, AIVector3D.Buffer buf) {
        if (buf != null) {
            myMesh.setAttrib(name, streamBuf(buf).flatMap(AssimpLoader::streamVec));
        }
    }

    private Mesh toMesh(AIMesh aiMesh) {
        var rawMesh = new RawMesh(aiMesh.mNumFaces(), aiMesh.mNumVertices());
        rawMesh.setIndices(streamBuf(aiMesh.mFaces()).flatMap(aiFace -> streamBuf(aiFace.mIndices())));
        possiblySetAttrib(rawMesh, "positions", aiMesh.mVertices());
        possiblySetAttrib(rawMesh, "normals", aiMesh.mNormals());
        possiblySetAttrib(rawMesh, "texCoords", aiMesh.mTextureCoords(0));
        possiblySetAttrib(rawMesh, "tangents", aiMesh.mTangents());
        possiblySetAttrib(rawMesh, "bitangents", aiMesh.mBitangents());

        var material = materials.get(aiMesh.mMaterialIndex());
        return new Mesh(rawMesh, material);
    }
}
