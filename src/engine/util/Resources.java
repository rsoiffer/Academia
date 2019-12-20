package engine.util;

import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.rendering.Mesh;
import engine.rendering.Renderable;
import engine.rendering.loading.AssimpLoader;
import engine.rendering.loading.OpenVRLoader;
import engine.rendering.loading.VoxelModelLoader;
import engine.rendering.utils.PBRTexture;
import engine.vr.VrController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Resources {

    private static final Cache<String, Renderable> ASSIMP_MODEL_CACHE = new Cache<>(AssimpLoader::loadNew);
    private static final Cache<String, PBRTexture> PBR_TEXTURE_CACHE = new Cache<>(PBRTexture::loadNew);
    private static final Cache<VrController, Renderable> OPENVR_MODEL_CACHE = new Cache<>(OpenVRLoader::loadNew);
    private static final Cache<String, Shader> SHADER_CACHE = new Cache<>(Shader::loadNew);
    private static final Cache<String, Texture> TEXTURE_CACHE = new Cache<>(Texture::loadNew);
    private static final Cache<String, Mesh> VOXEL_MODEL_CACHE = new Cache<>(VoxelModelLoader::loadNew);

    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }

    public static Renderable loadAssimpModel(String path) { return ASSIMP_MODEL_CACHE.get(path); }

    public static byte[] loadFileAsBytes(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String loadFileAsString(String path) {
        return new String(loadFileAsBytes(path));
    }

    public static PBRTexture loadPBRTexture(String path) { return PBR_TEXTURE_CACHE.get(path); }

    public static Renderable loadOpenVRModel(VrController controller) { return OPENVR_MODEL_CACHE.get(controller); }

    public static Shader loadShader(String name) { return SHADER_CACHE.get(name); }

    public static Texture loadTexture(String path) {
        return TEXTURE_CACHE.get(path);
    }

    public static Mesh loadVoxelModel(String path) { return VOXEL_MODEL_CACHE.get(path); }
}
