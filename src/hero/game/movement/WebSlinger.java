package hero.game.movement;

import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import static engine.vr.VrCore.TRIGGER;
import hero.game.Controller;
import hero.game.ModelBehavior;
import hero.game.Player;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.materials.ColorMaterial;
import java.util.OptionalDouble;

public class WebSlinger extends MovementMode {

    public Vec3d web;
    public double prefLength;
    public ModelNode webNode;

    public static boolean godMode;

    public WebSlinger(Player player, Controller controller) {
        super(player, controller);

        var model = add(new ModelBehavior(this));

        var material = new ColorMaterial();
        material.color = new Vec3d(1, 1, 1);
        webNode = new ModelNode(material.buildRenderable(Platonics.cube));
        model.node.addChild(webNode);
    }

    @Override
    public void onStep() {
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            Vec3d start = controller.pos();
            Vec3d dir = controller.controllerPose().applyRotation(new Vec3d(1, 0, -.2)).normalize();
            OptionalDouble t = player.physics.manager.raycast(start, dir);
            if (t.isPresent()) {
                web = start.add(dir.mul(t.getAsDouble()));
                prefLength = t.getAsDouble() - 4;
            } else {
                web = null;
            }
        }
        if (controller.controller.buttonJustReleased(TRIGGER)) {
            web = null;
        }
        if (web != null) {
            double exag = 10;
            prefLength = Math.min(prefLength, web.sub(controller.pos(exag)).length() - controller.controller.trigger());
            Vec3d pullDir = web.sub(controller.pos(exag)).normalize();
            double strength = (godMode ? 10000 : 1000) * Math.max(controller.pos(exag).sub(web).length() - prefLength, 0);
            player.physics.applyForce(pullDir.mul(strength));

            double thrustStrength = 200;
            player.physics.applyForce(controller.forwards().mul(thrustStrength));

//            Vec3d pullDir = web.sub(controller.pos()).normalize();
//            pullDir = pullDir.lerp(controller.controller.forwards(), .2);
//            controller.player.applyForce(pullDir.mul(20), .05);
//
//            double pullStrength = Math.exp(-.02 * pullDir.dot(controller.player.velocity.velocity));
//            controller.player.velocity.velocity = controller.player.velocity.velocity.add(pullDir.mul(pullStrength * dt() * 20));
        }

        webNode.visible = web != null;
        if (webNode.visible) {
            Vec3d pos = controller.pos();
            Vec3d forwards = web.sub(pos);
            Vec3d side = forwards.cross(new Vec3d(0, 0, 1)).setLength(.05);
            Vec3d up = forwards.cross(side).setLength(.05);
            Vec3d pos2 = pos.sub(side.div(2)).sub(up.div(2));
            webNode.transform = Transformation.create(pos2, forwards, side, up);
        }
    }
}
