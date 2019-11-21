package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.EyeCamera;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.loading.VoxelModelLoader;
import hero.graphics.materials.ColorMaterial;

import java.util.OptionalDouble;

import static beige_engine.engine.Core.dt;
import static beige_engine.vr.Vive.TRIGGER;
import static hero.game.Player.POSTPHYSICS;

public class Hand extends Behavior {

    private static double jumpTimer = 0;

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public Vec3d handPos;
    public ModelNode armNode;

    @Override
    public void createInner() {
        var material = new ColorMaterial();
        material.color = new Vec3d(.5, 1, .4);
        armNode = new ModelNode(material.buildRenderable(Platonics.cube));
        controller.model.node.addChild(armNode);
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            Vec3d start = controller.pos();
            Vec3d dir = controller.forwards();
            OptionalDouble t = controller.player.physics.world.collisionShape.raycast(start, dir);
            if (t.isPresent() && t.getAsDouble() <= 8) {
                handPos = start.add(dir.mul(t.getAsDouble()));
            } else {
                handPos = null;
            }
        }
        if (controller.controller.buttonJustReleased(TRIGGER) && handPos != null) {
            handPos = null;
            if (jumpTimer > 0) {
                controller.player.physics.velocity = EyeCamera.headPose().applyRotation(new Vec3d(1, 0, .5)).mul(25);
                jumpTimer = 0;
            } else {
                jumpTimer = .2;
            }
        }
        if (handPos != null) {
            jumpTimer -= dt();
            Vec3d dir = handPos.sub(controller.pos()).normalize();
            controller.player.physics.velocity = controller.player.physics.velocity
                    .lerp(dir.mul(20), 1 - Math.pow(1e-6, dt()));
        } else if (!controller.player.physics.onGround) {
            controller.player.physics.applyForce(
                    EyeCamera.headPose().applyRotation(new Vec3d(1, 0, 0)).mul(300),
                    controller.player.physics.centerOfMass.get());
        }

        Vec3d v = handPos;
        if (v == null) {
            Vec3d start = controller.pos();
            Vec3d dir = controller.forwards();
            OptionalDouble t = controller.player.physics.world.collisionShape.raycast(start, dir);
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
    }
}
