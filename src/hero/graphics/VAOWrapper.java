package hero.graphics;

import beige_engine.graphics.opengl.VertexArrayObject;

import static org.lwjgl.opengl.GL11.*;

public class VAOWrapper {

    private final VertexArrayObject vao;
    private final int numFaces;

    public VAOWrapper(VertexArrayObject vao, int numFaces) {
        this.vao = vao;
        this.numFaces = numFaces;
    }

    public void draw() {
        vao.bind();
        glDrawElements(GL_TRIANGLES, numFaces * 3, GL_UNSIGNED_INT, 0);
    }
}
