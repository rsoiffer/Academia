package hero.game;

import beige_engine.behaviors.FPSBehavior;
import beige_engine.behaviors.QuitOnEscapeBehavior;
import beige_engine.engine.Behavior;
import beige_engine.engine.Core;
import beige_engine.engine.Input;
import static beige_engine.engine.Layer.UPDATE;
import beige_engine.engine.Settings;
import beige_engine.graphics.Camera;
import beige_engine.util.Mutable;
import beige_engine.util.math.MathUtils;
import static beige_engine.util.math.MathUtils.floor;
import static beige_engine.util.math.MathUtils.mod;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import static beige_engine.vr.Vive.*;
import static hero.game.World.BLOCK_HEIGHT;
import static hero.game.World.BLOCK_WIDTH;
import hero.game.controllers.*;
import hero.graphics.loading.AssimpLoader;
import hero.graphics.passes.RenderPipeline;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import static org.lwjgl.glfw.GLFW.*;

public class MainVR {

    public static void main(String[] args) {
        Settings.SHOW_OPENGL_DEBUG_INFO = false;
        Settings.ENABLE_VSYNC = false;
        Core.init();

        new FPSBehavior().create();
        new QuitOnEscapeBehavior().create();
        Camera.current = Camera.camera3d;
        Vive.init();

        UPDATE.onStep(() -> {
            Vive.update();
//            if (LEFT.buttonDown(MENU) && RIGHT.buttonDown(MENU)) {
//                Vive.resetRightLeft();
//                Vive.resetSeatedZeroPose();
//            }
        });

        World world = new World();
        world.create();

        Player p = new Player();
        p.pose.position = new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10);
        p.physics.manager = world.manager;
        p.cameraOffset = new Vec3d(0, 0, -1);
        p.create();

        Class[] c = {WebSlinger.class, Thruster.class, IceCaster.class,
            Wing.class, Hand.class, Teleport.class};
        Mutable<Integer> leftType = new Mutable(1);
        Mutable<Behavior> left = new Mutable(null);
        Mutable<Integer> rightType = new Mutable(1);
        Mutable<Behavior> right = new Mutable(null);

        UPDATE.onStep(() -> {
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
                left.o.get(ControllerBehavior.class).controller = LEFT;
                left.o.get(ControllerBehavior.class).player = p;
                left.o.get(ControllerBehavior.class).myNum = leftType.o;
                left.o.create();
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
                right.o.get(ControllerBehavior.class).controller = RIGHT;
                right.o.get(ControllerBehavior.class).player = p;
                right.o.get(ControllerBehavior.class).myNum = rightType.o;
                right.o.create();
            }
        });

        UPDATE.onStep(() -> {
            if (LEFT.buttonJustPressed(MENU) || RIGHT.buttonJustPressed(MENU) || Input.keyJustPressed(GLFW_KEY_F) || Input.keyDown(GLFW_KEY_T)) {
                Drone d = new Drone();
                d.pose.position = p.pose.position.add(new Vec3d(0, 0, 100))
                        .add(MathUtils.randomInSphere(new Random()).mul(50));
                d.physics.manager = world.manager;
                d.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_G)) {
                WebSlinger.godMode = !WebSlinger.godMode;
            }
            for (var controller : Arrays.asList(LEFT, RIGHT)) {
                if (controller.buttonJustPressed(GRIP)) {
                    var drone = Drone.ALL.stream().max(Comparator.comparingDouble(d -> {
                        var delPos = d.pose.position.sub(controller.pose().position());
                        var dir = controller.pose().applyRotation(new Vec3d(1, 0, 0));
                        return delPos.normalize().dot(dir);
                    }));

                    Missile m = new Missile();
                    m.pose.position = controller.pose().position();
                    m.physics.setVelocity(p.physics.velocity());
                    m.physics.manager = world.manager;
                    m.isFriendly = true;
                    if (drone.isEmpty()) {
                        var dir = controller.pose().applyRotation(new Vec3d(1, 0, 0));
                        m.targetDir = () -> dir;
                    } else {
                        var delPos = drone.get().pose.position.sub(controller.pose().position());
                        var dir2 = controller.pose().applyRotation(new Vec3d(1, 0, 0));
                        var closeness = delPos.normalize().dot(dir2);

                        if (closeness > .8) {
                            m.targetDir = () -> {
                                if (drone.get().isCreated()) {
                                    return drone.get().pose.position.sub(m.pose.position);
                                } else {
                                    return null;
                                }
                            };
                        } else {
                            var dir = controller.pose().applyRotation(new Vec3d(1, 0, 0));
                            m.targetDir = () -> dir;
                        }

                    }
                    m.create();
                }
            }
        });
        AssimpLoader.load("drone model/optimized.fbx");
        AssimpLoader.load("bomb/mk83.obj");

        RenderPipeline rp = new RenderPipeline();
        rp.isVR = true;
        rp.create();

//        var timeOfDay = new Mutable<>(0.);
//        UPDATE.onStep(() -> {
//            timeOfDay.o += dt() * .6;
//            Vec3d baseDir = new Vec3d(.3, -.15, 1).normalize();
//            rp.setSunDirection(Quaternion.fromAngleAxis(new Vec3d(0, timeOfDay.o, 0)).applyTo(baseDir));
//        });
        Core.run();
    }
}
