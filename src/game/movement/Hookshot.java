package game.movement;

import static engine.core.Core.dt;
import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import engine.rendering.Platonics;
import engine.rendering.materials.ColorMaterial;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import static engine.vr.VrCore.TRIGGER;
import game.entities.Controller;
import game.entities.Player;

public class Hookshot extends MovementMode {

    public Vec3d hookPos, hookVel;
    public boolean grabbing;
    public ModelNode lineNode;

    public Hookshot(Player player, Controller controller) {
        super(player, controller);

        var model = add(new ModelComponent(this));

        var material = new ColorMaterial();
        material.color = new Vec3d(.5, .5, .5);
        lineNode = new ModelNode(material.buildRenderable(Platonics.cube));
        model.node.addChild(lineNode);

        model.beforeRender = () -> {
            lineNode.visible = hookPos != null;
            if (lineNode.visible) {
                Vec3d pos = controller.pos();
                Vec3d forwards = hookPos.sub(pos);
                Vec3d side = forwards.cross(new Vec3d(0, 0, 1)).setLength(.05);
                Vec3d up = forwards.cross(side).setLength(.05);
                Vec3d pos2 = pos.sub(side.div(2)).sub(up.div(2));
                lineNode.transform = Transformation.create(pos2, forwards, side, up);
            }
        };
    }

    @Override
    public void onStep() {
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            hookPos = controller.pos();
            hookVel = controller.forwards().mul(100);
        }
        if (controller.controller.buttonJustReleased(TRIGGER)) {
            hookPos = null;
            grabbing = false;
        }
        if (hookPos != null) {
            if (!grabbing) {
                hookPos = hookPos.add(hookVel.mul(dt()));
//                grabbing = controller.player.physics.manager.collisionShape.contains(hookPos);
            } else {
                Vec3d pullDir = hookPos.sub(controller.pos()).normalize();
                pullDir = pullDir.lerp(controller.forwards(), .2);
                player.physics.setVelocity(player.physics.velocity().lerp(
                        pullDir.mul(40), 1 - Math.exp(-1 * dt())));
            }
        }
    }
}
