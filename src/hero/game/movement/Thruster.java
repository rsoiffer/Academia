package hero.game.movement;

import static beige_engine.core.Core.dt;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrCore;
import hero.game.Controller;
import hero.game.Player;
import static hero.game.particles.ParticleTypes.FIRE;
import static hero.game.particles.ParticleTypes.SMOKE;
import java.util.Random;

public class Thruster extends MovementMode {

    public Thruster(Player player, Controller controller) {
        super(player, controller);
    }

    @Override
    public void onStep() {
        double t = controller.controller.trigger();
        if (t > .1) {
            Vec3d pullDir = controller.sideways();
            if (controller.controller == VrCore.LEFT) {
                pullDir = pullDir.mul(-1);
            }
//            pullDir = pullDir.lerp(controller.forwards().mul(-1), 1).normalize();
//            controller.player.applyForce(pullDir.mul(t * -10), .03);
            player.physics.applyForce(pullDir.mul(t * -1000));
//            double pullStrength = Math.exp(.02 * pullDir.dot(controller.player.velocity.velocity));
//            controller.player.velocity.velocity = controller.player.velocity.velocity.add(pullDir.mul(dt() * t * pullStrength * -10));
            for (int i = 0; i < 200 * t * dt(); i++) {
                var timeInPast = Math.random() * dt();
                var p = FIRE.addParticle();
                p.position = controller.pos().sub(player.physics.velocity().mul(timeInPast));
                p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + 4 * p.time)));
                p.velocity = player.physics.velocity().add(pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5));
                p.update(timeInPast);
            }
            for (int i = 0; i < 200 * t * dt() / 40; i++) {
                var timeInPast = Math.random() * dt();
                var p = SMOKE.addParticle();
                p.position = controller.pos().sub(player.physics.velocity().mul(timeInPast));
                p.scale = () -> new Vec3d(1, 1, 1).mul(Math.min(10 * p.time, .25 / (1 + .4 * p.time)));
                p.velocity = player.physics.velocity().add(pullDir.mul(10).add(MathUtils.randomInSphere(new Random())).mul(5));
                p.update(timeInPast);
            }

//            SDF shape2 = intersectionSmooth(3,
//                    cylinder(controller.pos(), pullDir, .5),
//                    halfSpace(controller.pos().add(pullDir.mul(5)), pullDir.mul(-1)),
//                    halfSpace(controller.pos(), pullDir));
//            AABB bounds2 = AABB.boundingBox(Arrays.asList(controller.pos().sub(10), controller.pos().add(10)));
//            iceModel.intersectionSDF(shape2.invert(), bounds2);
        }
    }
}
