package hero.game;

import beige_engine.behaviors.FPSBehavior;
import beige_engine.behaviors.QuitOnEscapeBehavior;
import beige_engine.engine.Core;
import beige_engine.engine.Input;
import beige_engine.engine.Settings;

import static hero.game.World.BLOCK_HEIGHT;
import static hero.game.World.BLOCK_WIDTH;
import static beige_engine.graphics.Camera.camera3d;

import hero.graphics.passes.RenderPipeline;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static beige_engine.util.math.MathUtils.clamp;
import beige_engine.util.math.Vec3d;

public class MainPC {

    public static void main(String[] args) {
//        Settings.SHOW_OPENGL_DEBUG_INFO = false;
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

        for (int i = 0; i < 1; i++) {
            Car c1 = new Car();
            c1.pose.position = new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 12, 1.5);
            c1.physics.world = world;
            c1.create();
        }

        RenderPipeline rp = new RenderPipeline();
        rp.create();

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
