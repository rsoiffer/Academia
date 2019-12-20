package hero.game.controllers;

import static beige_engine.core.Core.dt;
import beige_engine.samples.Behavior;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import static beige_engine.vr.VrCore.TRIGGER;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.materials.ColorMaterial;

public class Hookshot extends Behavior {

    public final Controller controller = new Controller(this);

    public Vec3d hookPos, hookVel;
    public boolean grabbing;
    public ModelNode lineNode;

    public Hookshot() {
        var material = new ColorMaterial();
        material.color = new Vec3d(.5, .5, .5);
        lineNode = new ModelNode(material.buildRenderable(Platonics.cube));
        controller.model.node.addChild(lineNode);
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
                controller.player.physics.setVelocity(controller.player.physics.velocity().lerp(
                        pullDir.mul(40), 1 - Math.exp(-1 * dt())));
            }
        }

        lineNode.visible = hookPos != null;
        if (lineNode.visible) {
            Vec3d pos = controller.pos();
            Vec3d forwards = hookPos.sub(pos);
            Vec3d side = forwards.cross(new Vec3d(0, 0, 1)).setLength(.05);
            Vec3d up = forwards.cross(side).setLength(.05);
            Vec3d pos2 = pos.sub(side.div(2)).sub(up.div(2));
            lineNode.transform = Transformation.create(pos2, forwards, side, up);
        }
    }
}
