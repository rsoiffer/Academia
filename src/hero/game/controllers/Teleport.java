package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.loading.VoxelModelLoader;
import hero.graphics.materials.ColorMaterial;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static beige_engine.engine.Core.dt;
import static beige_engine.vr.Vive.TRIGGER;
import static hero.game.Player.POSTPHYSICS;

public class Teleport extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public final List<Transformation> particles = new ArrayList<>();
    public ModelNode markerNode, arcNode;

    @Override
    public void createInner() {
        var material = new ColorMaterial();
        material.color = new Vec3d(.6, .2, .8);

        markerNode = new ModelNode(material.buildRenderable(Platonics.cube));
        controller.model.node.addChild(markerNode);
        arcNode = new ModelNode(material.buildRenderable(new ParticlesDS(Platonics.cube, particles::stream)));
        controller.model.node.addChild(arcNode);
    }

    public Vec3d findPos() {
        Vec3d pos = controller.pos();
        if (controller.player.physics.wouldCollideAt(pos)) {
            return null;
        }
        Vec3d vel = controller.forwards();
        for (int i = 0; i < 100; i++) {
            Vec3d pos2 = pos.add(vel.mul(.5));
            if (controller.player.physics.wouldCollideAt(pos2)) {
                return pos;
            }
            pos = pos2;
            vel = vel.add(new Vec3d(0, 0, -.005));
        }
        return pos;
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    public double dash = 0;
    public Vec3d dashDir = null;

    @Override
    public void step() {
        dash -= dt();
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            dashDir = controller.forwards().setLength(100);
            dash = .5;
        }
        if (dash > 0) {
            controller.player.physics.velocity = dashDir;
        } else {
            if (dashDir != null) {
                dashDir = null;
                controller.player.physics.velocity = controller.player.physics.velocity.mul(.01);
            }
        }

//        if (controller.controller.buttonJustPressed(TRIGGER)) {
//            Vec3d newPos = findPos();
//            if (newPos != null) {
//                controller.player.pose.position = newPos;
//                // controller.player.velocity.velocity = new Vec3d(0, 0, 0);
//            }
//        }

//        Vec3d newPos = findPos();
//        markerNode.visible = newPos != null;
//        if (markerNode.visible) {
//            double scale = Math.min(1, newPos.sub(controller.pos()).length() / 20);
//            markerNode.transform = Transformation.create(newPos.sub(scale / 2), Quaternion.IDENTITY, scale);
//
//            particles.clear();
//            Vec3d pos = controller.pos();
//            Vec3d vel = controller.forwards();
//            for (int i = 0; i < 100; i++) {
//                Vec3d pos2 = pos.add(vel.mul(.5));
//                if (controller.player.physics.wouldCollideAt(pos2)) {
//                    break;
//                }
//                Vec3d dir = pos2.sub(pos);
//                double scale2 = Math.min(1, pos.sub(controller.pos()).length() / 20) / 4;
//                Vec3d dir1 = dir.cross(new Vec3d(0, 0, 1)).setLength(scale2);
//                Vec3d dir2 = dir1.cross(dir).setLength(scale2);
//                particles.add(Transformation.create(pos.sub(dir1.div(2)).sub(dir2.div(2)), dir, dir1, dir2));
//                pos = pos2;
//                vel = vel.add(new Vec3d(0, 0, -.005));
//            }
//        }
    }
}
