package game.movement;

import static engine.core.Core.dt;
import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import engine.rendering.Platonics;
import engine.rendering.materials.ColorMaterial;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import static engine.vr.VrCore.TRIGGER;
import engine.vr.VrEyeCamera;
import game.entities.Controller;
import game.entities.Player;
import java.util.OptionalDouble;

public class Hand extends MovementMode {

    private static double jumpTimer = 0;

    public Vec3d handPos;
    public ModelNode armNode;

    public Hand(Player player, Controller controller) {
        super(player, controller);

        var model = add(new ModelComponent(this));

        var material = new ColorMaterial();
        material.color = new Vec3d(.5, 1, .4);
        armNode = new ModelNode(material.buildRenderable(Platonics.cube));
        model.node.addChild(armNode);

        model.beforeRender = () -> {
            Vec3d v = handPos;
            if (v == null) {
                Vec3d start = controller.pos();
                Vec3d dir = controller.forwards();
                OptionalDouble t = player.physics.manager.raycast(start, dir);
                if (t.isPresent() && t.getAsDouble() <= 8) {
                    v = start.add(dir.mul(t.getAsDouble()));
                }
            }
            armNode.visible = v != null;
            if (armNode.visible) {
                Vec3d pos = controller.pos();
                Vec3d forwards = v.sub(pos);
                Vec3d side = forwards.cross(new Vec3d(0, 0, 1)).setLength(.05);
                Vec3d up = forwards.cross(side).setLength(.05);
                Vec3d pos2 = pos.sub(side.div(2)).sub(up.div(2));
                armNode.transform = Transformation.create(pos2, forwards, side, up);
            }
        };
    }

    @Override
    public void onStep() {
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            Vec3d start = controller.pos();
            Vec3d dir = controller.forwards();
            OptionalDouble t = player.physics.manager.raycast(start, dir);
            if (t.isPresent() && t.getAsDouble() <= 8) {
                handPos = start.add(dir.mul(t.getAsDouble()));
            } else {
                handPos = null;
            }
        }
        if (controller.controller.buttonJustReleased(TRIGGER) && handPos != null) {
            handPos = null;
            if (jumpTimer > 0) {
                player.physics.setVelocity(VrEyeCamera.headPose().applyRotation(new Vec3d(1, 0, .5)).mul(25));
                jumpTimer = 0;
            } else {
                jumpTimer = .2;
            }
        }
        if (handPos != null) {
            jumpTimer -= dt();
            Vec3d dir = handPos.sub(controller.pos()).normalize();
            player.physics.setVelocity(player.physics.velocity()
                    .lerp(dir.mul(20), 1 - Math.pow(1e-6, dt())));
        } else if (!player.physics.onGround) {
            player.physics.applyForce(
                    VrEyeCamera.headPose().applyRotation(new Vec3d(1, 0, 0)).mul(300));
        }
    }
}
