package hero.graphics.restructure.loading;

import beige_engine.graphics.opengl.Texture;
import beige_engine.graphics.sprites.Sprite;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.restructure.*;
import hero.graphics.restructure.materials.ColorMaterial;
import hero.graphics.restructure.materials.DiffuseMaterial;
import hero.graphics.restructure.materials.Material;
import org.joml.Matrix4d;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hero.graphics.restructure.VertexAttrib.*;
import static org.lwjgl.assimp.Assimp.*;

public class AssimpLoader {

    private static final Map<String, AssimpLoader> MODEL_CACHE = new HashMap();
    private final String texturesDir;
    private final List<Material> materials;
    private final List<Strategy> strategies;
    public ModelNode rootNode;

    private AssimpLoader(String filename) {
        if (filename.contains("/")) {
            texturesDir = "../models/" + filename.substring(0, filename.lastIndexOf('/'));
        } else {
            texturesDir = "";
        }

        var flags = aiProcess_JoinIdenticalVertices
                | aiProcess_Triangulate
                | aiProcess_FixInfacingNormals
                | aiProcess_CalcTangentSpace
//                | aiProcess_GenNormals
//                | aiProcess_RemoveRedundantMaterials
//                | aiProcess_OptimizeGraph
//                | aiProcess_OptimizeMeshes
                ;
        var aiScene = aiImportFile("models/" + filename, flags);
        if (aiScene == null) {
            throw new RuntimeException("Error loading model: " + filename);
        }

        materials = streamBuf(aiScene.mMaterials()).map(AIMaterial::create).map(this::toMaterial).collect(Collectors.toList());
        strategies = streamBuf(aiScene.mMeshes()).map(AIMesh::create).map(this::toStrategy).collect(Collectors.toList());
        rootNode = toModelNode(aiScene.mRootNode());
    }

    public static AssimpLoader load(String fileName) {
        if (!MODEL_CACHE.containsKey(fileName)) {
            AssimpLoader f = new AssimpLoader(fileName);
            MODEL_CACHE.put(fileName, f);
        }
        return MODEL_CACHE.get(fileName);
    }

    private static Matrix4d toMatrix4d(AIMatrix4x4 m) {
        return new Matrix4d(
                m.a1(), m.a2(), m.a3(), m.a4(),
                m.b1(), m.b2(), m.b3(), m.b4(),
                m.c1(), m.c2(), m.c3(), m.c4(),
                m.d1(), m.d2(), m.d3(), m.d4()).transpose();
    }

    private static Optional<Vec3d> loadMaterialColor(AIMaterial aiMaterial, String key) {
        AIColor4D aiColor = AIColor4D.create();
        return aiGetMaterialColor(aiMaterial, key, aiTextureType_NONE, 0, aiColor) == 0
                ? Optional.of(new Vec3d(aiColor.r(), aiColor.g(), aiColor.b())) : Optional.empty();
    }

    private static Optional<Double> loadMaterialFloat(AIMaterial aiMaterial, String key) {
        float[] res = new float[1];
        return aiGetMaterialFloatArray(aiMaterial, key, aiTextureType_NONE, 0, res, new int[]{1}) == 0
                ? Optional.of((double) res[0]) : Optional.empty();
    }

    private static Optional<String> loadMaterialTexturePath(AIMaterial aiMaterial, int type) {
        AIString aiPath = AIString.calloc();
        var ecode = aiGetMaterialTexture(aiMaterial, type, 0, aiPath,
                (IntBuffer) null, null, null, null, null, null);
        return ecode == 0 ? Optional.of(aiPath.dataString()) : Optional.empty();
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

    private Texture loadMaterialTexture(AIMaterial aiMaterial, int type) {
        var path = loadMaterialTexturePath(aiMaterial, type);
        if (path.isEmpty() || path.get().length() == 0) {
            return null;
        }
        return Sprite.load(texturesDir + "/" + path.get()).texture;
    }

    private Material toMaterial(AIMaterial aiMaterial) {
        var diffuse = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE).orElse(new Vec3d(1, 0, 1));
        var specular = loadMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR).orElse(new Vec3d(0, 0, 0));
        var opacity = loadMaterialFloat(aiMaterial, AI_MATKEY_OPACITY).orElse(1.);
        var shininess = loadMaterialFloat(aiMaterial, AI_MATKEY_SHININESS).orElse(2.);
        var texture = loadMaterialTexture(aiMaterial, aiTextureType_DIFFUSE);

        var metallic = specular.length() / Math.sqrt(3);
        var roughness = Math.pow(2 / (shininess + 2), .25);

        if (opacity != 1) return null;

        if (texture == null) {
            var m = new ColorMaterial();
            m.color = diffuse.add(specular);
            m.metallic = metallic;
            m.roughness = roughness;
            return m;
        } else {
            var m = new DiffuseMaterial();
            m.texture = texture;
            m.metallic = metallic;
            m.roughness = roughness;
            return m;
        }
    }

    private void possiblySetAttrib(Mesh myMesh, VertexAttrib name, AIVector3D.Buffer buf) {
        if (buf != null) {
            myMesh.setAttrib(name, streamBuf(buf).flatMap(AssimpLoader::streamVec));
        }
    }

    private Strategy toStrategy(AIMesh aiMesh) {
        var rawMesh = new Mesh(aiMesh.mNumFaces(), aiMesh.mNumVertices());
        rawMesh.setIndices(streamBuf(aiMesh.mFaces()).flatMap(aiFace -> streamBuf(aiFace.mIndices())));
        possiblySetAttrib(rawMesh, POSITIONS, aiMesh.mVertices());
        possiblySetAttrib(rawMesh, NORMALS, aiMesh.mNormals());
        possiblySetAttrib(rawMesh, TEX_COORDS, aiMesh.mTextureCoords(0));
        possiblySetAttrib(rawMesh, TANGENTS, aiMesh.mTangents());
        possiblySetAttrib(rawMesh, BITANGENTS, aiMesh.mBitangents());

        var material = materials.get(aiMesh.mMaterialIndex());
        return material.buildStrategy(rawMesh);
    }

    private ModelNode toModelNode(AINode aiNode) {
        var transform = new Transformation(toMatrix4d(aiNode.mTransformation()));
        var nodeMeshes = aiNode.mNumMeshes() == 0 ? Collections.EMPTY_LIST :
                streamBuf(aiNode.mMeshes()).map(strategies::get).collect(Collectors.toList());
        var children = aiNode.mNumChildren() == 0 ? Collections.EMPTY_LIST :
                streamBuf(aiNode.mChildren()).map(AINode::create).map(this::toModelNode).collect(Collectors.toList());
        return new ModelNode(transform, nodeMeshes, children);
    }
}
