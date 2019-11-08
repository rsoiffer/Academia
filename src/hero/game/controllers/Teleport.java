package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import static hero.game.Player.POSTPHYSICS;
import hero.game.RenderableBehavior;
import static hero.game.RenderableBehavior.createRB;
import hero.graphics.models.VoxelModel2;
import hero.graphics.renderables.ColorModel;
import hero.graphics.renderables.ColorModelParticles;
import hero.graphics.renderables.RenderableList;
import java.util.LinkedList;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import static beige_engine.vr.Vive.TRIGGER;

public class Teleport extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public RenderableBehavior markerRB;

    @Override
    public void createInner() {
        ColorModel markerModel = new ColorModel(VoxelModel2.load("singlevoxel.vox"));
        markerModel.color = new Vec3d(.6, .2, .8);

        ColorModelParticles arcModel = new ColorModelParticles(VoxelModel2.load("singlevoxel.vox"));
        arcModel.color = new Vec3d(.6, .2, .8);

        markerRB = createRB(new RenderableList(markerModel, arcModel));
        markerRB.beforeRender = () -> {
            Vec3d newPos = findPos();
            markerRB.visible = newPos != null;
            if (markerRB.visible) {
                double scale = Math.min(1, newPos.sub(controller.pos()).length() / 20);
                markerModel.t = Transformation.create(newPos.sub(scale / 2), Quaternion.IDENTITY, scale);

                arcModel.transforms = new LinkedList();
                Vec3d pos = controller.pos();
                Vec3d vel = controller.forwards();
                for (int i = 0; i < 100; i++) {
                    Vec3d pos2 = pos.add(vel.mul(.5));
                    if (controller.player.physics.wouldCollideAt(pos2)) {
                        break;
                    }
                    Vec3d dir = pos2.sub(pos);
                    double scale2 = Math.min(1, pos.sub(controller.pos()).length() / 20) / 4;
                    Vec3d dir1 = dir.cross(new Vec3d(0, 0, 1)).setLength(scale2);
                    Vec3d dir2 = dir1.cross(dir).setLength(scale2);
                    arcModel.transforms.add(Transformation.create(pos.sub(dir1.div(2)).sub(dir2.div(2)), dir, dir1, dir2));
                    pos = pos2;
                    vel = vel.add(new Vec3d(0, 0, -.005));
                }
            }
        };
    }

    @Override
    public void destroyInner() {
        markerRB.destroy();
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

    @Override
    public void step() {
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            Vec3d newPos = findPos();
            if (newPos != null) {
                controller.player.pose.position = newPos;
                // controller.player.velocity.velocity = new Vec3d(0, 0, 0);
            }
        }
    }
}
