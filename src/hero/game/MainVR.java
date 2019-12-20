package hero.game;

import beige_engine.core.*;
import beige_engine.graphics.Camera;
import static beige_engine.samples.Behavior.BEHAVIOR_SYSTEM;
import beige_engine.samples.FPSBehavior;
import beige_engine.samples.QuitOnEscapeBehavior;
import beige_engine.util.math.MathUtils;
import static beige_engine.util.math.MathUtils.floor;
import static beige_engine.util.math.MathUtils.mod;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.VrCore;
import static beige_engine.vr.VrCore.*;
import beige_engine.vr.VrSide;
import static beige_engine.vr.VrSide.LEFT_HAND;
import static hero.game.World.BLOCK_HEIGHT;
import static hero.game.World.BLOCK_WIDTH;
import hero.game.movement.*;
import hero.graphics.loading.AssimpLoader;
import hero.graphics.passes.RenderPipeline;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Random;
import java.util.function.Function;
import static org.lwjgl.glfw.GLFW.*;

public class MainVR {

    public static void main(String[] args) {
        Settings.SHOW_OPENGL_DEBUG_INFO = false;
        Settings.ENABLE_VSYNC = false;
        Core.init();

        initVR();

        Core.ROOT.add(BEHAVIOR_SYSTEM);

        new FPSBehavior();
        new QuitOnEscapeBehavior();
        Camera.current = Camera.camera3d;

        var world = new World();

        var player = new Player(new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10), world.manager);
        player.cameraOffset = new Vec3d(0, 0, -1);
        createMovementModeSelector(player);

        var cheats = AbstractSystem.of(() -> {
            if (LEFT.buttonJustPressed(MENU) || RIGHT.buttonJustPressed(MENU) || Input.keyJustPressed(GLFW_KEY_F) || Input.keyDown(GLFW_KEY_T)) {
                var position = player.pose.position.add(new Vec3d(0, 0, 100)).add(MathUtils.randomInSphere(new Random()).mul(50));
                new Drone(position, world.manager);
            }
            if (Input.keyJustPressed(GLFW_KEY_G)) {
                WebSlinger.godMode = !WebSlinger.godMode;
            }
        });
        Core.ROOT.add(cheats);

        var fireMissiles = AbstractSystem.of(() -> {
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

                    var m = new Missile(controller.pose().position(), world.manager, targeter);
                    m.physics.ignore.add(player.physics);
                    m.isFriendly = true;
                    m.physics.setVelocity(player.physics.velocity());
                }
            }
        });
        Core.ROOT.add(fireMissiles);

        var rp = new RenderPipeline(true);
        Core.ROOT.add(rp);

        AssimpLoader.load("drone model/optimized.fbx");
        AssimpLoader.load("bomb/mk83.obj");

//        var timeOfDay = new Mutable<>(0.);
//        UPDATE.onStep(() -> {
//            timeOfDay.o += dt() * .6;
//            Vec3d baseDir = new Vec3d(.3, -.15, 1).normalize();
//            rp.setSunDirection(Quaternion.fromAngleAxis(new Vec3d(0, timeOfDay.o, 0)).applyTo(baseDir));
//        });
        Core.run();
    }

    public static void createMovementModeSelector(Player player) {
        var controllers = new EnumMap<VrSide, Controller>(VrSide.class);
        var movementModes = new EnumMap<VrSide, MovementMode>(VrSide.class);
        Class[] c = {WebSlinger.class, Thruster.class, IceCaster.class, Wing.class, Hand.class, Teleport.class};

        for (var hand : VrSide.values()) {
            controllers.put(hand, new Controller(hand));
            movementModes.put(hand, new Thruster(player, controllers.get(hand)));
        }
        var selectorSystem = AbstractSystem.perEntity(Controller.class, controller -> {
            var currentMode = movementModes.get(controller.side);
            if (controller.controller.buttonJustPressed(TRACKPAD)) {
                var v = controller.controller.trackpad();
                var idx = floor(mod(Math.atan2(v.y, (controller.side == LEFT_HAND ? 1 : -1) * v.x) / (2 * Math.PI), 1) * c.length);
                controller.myNum = idx;
                if (!c[idx].equals(currentMode.getClass())) {
                    currentMode.destroy();
                    try {
                        var newMode = c[idx].getConstructor(Player.class, Controller.class).newInstance(player, controller);
                        movementModes.put(controller.side, (MovementMode) newMode);
                    } catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                            | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        Core.ROOT.add(selectorSystem);
    }

    public static void initVR() {
        VrCore.init();
        while (VrCore.LEFT == null || VrCore.RIGHT == null) {
            System.out.println("Failed to pair controllers, retrying...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            VrCore.initControllers();
        }
        Core.ROOT.add(AbstractSystem.of(() -> VrCore.update()));
    }
}
