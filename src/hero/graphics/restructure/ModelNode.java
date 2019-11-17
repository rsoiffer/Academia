package hero.graphics.restructure;

import beige_engine.util.math.Transformation;
import hero.graphics.renderables.Renderable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelNode {

    public boolean visible = true;
    public Transformation transform;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

    public ModelNode() {
        this(Transformation.IDENTITY, Collections.emptyList(), Collections.emptyList());
    }

    public ModelNode(Mesh mesh) {
        this(Transformation.IDENTITY, Collections.singletonList(mesh), Collections.emptyList());
    }

    public ModelNode(Transformation transform, List<Mesh> meshes, List<ModelNode> children) {
        this.transform = transform;
        this.meshes = new ArrayList<>(meshes);
        this.children = new ArrayList<>(children);
    }

    public void addChild(ModelNode child) {
        children.add(child);
    }

    public void render(Transformation t, int pass) {
        if (visible) {
            t = t.mul(transform);
            for (var mesh : meshes) {
                mesh.render(t, pass);
            }
            for (var child : children) {
                child.render(t, pass);
            }
        }
    }
}
