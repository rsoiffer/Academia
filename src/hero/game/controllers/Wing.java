package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import hero.graphics.restructure.Mesh;
import hero.graphics.restructure.ModelNode;
import hero.graphics.restructure.loading.VoxelModelLoader;
import hero.graphics.restructure.materials.ColorMaterial;

import static beige_engine.engine.Core.dt;
import static beige_engine.util.math.MathUtils.clamp;
import static hero.game.Player.POSTPHYSICS;

public class Wing extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public Vec3d prevPos = null;
    public ModelNode wingNode;

    @Override
    public void createInner() {
        var material = new ColorMaterial();
        material.color = new Vec3d(.3, .5, .1);
        wingNode = new ModelNode(new Mesh(VoxelModelLoader.load("singlevoxel.vox").rawMesh, material));
        controller.ovrNode.addChild(wingNode);
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
        Vec3d sideways = controller.sideways();
        if (controller.controller != Vive.LEFT) {
            sideways = sideways.mul(-1);
        }
        Vec3d pos = controller.pos(5).add(sideways.mul(1.5));
//        Vec3d pos = controller.pos().add(sideways.mul(.5));

        if (prevPos != null) {
            Vec3d wingVel = pos.sub(prevPos).div(dt());
            if (wingVel.lengthSquared() >= 1e-6) {
                Vec3d wingUp = controller.upwards();
                double C = -wingVel.normalize().dot(wingUp);
                if (C < 0) {
                    C *= .2;
                }
                double strength = 10 * C * wingVel.lengthSquared();
                if (Math.abs(strength) > 1e5) {
                    System.out.println(strength);
                }
                strength = clamp(strength, -1e5, 1e5);
                controller.player.physics.applyForce(wingUp.mul(strength), pos);
            }
        }
        prevPos = pos;

        if (!controller.player.physics.onGround) {
            double thrustStrength = 200;
            controller.player.physics.applyForce(controller.forwards().mul(thrustStrength), pos);
        }

        var size = new Vec3d(.8, 1.6, .05);
        var offset = new Vec3d(0, controller.controller == Vive.LEFT ? 1 : -1, 0);
        wingNode.transform = Transformation.create(offset.sub(size.div(2)), Quaternion.IDENTITY, size);
    }
}
