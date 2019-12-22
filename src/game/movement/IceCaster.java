package game.movement;

import static engine.core.Core.dt;
import engine.physics.AABB;
import engine.rendering.surfaces.SDFSurface;
import engine.rendering.utils.SDF;
import static engine.rendering.utils.SDF.*;
import engine.util.math.MathUtils;
import engine.util.math.Vec3d;
import engine.vr.VrEyeCamera;
import game.entities.Controller;
import game.entities.Player;
import static game.particles.ParticleTypes.ICE;
import java.util.Arrays;
import java.util.Random;

public class IceCaster extends MovementMode {

    public static final SDFSurface iceModel = new SDFSurface(.5);

    public double timer;

    public IceCaster(Player player, Controller controller) {
        super(player, controller);
    }

    public static void createIce(Vec3d pos1, Vec3d pos2, Vec3d up) {
        double radius = 2, thickness = 2, negRadius = 2;
        Vec3d dir = pos2.sub(pos1);

        Vec3d side = dir.cross(up).normalize();
        Vec3d normal = dir.cross(side).normalize();
        var foot1 = pos1.add(normal.mul(1.5));
        var foot2 = pos2.add(normal.mul(1.5));

        SDF shape = intersectionSmooth(6,
                cylinder(foot1, dir, radius),
                halfSpace(foot1, normal),
                halfSpace(foot1.add(normal.mul(thickness)), normal.mul(-1)),
                halfSpace(foot1, dir),
                halfSpace(foot2, dir.mul(-1)));
        AABB bounds = AABB.boundingBox(Arrays.asList(foot1, foot2)).expand(radius);
        iceModel.unionSDF(shape, bounds);

        SDF shape2 = intersectionSmooth(6,
                cylinder(pos1, dir, negRadius),
                halfSpace(pos1, dir),
                halfSpace(pos2.add(dir.setLength(1)), dir.mul(-1))).invert();
        AABB bounds2 = AABB.boundingBox(Arrays.asList(pos1, pos2)).expand(negRadius);
        iceModel.intersectionSDF(shape2, bounds2);

        for (int i = 0; i < 500 * dt() * dir.length(); i++) {
            var sideAmt = Math.random() - .5;
            var p = ICE.addParticle();
            p.position = foot1.lerp(foot2, Math.random()).add(side.mul(sideAmt * 4));
            var randVel = side.mul(sideAmt * 10).add(normal.mul(-1))
                    .add(MathUtils.randomInSphere(new Random()).mul(2));
            p.velocity = dir.mul(1 / .3).add(randVel);
        }
    }

    @Override
    public void onStep() {
        if (controller.controller.trigger() > .01) {
            var goalDir = controller.forwards().lerp(VrEyeCamera.headPose().applyRotation(new Vec3d(1, 0, 0)), .2);

            double maxSpeed = 20;
            double accel = 10;
            double keepLine = 3;

            var vel = player.physics.velocity();
            var myUp = new Vec3d(0, 0, 1).cross(goalDir).cross(goalDir);

            var runPart = goalDir.setLength(Math.max(0, accel * (controller.controller.trigger() - vel.length() / maxSpeed)));
            var keepLinePart = goalDir.projectAgainst(vel).mul(vel.length() * keepLine);
            var antiGravPart = new Vec3d(0, 0, 9.81).projectOnto(myUp);
            var totalAccel = runPart.add(keepLinePart).add(antiGravPart);
            player.physics.applyForce(totalAccel.mul(100));

            timer += dt();
            if (timer > 0) {
                timer -= 1 / 30.;
                createIce(player.pose.position, player.pose.position.add(player.physics.velocity().mul(.3)), totalAccel);
            }
        }
    }
}
