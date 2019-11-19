package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import hero.graphics.ModelNode;

import java.util.Collection;
import java.util.stream.Stream;

import static beige_engine.engine.Layer.POSTUPDATE;

public class ModelNodeBehavior extends Behavior {

    public static final Collection<ModelNodeBehavior> ALL = track(ModelNodeBehavior.class);

    public ModelNode node = new ModelNode();
    public boolean visible = true;
    public Runnable beforeRender = null;

    public static Stream<ModelNode> allNodes() {
        return ALL.stream().filter(r -> r.visible).map(r -> r.node);
    }

    @Override
    public Layer layer() {
        return POSTUPDATE;
    }

    @Override
    public void step() {
        if (beforeRender != null) {
            beforeRender.run();
        }
    }
}
