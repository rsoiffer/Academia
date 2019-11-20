package hero.graphics;

import beige_engine.util.math.Transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelNode implements Renderable {

    public boolean visible = true;
    public Transformation transform = Transformation.IDENTITY;;
    private final List<Renderable> children;

    public ModelNode() {
        children = new ArrayList<>();
    }

    public ModelNode(Renderable renderable) {
        this(Arrays.asList(renderable));
    }

    public ModelNode(List<Renderable> children) {
        this.children = new ArrayList<>(children);
    }

    public ModelNode(Transformation transform, List<Renderable> children) {
        this.transform = transform;
        this.children = new ArrayList<>(children);
    }

    public void addChild(Renderable child) {
        children.add(child);
    }

    public void render(Transformation t, int pass) {
        if (visible) {
            t = t.mul(transform);
            for (var child : children) {
                child.render(t, pass);
            }
        }
    }
}
