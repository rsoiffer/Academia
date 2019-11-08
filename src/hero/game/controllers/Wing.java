package hero.game.controllers;

import beige_engine.engine.Behavior;
import static beige_engine.engine.Core.dt;
import beige_engine.engine.Layer;
import static hero.game.Player.POSTPHYSICS;
import hero.graphics.models.VoxelModel2;
import hero.graphics.renderables.ColorModel;
import static beige_engine.util.math.MathUtils.clamp;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;

public class Wing extends Behavior {

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public Vec3d prevPos = null;

    @Override
    public void createInner() {
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
    }
}
