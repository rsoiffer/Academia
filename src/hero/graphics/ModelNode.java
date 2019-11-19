package hero.graphics;

import beige_engine.util.math.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelNode {

    public boolean visible = true;
    public Transformation transform;
    private final List<Renderable> strategies;
    private final List<ModelNode> children;

    public ModelNode() {
        this(Transformation.IDENTITY, Collections.emptyList(), Collections.emptyList());
    }

    public ModelNode(Renderable renderable) {
        this(Transformation.IDENTITY, Collections.singletonList(renderable), Collections.emptyList());
    }

    public ModelNode(Transformation transform, List<Renderable> strategies, List<ModelNode> children) {
        this.transform = transform;
        this.strategies = new ArrayList<>(strategies);
        this.children = new ArrayList<>(children);
    }

    public void addChild(ModelNode child) {
        children.add(child);
    }

    public void render(Transformation t, int pass) {
        if (visible) {
            t = t.mul(transform);
            for (var strategy : strategies) {
                strategy.render(t, pass);
            }
            for (var child : children) {
                child.render(t, pass);
            }
        }
    }
}
