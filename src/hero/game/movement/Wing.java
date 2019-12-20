package hero.game.movement;

import static beige_engine.core.Core.dt;
import static beige_engine.util.math.MathUtils.clamp;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrCore;
import hero.game.Controller;
import hero.game.ModelBehavior;
import hero.game.Player;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.materials.ColorMaterial;

public class Wing extends MovementMode {

    public final ModelBehavior model;

    public Vec3d prevPos = null;

    public Wing(Player player, Controller controller) {
        super(player, controller);

        model = add(new ModelBehavior(this));

        var material = new ColorMaterial();
        material.color = new Vec3d(.3, .5, .1);
        var wingNode = new ModelNode(material.buildRenderable(Platonics.cube));
        model.node.addChild(wingNode);

        var size = new Vec3d(.8, 1.6, .05);
        var offset = new Vec3d(-.3, controller.controller == VrCore.LEFT ? 1 : -1, 0);
        wingNode.transform = Transformation.create(offset.sub(size.div(2)), Quaternion.IDENTITY, size);
    }

    @Override
    public void onStep() {
        updateModelNode(model.node);

        Vec3d sideways = controller.sideways();
        if (controller.controller != VrCore.LEFT) {
            sideways = sideways.mul(-1);
        }
        Vec3d pos = controller.pos(5).add(sideways.mul(1.5)).sub(controller.forwards().mul(.3));
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
                player.physics.applyForce(wingUp.mul(strength));
            }
        }
        prevPos = pos;

        if (!player.physics.onGround) {
            double thrustStrength = 200;
            player.physics.applyForce(controller.forwards().mul(thrustStrength));
        }
    }
}
