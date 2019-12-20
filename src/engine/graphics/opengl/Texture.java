package engine.graphics.opengl;

import engine.core.Settings;
import engine.graphics.Color;
import engine.util.Resources;
import engine.util.math.Transformation;

import java.nio.ByteBuffer;
import static org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture extends GLObject {

    public static final Shader SPRITE_SHADER = Resources.loadShader("sprite");
    public static final VertexArrayObject SPRITE_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
                0.5f, 0.5f, 0, 1, 1,
                0.5f, -0.5f, 0, 1, 0,
                -0.5f, -0.5f, 0, 0, 0,
                -0.5f, 0.5f, 0, 0, 1
        });
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
        glEnableVertexAttribArray(1);
    });

    public static int MAG_FILTER = GL_NEAREST;

    final int type;
    public int num;
    private int width, height;

    public Texture(int type) {
        super(glGenTextures());
        this.type = type;
    }

    public static void drawTexture(Texture texture, Transformation t, Color color) {
        SPRITE_SHADER.setMVP(t);
        SPRITE_SHADER.setUniform("color", color);
        bindAll(texture, SPRITE_SHADER, SPRITE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public static Texture loadNew(String fileName) {
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
