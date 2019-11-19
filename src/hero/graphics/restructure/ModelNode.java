package hero.graphics.restructure;

import beige_engine.util.math.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelNode {

    public boolean visible = true;
    public Transformation transform;
    private final List<Strategy> strategies;
    private final List<ModelNode> children;

    public ModelNode() {
        this(Transformation.IDENTITY, Collections.emptyList(), Collections.emptyList());
    }

    public ModelNode(Strategy strategy) {
        this(Transformation.IDENTITY, Collections.singletonList(strategy), Collections.emptyList());
    }

    public ModelNode(Transformation transform, List<Strategy> strategies, List<ModelNode> children) {
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
