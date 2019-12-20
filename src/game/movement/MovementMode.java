package game.movement;

import engine.samples.Behavior;
import game.entities.Controller;
import game.entities.Player;
import engine.rendering.ModelNode;

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
