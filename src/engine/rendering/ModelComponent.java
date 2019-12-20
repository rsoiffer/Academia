package engine.rendering;

import engine.core.AbstractComponent;
import engine.core.AbstractEntity;
import engine.core.AbstractSystem;
import java.util.stream.Stream;

public class ModelComponent extends AbstractComponent {

    public static final AbstractSystem BEFORE_RENDER = AbstractSystem.perComponent(ModelComponent.class, model -> {
        if (model.beforeRender != null) {
            model.beforeRender.run();
        }
    });

    public ModelNode node = new ModelNode();
    public boolean visible = true;
    public Runnable beforeRender;

    public static Stream<ModelNode> allNodes() {
        return AbstractComponent.getAll(ModelComponent.class).stream().filter(r -> r.visible).map(r -> r.node);
    }

    public ModelComponent(AbstractEntity entity) {
        super(entity);
    }
}
