package engine.rendering;

import engine.core.AbstractComponent;
import engine.core.AbstractEntity;

import java.util.stream.Stream;

public class ModelComponent extends AbstractComponent {

    public ModelNode node = new ModelNode();
    public boolean visible = true;

    public static Stream<ModelNode> allNodes() {
        return AbstractComponent.getAll(ModelComponent.class).stream().filter(r -> r.visible).map(r -> r.node);
    }

    public ModelComponent(AbstractEntity entity) {
        super(entity);
    }
}
