package hero.game.movement;

import engine.samples.Behavior;
import hero.game.Controller;
import hero.game.Player;
import hero.graphics.ModelNode;

public abstract class MovementMode extends Behavior {

    public final Player player;
    public final Controller controller;

    public MovementMode(Player player, Controller controller) {
        this.player = player;
        this.controller = controller;
    }

    protected void updateModelNode(ModelNode modelNode) {
        modelNode.transform = controller.controllerPose();
    }
}
