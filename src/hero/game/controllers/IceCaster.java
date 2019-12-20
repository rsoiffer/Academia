package hero.game.controllers;

import static beige_engine.core.Core.dt;
import beige_engine.samples.Behavior;
import beige_engine.util.math.MathUtils;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrEyeCamera;
import static hero.game.particles.ParticleTypes.ICE;
import hero.graphics.utils.SDF;
import static hero.graphics.utils.SDF.*;
import hero.graphics.utils.SurfaceNet;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;
import hero.physics.shapes.AABB;
import java.util.Arrays;
import java.util.Random;

public class IceCaster extends Behavior {

    public static final SurfaceNet iceModel = new SurfaceNet(.5);

    public final Controller controller = new Controller(this);

    public double timer;
    public double timer2;

    public static void createIce(Vec3d pos1, Vec3d pos2, Vec3d up) {
        double radius = 2, thickness = 1.5, negRadius = 1.5;
        Vec3d dir = pos2.sub(pos1);

        SDF shape2 = intersectionSmooth(3,
                cylinder(pos1, dir, negRadius),
                halfSpace(pos1, dir),
                halfSpace(pos2.add(dir.setLength(1)), dir.mul(-1))).invert();
        AABB bounds2 = AABB.boundingBox(Arrays.asList(pos1.sub(negRadius), pos1.add(negRadius), pos2.sub(negRadius), pos2.add(negRadius)));
        iceModel.intersectionSDF(shape2, bounds2);

        Vec3d side = dir.cross(up).normalize();
        Vec3d normal = dir.cross(side).normalize();
        pos1 = pos1.add(normal);
        pos2 = pos2.add(normal);

        SDF shape = intersectionSmooth(3,
                cylinder(pos1, dir, radius),
                halfSpace(pos1, normal),
                halfSpace(pos1.add(normal.mul(thickness)), normal.mul(-1)),
                halfSpace(pos1, dir),
                halfSpace(pos2, dir.mul(-1)));
        AABB bounds = AABB.boundingBox(Arrays.asList(pos1.sub(radius), pos1.add(radius), pos2.sub(radius), pos2.add(radius)));
        iceModel.unionSDF(shape, bounds);

        for (int i = 0; i < 500 * dt() * dir.length(); i++) {
            var sideAmt = Math.random() - .5;
            var p = ICE.addParticle();
            p.position = pos1.lerp(pos2, Math.random()).add(side.mul(sideAmt * 4));
            var randVel = side.mul(sideAmt * 10).add(normal.mul(-1))
                    .add(MathUtils.randomInSphere(new Random()).mul(2));
            p.velocity = dir.mul(1 / .3).add(randVel);
        }
    }

    private void moveTowards(Vec3d vel) {
        timer += dt();
        timer2 += dt();

        PoseBehavior pose = controller.player.pose;
        PhysicsBehavior physics = controller.player.physics;

//        double height = controller.player.hero.physics.world.raycastDown(position.position);
//        double speedMod = 8 + 50 * Math.pow(.7, height);
//        Vec3d side = velocity.velocity.add(MathUtils.randomInSphere(new Random()).mul(1e-12)).cross(new Vec3d(0, 0, 1));
//        Vec3d accel = vel.add(side.mul(.01 * Math.sin(5 * timer))).add(new Vec3d(0, 0, speedMod));
//        velocity.velocity = velocity.velocity.add(accel.mul(dt()));
//        if (velocity.velocity.length() > 20) {
//            velocity.velocity = velocity.velocity.setLength(20);
//        }
        Vec3d along = vel.setLength(vel.normalize().dot(physics.velocity()));
        Vec3d opposite = physics.velocity().sub(along);
        Vec3d newVel = opposite.mul(Math.pow(.001, dt())).add(along).add(vel.mul(dt() * Math.exp(-.05 * physics.velocity().dot(vel.normalize()))));
        Vec3d accel = newVel.sub(physics.velocity());
        physics.setVelocity(newVel);

        if (timer2 > 0) {
            timer2 -= 1 / 30.;
            createIce(pose.position, pose.position.add(physics.velocity().mul(.3)), accel);
        }
    }

    @Override
    public void onStep() {
        if (controller.controller.trigger() > .01) {
            Vec3d goalDir = controller.forwards().lerp(VrEyeCamera.headPose().applyRotation(new Vec3d(1, 0, 0)), .2);
            moveTowards(goalDir.mul(10 * controller.controller.trigger()));
        }
    }
}
