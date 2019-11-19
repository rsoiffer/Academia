package hero.graphics;

import beige_engine.graphics.Camera;
import beige_engine.graphics.opengl.GLState;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.utils.MeshSimplifier;

import java.util.List;

import static beige_engine.util.math.MathUtils.*;

public interface Renderable {

    void render(Transformation t, int pass);

    abstract class BasicRenderable implements Renderable {

        private final VAOWrapper model;
        private Transformation t;

        public BasicRenderable(Mesh mesh) {
            this.model = mesh.getVAOW(attribs());
        }

        protected abstract List<VertexAttrib> attribs();

        protected void drawModel() {
            GLState.getShaderProgram().setUniform("model", t.matrix());
            model.render();
        }

        protected void drawModelOffset(Transformation t2) {
            GLState.getShaderProgram().setUniform("model", t.mul(t2).matrix());
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

    abstract class LODRenderable implements Renderable {

        private static final int NUM_LOD = 6;

        private int numLod = NUM_LOD;
        private double distOffset;
        private final VAOWrapper[] models = new VAOWrapper[NUM_LOD];
        private Transformation current;

        public LODRenderable(Mesh mesh) {
            distOffset = mesh.aabb.center().length() + mesh.aabb.size().length() / 2;
            mesh = MeshSimplifier.simplify(mesh, .01);
            for (int i = 0; i < NUM_LOD; i++) {
                this.models[i] = mesh.getVAOW(attribs());
                mesh = MeshSimplifier.simplify(mesh, .1 * Math.pow(2, i));
                if (mesh == null) {
                    numLod = i + 1;
                    break;
                }
            }
        }

        protected abstract List<VertexAttrib> attribs();

        private void drawInternal(Transformation t) {
            double lod = getLOD(t);
            double lodFrac = Math.max((lod - floor(lod)) * 10 - 9, 0);
            if (lod < numLod) {
                GLState.getShaderProgram().setUniform("lod", (float) lodFrac);
                GLState.getShaderProgram().setUniform("model", t.matrix());
                models[floor(lod)].render();
                if (lodFrac > 0 && ceil(lod) < numLod) {
                    GLState.getShaderProgram().setUniform("lod", (float) lodFrac - 1);
                    models[ceil(lod)].render();
                }
            }
        }

        protected void drawModel() {
            drawInternal(current);
        }

        protected void drawModelOffset(Transformation t) {
            drawInternal(current.mul(t));
        }

        private double getLOD(Transformation t) {
            var estimatedDist = t.apply(new Vec3d(0, 0, 0)).sub(Camera.camera3d.position).length();
            var lod = Math.log(Math.max(estimatedDist - distOffset, 0)) / Math.log(2) - 4;
            return Math.max(lod, 0);
        }

        public void render(Transformation t, int pass) {
            this.current = t;
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
