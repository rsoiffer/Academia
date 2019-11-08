package hero.game;

import beige_engine.behaviors.FPSBehavior;
import beige_engine.behaviors.QuitOnEscapeBehavior;
import beige_engine.engine.Behavior;
import beige_engine.engine.Core;
import static beige_engine.engine.Layer.UPDATE;
import beige_engine.engine.Settings;
import hero.game.Player;
import hero.game.World;
import static hero.game.World.BLOCK_HEIGHT;
import static hero.game.World.BLOCK_WIDTH;
import beige_engine.graphics.Camera;
import hero.game.controllers.*;
import hero.graphics.passes.RenderPipeline;
import beige_engine.util.Mutable;
import static beige_engine.util.math.MathUtils.floor;
import static beige_engine.util.math.MathUtils.mod;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import static beige_engine.vr.Vive.MENU;
import static beige_engine.vr.Vive.TRACKPAD;

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
            if (Vive.LEFT.buttonDown(MENU) && Vive.RIGHT.buttonDown(MENU)) {
                Vive.resetRightLeft();
                Vive.resetSeatedZeroPose();
            }
        });

        World world = new World();
        world.create();

        Player p = new Player();
        p.pose.position = new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10);
        p.physics.world = world;
        p.cameraOffset = new Vec3d(0, 0, -1);
        p.create();

//        Frozone f2 = new Frozone();
//        f2.player.position.position = new Vec3d(7 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10);
//        f2.player.hero.physics.world = world;
//        f2.player.cameraOffset = null;
//        f2.isPlayer = false;
//        f2.create();
//        Class[] c = {WebSlinger.class, Thruster.class, Hookshot.class, IceCaster.class,
//            Wing.class, Hand.class, Explosion.class, Teleport.class};
        Class[] c = {WebSlinger.class, Thruster.class, IceCaster.class,
            Wing.class, Hand.class, Teleport.class};
        Mutable<Integer> leftType = new Mutable(1);
        Mutable<Behavior> left = new Mutable(null);
        Mutable<Integer> rightType = new Mutable(1);
        Mutable<Behavior> right = new Mutable(null);

        UPDATE.onStep(() -> {
            if (Vive.LEFT.buttonJustPressed(TRACKPAD)) {
                if (left.o != null) {
                    left.o.destroy();
                    left.o = null;
                }
                Vec2d v = Vive.LEFT.trackpad();
                leftType.o = floor(mod(Math.atan2(v.y, v.x) / (2 * Math.PI), 1) * c.length);
            }
            if (left.o == null) {
                try {
                    left.o = (Behavior) c[leftType.o].newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                left.o.get(ControllerBehavior.class).controller = Vive.LEFT;
                left.o.get(ControllerBehavior.class).player = p;
                left.o.get(ControllerBehavior.class).myNum = leftType.o;
                left.o.create();
            }
            if (Vive.RIGHT.buttonJustPressed(TRACKPAD)) {
                if (right.o != null) {
                    right.o.destroy();
                    right.o = null;
                }
                Vec2d v = Vive.RIGHT.trackpad();
                rightType.o = floor(mod(Math.atan2(v.y, -v.x) / (2 * Math.PI), 1) * c.length);
            }
            if (right.o == null) {
                try {
                    right.o = (Behavior) c[rightType.o].newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                right.o.get(ControllerBehavior.class).controller = Vive.RIGHT;
                right.o.get(ControllerBehavior.class).player = p;
                right.o.get(ControllerBehavior.class).myNum = rightType.o;
                right.o.create();
            }
        });

        RenderPipeline rp = new RenderPipeline();
        rp.isVR = true;
        rp.create();

        Core.run();
    }
}
