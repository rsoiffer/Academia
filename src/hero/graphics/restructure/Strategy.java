package hero.graphics.restructure;

import beige_engine.graphics.opengl.GLState;
import beige_engine.util.math.Transformation;
import hero.graphics.models.Model;
import hero.graphics.restructure.materials.Material;

import java.util.List;

public interface Strategy {

    void render(Transformation t, int pass);

    abstract class BasicStrategy implements Strategy {

        protected final Material material;
        private final Model model;
        private Transformation t;

        public BasicStrategy(RawMesh rawMesh, Material material) {
            this.material = material;
            this.model = rawMesh.buildModel(attribs());
        }

        protected abstract List<VertexAttrib> attribs();

        protected void drawModel() {
            GLState.getShaderProgram().setUniform("model", t.matrix());
            model.render();
        }

        public void render(Transformation t, int pass) {
            this.t = t;
            switch (pass) {
                case 0:
                    renderGeom();
                    break;
                case 1:
                    renderShadow();
                    break;
            }
        }

        public abstract void renderGeom();
        public abstract void renderShadow();
    }
}
