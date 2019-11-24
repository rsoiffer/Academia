package hero.game;

import beige_engine.behaviors.FPSBehavior;
import beige_engine.behaviors.QuitOnEscapeBehavior;
import beige_engine.engine.Core;
import static beige_engine.engine.Core.dt;
import beige_engine.engine.Input;
import static beige_engine.engine.Layer.UPDATE;
import beige_engine.engine.Settings;
import static beige_engine.graphics.Camera.camera3d;
import static beige_engine.util.math.MathUtils.clamp;
import static beige_engine.util.math.MathUtils.round;
import beige_engine.util.math.Vec3d;
import static hero.game.World.BLOCK_HEIGHT;
import static hero.game.World.BLOCK_WIDTH;
import static hero.game.particles.ParticleTypes.explosion;
import hero.graphics.loading.AssimpLoader;
import hero.graphics.passes.RenderPipeline;
import static org.lwjgl.glfw.GLFW.*;

public class MainPC {

    public static void main(String[] args) {
        Settings.SHOW_OPENGL_DEBUG_INFO = false;
        Settings.SHOW_CURSOR = false;
        Settings.ENABLE_VSYNC = false;
        Settings.ANTI_ALIASING = 4;
        Core.init();

        new FPSBehavior().create();
        new QuitOnEscapeBehavior().create();

        World world = new World();
        world.create();

        FrozoneAI f = new FrozoneAI();
        f.player.pose.position = new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10);
        f.player.physics.world = world;
        f.create();

        UPDATE.onStep(() -> {
            if (Input.keyJustPressed(GLFW_KEY_F) || Input.keyDown(GLFW_KEY_T)) {
                Drone d = new Drone();
                d.pose.position = new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 12, 1.5);
                d.physics.world = world;
                d.create();
            }
            if (Input.mouseDown(0)) {
                var v = world.collisionShape.raycast(camera3d.position, camera3d.facing());
                v.ifPresent(t -> {
                    explosion(camera3d.position.add(camera3d.facing().mul(t)), new Vec3d(0, 0, 0), round(10000 * dt()));
                });
            }
        });
        AssimpLoader.load("drone model/optimized.fbx");

        RenderPipeline rp = new RenderPipeline();
        rp.create();

//        var timeOfDay = new Mutable<>(0.);
//        UPDATE.onStep(() -> {
//            timeOfDay.o += dt() * .6;
//            Vec3d baseDir = new Vec3d(0, -.15, 1).normalize();
//            rp.setSunDirection(Quaternion.fromAngleAxis(new Vec3d(0, timeOfDay.o, 0)).applyTo(baseDir));
//            var color = new Vec3d(10, 5 + 4 * Math.cos(timeOfDay.o), 4 + 4 * Math.cos(timeOfDay.o)).mul(.25);
//            rp.setSunColor(color);
//            rp.setSkyColor( new Color(.4, .7, 1, 1).multRGB(Math.pow(.51 + .49 * Math.cos(timeOfDay.o), 2)));
//        });
        Core.run();
    }

    public static void moveCamera(Player p) {
        camera3d.horAngle -= Input.mouseDelta().x * 16. / 3;
        camera3d.vertAngle -= Input.mouseDelta().y * 3;
        camera3d.vertAngle = clamp(camera3d.vertAngle, -1.55, 1.55);

        double flySpeed = 20;
        Vec3d vel = new Vec3d(0, 0, 0);
        if (Input.keyDown(GLFW_KEY_W)) {
            vel = vel.add(camera3d.facing().setLength(flySpeed));
        }
        if (Input.keyDown(GLFW_KEY_A)) {
            vel = vel.add(camera3d.facing().cross(camera3d.up).setLength(-flySpeed));
        }
        if (Input.keyDown(GLFW_KEY_S)) {
            vel = vel.add(camera3d.facing().setLength(-flySpeed));
        }
        if (Input.keyDown(GLFW_KEY_D)) {
            vel = vel.add(camera3d.facing().cross(camera3d.up).setLength(flySpeed));
        }
        if (Input.keyDown(GLFW_KEY_SPACE)) {
            vel = vel.add(camera3d.up.setLength(flySpeed));
        }
        if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {
            vel = vel.add(camera3d.up.setLength(-flySpeed));
        }
        p.physics.velocity = vel;
    }
}
