package hero.game;

import engine.core.AbstractComponent;
import engine.core.AbstractEntity;
import hero.graphics.ModelNode;
import java.util.stream.Stream;

public class ModelBehavior extends AbstractComponent {

    public ModelNode node = new ModelNode();
    public boolean visible = true;

    public static Stream<ModelNode> allNodes() {
        return AbstractComponent.getAll(ModelBehavior.class).stream().filter(r -> r.visible).map(r -> r.node);
    }

    public ModelBehavior(AbstractEntity entity) {
        super(entity);
    }
}
