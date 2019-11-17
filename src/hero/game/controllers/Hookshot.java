package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.restructure.Mesh;
import hero.graphics.restructure.ModelNode;
import hero.graphics.restructure.loading.VoxelModelLoader;
import hero.graphics.restructure.materials.ColorMaterial;

import static beige_engine.engine.Core.dt;
import static beige_engine.vr.Vive.TRIGGER;
import static hero.game.Player.POSTPHYSICS;

public class Hookshot extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public Vec3d hookPos, hookVel;
    public boolean grabbing;
    public ModelNode lineNode;

    @Override
    public void createInner() {
        var material = new ColorMaterial();
        material.color = new Vec3d(.5, .5, .5);
        lineNode = new ModelNode(new Mesh(VoxelModelLoader.load("singlevoxel.vox").rawMesh, material));
        controller.modelNode.node.addChild(lineNode);
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
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
                grabbing = controller.player.physics.world.collisionShape.contains(hookPos);
            } else {
                Vec3d pullDir = hookPos.sub(controller.pos()).normalize();
                pullDir = pullDir.lerp(controller.forwards(), .2);
                controller.player.physics.velocity = controller.player.physics.velocity.lerp(
                        pullDir.mul(40), 1 - Math.exp(-1 * dt()));
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
