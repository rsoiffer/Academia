package engine.rendering.utils;

import engine.graphics.opengl.GLObject;
import engine.graphics.opengl.Texture;
import engine.util.Resources;

import java.io.File;

public class PBRTexture extends GLObject {

    public static final int NUM_COMPONENTS = 8;
    private static final String[] NAMES = {
        "albedo", "normal", "metallic", "roughness",
        "ao", "height", "alpha", "emissive"};
    private static final PBRTexture DEFAULT = loadNew("default");

    private final Texture[] textures;

    private PBRTexture(String folder, String extension) {
        super(0);
        textures = new Texture[NUM_COMPONENTS];
        for (int i = 0; i < NUM_COMPONENTS; i++) {
            String filename = folder + "/" + NAMES[i] + "." + extension;
            if (new File("sprites/" + filename).isFile()) {
                textures[i] = Resources.loadTexture(filename);
                textures[i].num = i;
            } else {
                textures[i] = DEFAULT.textures[i];
            }
        }
    }

    public static PBRTexture loadNew(String folder) {
        return new PBRTexture(folder, "png");
    }

    @Override
    public void bind() {
        bindAll(textures);
    }

    @Override
    public void destroy() {
    }

    public boolean hasAlpha() {
        return textures[6] != DEFAULT.textures[6];
    }
}
