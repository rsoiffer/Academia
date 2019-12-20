package engine.graphics.opengl;

import engine.core.Settings;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture extends GLObject {

    public static int MAG_FILTER = GL_NEAREST;

    final int type;
    public int num;
    private int width, height;

    public Texture(int type) {
        super(glGenTextures());
        this.type = type;
    }

    public static Texture load(String fileName) {
        int[] widthArray = new int[1];
        int[] heightArray = new int[1];
        int[] compArray = new int[1];
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load(Settings.TEXTURE_LOAD_FOLDER + fileName, widthArray, heightArray, compArray, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load image " + Settings.TEXTURE_LOAD_FOLDER + fileName + " : " + stbi_failure_reason());
        }

        Texture t = new Texture(GL_TEXTURE_2D);
        t.setParameter(GL_TEXTURE_MAX_LEVEL, 16);
        t.setParameter(GL_TEXTURE_MAX_ANISOTROPY, 16);
        t.setParameter(GL_TEXTURE_MAG_FILTER, MAG_FILTER);
        t.uploadData(widthArray[0], heightArray[0], image);
        return t;
    }

    @Override
    public void bind() {
        GLState.bindTexture(this);
    }

    @Override
    public void destroy() {
        glDeleteTextures(id);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setParameter(int name, int value) {
        bind();
        glTexParameteri(type, name, value);
    }

    public void setParameter(int name, float[] value) {
        bind();
        glTexParameterfv(type, name, value);
    }

    public void uploadData(int width, int height, ByteBuffer data) {
        this.width = width;
        this.height = height;
        bind();
        glTexImage2D(type, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(type);
    }
}