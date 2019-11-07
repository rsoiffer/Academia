package hero.game.vr;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.engine.Layer;
import static hero.game.Player.POSTPHYSICS;
import hero.game.RenderableBehavior;
import static hero.game.RenderableBehavior.createRB;
import hero.graphics.models.VoxelModel2;
import hero.graphics.renderables.ColorModel;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import static beige_engine.vr.Vive.TRIGGER;

public class Hookshot extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public Vec3d hookPos, hookVel;
    public boolean grabbing;
    public ColorModel lineModel;
    public RenderableBehavior lineRB;

    @Override
    public void createInner() {
        controller.renderable.renderable = new ColorModel(VoxelModel2.load("controller_gray.vox"));
        lineModel = new ColorModel(VoxelModel2.load("singlevoxel.vox"));
        lineModel.color = new Vec3d(.5, .5, .5);
        lineRB = createRB(lineModel);
        lineRB.beforeRender = () -> {
            lineRB.visible = hookPos != null;
            if (lineRB.visible) {
                Vec3d pos = controller.pos();
                Vec3d forwards = hookPos.sub(pos);
                Vec3d side = forwards.cross(new Vec3d(0, 0, 1)).setLength(.05);
                Vec3d up = forwards.cross(side).setLength(.05);;
                Vec3d pos2 = pos.sub(side.div(2)).sub(up.div(2));
                lineModel.t = Transformation.create(pos2, forwards, side, up);
            }
        };
    }

    @Override
    public void destroyInner() {
        lineRB.destroy();
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
    }
}
