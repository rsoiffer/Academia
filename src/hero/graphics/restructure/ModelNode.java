package hero.graphics.restructure;

import beige_engine.util.math.Transformation;
import hero.graphics.renderables.Renderable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelNode {

    public Transformation transform;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

    public ModelNode() {
        this(Transformation.IDENTITY, Collections.emptyList(), Collections.emptyList());
    }

    public ModelNode(Mesh mesh) {
        this(Transformation.IDENTITY, Collections.singletonList(mesh), Collections.emptyList());
    }
    public ModelNode(Transformation transform, Mesh mesh) {
        this(transform, Collections.singletonList(mesh), Collections.emptyList());
    }

    public ModelNode(Transformation transform, List<Mesh> meshes, List<ModelNode> children) {
        this.transform = transform;
        this.meshes = new ArrayList<>(meshes);
        this.children = new ArrayList<>(children);
    }

    public void addChild(ModelNode child) {
        children.add(child);
    }

    public Renderable buildRenderable() {
        return new Renderable() {
            @Override
            public void renderGeomInner(Transformation t) {
                render(t, 0);
            }
            @Override
            public void renderShadowInner(Transformation t) {
                render(t, 1);
            }
        };
    }

    public void render(Transformation t, int pass) {
        t = t.mul(transform);
        for (var mesh : meshes) {
            mesh.render(t, pass);
        }
        for (var child : children) {
            child.render(t, pass);
        }
    }
}
