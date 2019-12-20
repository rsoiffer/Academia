package game.movement;

import static engine.core.Core.dt;
import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import engine.rendering.Platonics;
import engine.rendering.materials.ColorMaterial;
import static engine.util.math.MathUtils.clamp;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import engine.vr.VrCore;
import game.entities.Controller;
import game.entities.Player;

public class Wing extends MovementMode {

    public final ModelComponent model;

    public Vec3d prevPos = null;

    public Wing(Player player, Controller controller) {
        super(player, controller);

        model = add(new ModelComponent(this));

        var material = new ColorMaterial();
        material.color = new Vec3d(.3, .5, .1);
        var wingNode = new ModelNode(material.buildRenderable(Platonics.cube));
        model.node.addChild(wingNode);

        var size = new Vec3d(.8, 1.6, .05);
        var offset = new Vec3d(-.3, controller.controller == VrCore.LEFT ? 1 : -1, 0);
        wingNode.transform = Transformation.create(offset.sub(size.div(2)), Quaternion.IDENTITY, size);

        model.beforeRender = () -> updateModelNode(model.node);
    }

    @Override
    public void onStep() {
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
