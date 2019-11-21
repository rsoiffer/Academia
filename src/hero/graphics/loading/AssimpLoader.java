package hero.graphics.loading;

import beige_engine.graphics.opengl.Texture;
import beige_engine.graphics.sprites.Sprite;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.Mesh;
import hero.graphics.ModelNode;
import hero.graphics.Renderable;
import hero.graphics.VertexAttrib;
import hero.graphics.materials.ColorMaterial;
import hero.graphics.materials.DiffuseMaterial;
import hero.graphics.materials.Material;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIVector3D;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hero.graphics.VertexAttrib.*;
import static hero.graphics.loading.ConversionUtils.*;
import static org.lwjgl.assimp.Assimp.*;

public class AssimpLoader {

    private static final Map<String, AssimpLoader> MODEL_CACHE = new HashMap();
    private final String texturesDir;
    private final List<Material> materials;
    private final List<Renderable> renderables;
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
                | aiProcess_GenNormals
//                | aiProcess_RemoveRedundantMaterials
//                | aiProcess_OptimizeGraph
//                | aiProcess_OptimizeMeshes
                ;
        var aiScene = aiImportFile("models/" + filename, flags);
        if (aiScene == null) {
            throw new RuntimeException("Error loading model " + filename + ": " + aiGetErrorString());

        }

        materials = streamBuf(aiScene.mMaterials()).map(AIMaterial::create).map(this::toMaterial).collect(Collectors.toList());
        renderables = streamBuf(aiScene.mMeshes()).map(AIMesh::create).map(this::toRenderable).collect(Collectors.toList());
        rootNode = toModelNode(aiScene.mRootNode());
    }

    public static AssimpLoader load(String fileName) {
        if (!MODEL_CACHE.containsKey(fileName)) {
            AssimpLoader f = new AssimpLoader(fileName);
            MODEL_CACHE.put(fileName, f);
        }
        return MODEL_CACHE.get(fileName);
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

    private void possiblySetAttrib(List<VertexAttrib> attribs, Map<VertexAttrib, float[]> data, VertexAttrib a, AIVector3D.Buffer buf) {
        if (buf != null) {
            attribs.add(a);
            data.put(a, toFloatArray(streamBuf(buf).flatMap(ConversionUtils::streamVec3d)));
        }
    }

    private void possiblySetAttrib2d(List<VertexAttrib> attribs, Map<VertexAttrib, float[]> data, VertexAttrib a, AIVector3D.Buffer buf) {
        if (buf != null) {
            attribs.add(a);
            data.put(a, toFloatArray(streamBuf(buf).flatMap(ConversionUtils::streamVec2d)));
        }
    }

    private Renderable toRenderable(AIMesh aiMesh) {
        var attribs = new ArrayList<VertexAttrib>();
        var data = new EnumMap<VertexAttrib, float[]>(VertexAttrib.class);
        var indices = toIntArray(streamBuf(aiMesh.mFaces()).flatMap(aiFace -> streamBuf(aiFace.mIndices())));
        possiblySetAttrib(attribs, data, POSITIONS, aiMesh.mVertices());
        possiblySetAttrib2d(attribs, data, TEX_COORDS, aiMesh.mTextureCoords(0));
        possiblySetAttrib(attribs, data, NORMALS, aiMesh.mNormals());
        possiblySetAttrib(attribs, data, TANGENTS, aiMesh.mTangents());
        possiblySetAttrib(attribs, data, BITANGENTS, aiMesh.mBitangents());

        var mesh = new Mesh(attribs, data, indices);
        var material = materials.get(aiMesh.mMaterialIndex());
        return material.buildRenderable(mesh);
    }

    private ModelNode toModelNode(AINode aiNode) {
        var transform = new Transformation(toMatrix4d(aiNode.mTransformation()));
        var children = Stream.concat(
                aiNode.mNumMeshes() == 0 ? Stream.empty()
                        : streamBuf(aiNode.mMeshes()).map(renderables::get),
                aiNode.mNumChildren() == 0 ? Stream.empty()
                        : streamBuf(aiNode.mChildren()).map(AINode::create).map(this::toModelNode)
        ).collect(Collectors.toList());
        return new ModelNode(transform, children);
    }
}
