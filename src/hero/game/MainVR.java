package hero.game;

import beige_engine.core.*;
import beige_engine.graphics.Camera;
import beige_engine.samples.Behavior;
import static beige_engine.samples.Behavior.BEHAVIOR_SYSTEM;
import beige_engine.samples.FPSBehavior;
import beige_engine.samples.QuitOnEscapeBehavior;
import beige_engine.util.Mutable;
import beige_engine.util.math.MathUtils;
import static beige_engine.util.math.MathUtils.floor;
import static beige_engine.util.math.MathUtils.mod;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrCore;
import static beige_engine.vr.VrCore.*;
import static hero.game.World.BLOCK_HEIGHT;
import static hero.game.World.BLOCK_WIDTH;
import hero.game.controllers.*;
import hero.graphics.loading.AssimpLoader;
import hero.graphics.passes.RenderPipeline;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import static org.lwjgl.glfw.GLFW.*;

public class MainVR {

    public static void main(String[] args) {
        Settings.SHOW_OPENGL_DEBUG_INFO = false;
        Settings.ENABLE_VSYNC = false;
        Core.init();

        Core.ROOT.add(BEHAVIOR_SYSTEM);

        new FPSBehavior();
        new QuitOnEscapeBehavior();
        Camera.current = Camera.camera3d;
        VrCore.init();

        var vrUpdateSystem = AbstractSystem.of(() -> {
            VrCore.update();
//            if (LEFT.buttonDown(MENU) && RIGHT.buttonDown(MENU)) {
//                Vive.resetRightLeft();
//                Vive.resetSeatedZeroPose();
//            }
        });
        Core.ROOT.add(vrUpdateSystem);

        var world = new World();

        var p = new Player(new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10), world.manager);
        p.cameraOffset = new Vec3d(0, 0, -1);

        Class[] c = {WebSlinger.class, Thruster.class, IceCaster.class,
            Wing.class, Hand.class, Teleport.class};
        Mutable<Integer> leftType = new Mutable(1);
        Mutable<Behavior> left = new Mutable(null);
        Mutable<Integer> rightType = new Mutable(1);
        Mutable<Behavior> right = new Mutable(null);

        var updateSystem = AbstractSystem.of(() -> {
            if (LEFT.buttonJustPressed(TRACKPAD)) {
                if (left.o != null) {
                    left.o.destroy();
                    left.o = null;
                }
                Vec2d v = LEFT.trackpad();
                leftType.o = floor(mod(Math.atan2(v.y, v.x) / (2 * Math.PI), 1) * c.length);
            }
            if (left.o == null) {
                try {
                    left.o = (Behavior) c[leftType.o].newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                left.o.getComponent(Controller.class).controller = LEFT;
                left.o.getComponent(Controller.class).player = p;
                left.o.getComponent(Controller.class).myNum = leftType.o;
            }
            if (RIGHT.buttonJustPressed(TRACKPAD)) {
                if (right.o != null) {
                    right.o.destroy();
                    right.o = null;
                }
                Vec2d v = RIGHT.trackpad();
                rightType.o = floor(mod(Math.atan2(v.y, -v.x) / (2 * Math.PI), 1) * c.length);
            }
            if (right.o == null) {
                try {
                    right.o = (Behavior) c[rightType.o].newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                right.o.getComponent(Controller.class).controller = RIGHT;
                right.o.getComponent(Controller.class).player = p;
                right.o.getComponent(Controller.class).myNum = rightType.o;
            }

            if (LEFT.buttonJustPressed(MENU) || RIGHT.buttonJustPressed(MENU) || Input.keyJustPressed(GLFW_KEY_F) || Input.keyDown(GLFW_KEY_T)) {
                var position = p.pose.position.add(new Vec3d(0, 0, 100)).add(MathUtils.randomInSphere(new Random()).mul(50));
                Drone d = new Drone(position, world.manager);
            }
            if (Input.keyJustPressed(GLFW_KEY_G)) {
                WebSlinger.godMode = !WebSlinger.godMode;
            }
            for (var controller : Arrays.asList(LEFT, RIGHT)) {
                if (controller.buttonJustPressed(GRIP)) {
                    var drone = AbstractEntity.getAll(Drone.class).stream().max(Comparator.comparingDouble(d -> {
                        var delPos = d.pose.position.sub(controller.pose().position());
                        var dir = controller.pose().applyRotation(new Vec3d(1, 0, 0));
                        return delPos.normalize().dot(dir);
                    }));

                    var dir = controller.pose().applyRotation(new Vec3d(1, 0, 0));
                    Function<Missile, Vec3d> targeter = m -> dir;
                    if (!drone.isEmpty()) {
                        var delPos = drone.get().pose.position.sub(controller.pose().position());
                        var closeness = delPos.normalize().dot(dir);
                        if (closeness > .8) {
                            targeter = m -> {
                                if (!drone.get().isDestroyed()) {
                                    return drone.get().pose.position.sub(m.pose.position);
                                } else {
                                    return null;
                                }
                            };
                        }
                    }

                    Missile m = new Missile(controller.pose().position(), world.manager, targeter);
                    m.physics.ignore.add(p.physics);
                    m.isFriendly = true;
                    m.physics.setVelocity(p.physics.velocity());
                }
            }
        });
        Core.ROOT.add(updateSystem);
        AssimpLoader.load("drone model/optimized.fbx");
        AssimpLoader.load("bomb/mk83.obj");

        var rp = new RenderPipeline();
        rp.isVR = true;
        Core.ROOT.add(rp);

//        var timeOfDay = new Mutable<>(0.);
//        UPDATE.onStep(() -> {
//            timeOfDay.o += dt() * .6;
//            Vec3d baseDir = new Vec3d(.3, -.15, 1).normalize();
//            rp.setSunDirection(Quaternion.fromAngleAxis(new Vec3d(0, timeOfDay.o, 0)).applyTo(baseDir));
//        });
        Core.run();
    }
}
