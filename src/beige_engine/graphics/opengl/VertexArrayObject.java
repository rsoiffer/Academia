package beige_engine.graphics.opengl;

import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;

public class VertexArrayObject extends GLObject {

    private VertexArrayObject() {
        super(glGenVertexArrays());
    }

    public static VertexArrayObject createVAO(Runnable r) {
        VertexArrayObject vao = new VertexArrayObject();
        vao.bind();
        r.run();
        return vao;
    }

    @Override
    public void bind() {
        GLState.bindVertexArrayObject(this);
    }

    @Override
    public void destroy() {
        glDeleteVertexArrays(id);
    }
}
