package beige_engine.graphics.sprites;

import beige_engine.graphics.Color;
import beige_engine.graphics.opengl.BufferObject;
import static beige_engine.graphics.opengl.GLObject.bindAll;
import beige_engine.graphics.opengl.Shader;
import beige_engine.graphics.opengl.Texture;
import beige_engine.graphics.opengl.VertexArrayObject;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import beige_engine.util.math.Transformation;

public class Sprite {

    private static final Map<String, Sprite> SPRITE_CACHE = new HashMap();

    public static Sprite load(String fileName) {
        if (!SPRITE_CACHE.containsKey(fileName)) {
            Sprite s = new Sprite(fileName);
            SPRITE_CACHE.put(fileName, s);
        }
        return SPRITE_CACHE.get(fileName);
    }

    public static final Shader SPRITE_SHADER = Shader.load("sprite");

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

    private final Texture texture;

    private Sprite(String fileName) {
        this.texture = Texture.load(fileName);
    }

    public void draw(Transformation t, Color color) {
        drawTexture(texture, t, color);
    }

    public static void drawTexture(Texture texture, Transformation t, Color color) {
        SPRITE_SHADER.setMVP(t);
        SPRITE_SHADER.setUniform("color", color);
        bindAll(texture, SPRITE_SHADER, SPRITE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
