package hero.game.controllers;

import static beige_engine.core.Core.dt;
import beige_engine.samples.Behavior;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import static beige_engine.vr.VrCore.TRIGGER;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import static hero.graphics.VertexAttrib.NORMALS;
import static hero.graphics.VertexAttrib.POSITIONS;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.loading.RawMeshBuilder;
import hero.graphics.materials.ColorMaterial;
import java.util.ArrayList;
import java.util.List;

public class Teleport extends Behavior {

    public final Controller controller = new Controller(this);

    public final List<Transformation> particles = new ArrayList<>();
    public ModelNode markerNode, arcNode;

    public Teleport() {
        var material = new ColorMaterial();
        material.color = new Vec3d(.6, .2, .8);

        markerNode = new ModelNode(material.buildRenderable(Platonics.cube));
        controller.model.node.addChild(markerNode);
        arcNode = new ModelNode(material.buildRenderable(new ParticlesDS(Platonics.cube, particles::stream)));
        controller.model.node.addChild(arcNode);

        var bladeMat = new ColorMaterial();
        bladeMat.color = new Vec3d(0, 0, 0);
        bladeMat.emissive = new Vec3d(1, 4, 2).mul(12);
        var mesh = new RawMeshBuilder(POSITIONS, NORMALS)
                .addCylinder(new Vec3d(.05, 0, -.05), new Vec3d(1, 0, 0), .015, 12);
        var blade = new ModelNode(bladeMat.buildRenderable(mesh));
        controller.ovrNode.addChild(blade);
    }

    public double dash = 0;
    public Vec3d dashDir = null;

    @Override
    public void onStep() {
        dash -= dt();
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            dashDir = controller.forwards().setLength(100);
            dash = .5;
        }
        if (dash > 0) {
//            controller.player.physics.velocity = dashDir;
        } else {
            if (dashDir != null) {
                dashDir = null;
//                controller.player.physics.velocity = controller.player.physics.velocity.mul(.01);
            }
        }

        var startPos = controller.pos().add(controller.upwards().mul(-.05));
//        var v = controller.player.physics.manager.collisionShape.raycast(startPos, controller.forwards());
//        v.ifPresent(t -> {
//            if (t < 1)
//            explosion(startPos.add(controller.forwards().mul(t)), new Vec3d(0, 0, 0), round(1000 * dt()), .02);
//        });

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
