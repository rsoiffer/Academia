package game;

import engine.core.AbstractSystem;
import engine.core.Core;
import static engine.core.Core.dt;
import engine.core.Input;
import engine.core.Settings;
import static engine.graphics.Camera.camera3d;
import engine.samples.Behavior;
import static engine.samples.Behavior.BEHAVIOR_SYSTEM;
import engine.samples.FPSBehavior;
import engine.samples.QuitOnEscapeBehavior;
import static engine.util.math.MathUtils.clamp;
import static engine.util.math.MathUtils.round;
import engine.util.math.Vec3d;
import static game.world.World.BLOCK_HEIGHT;
import static game.world.World.BLOCK_WIDTH;
import static game.movement.IceCaster.iceModel;
import static game.particles.ParticleTypes.explosion;

import game.entities.Drone;
import game.world.World;
import engine.rendering.loading.AssimpLoader;
import engine.rendering.passes.RenderPipeline;
import engine.rendering.utils.SDF;
import static engine.rendering.utils.SDF.*;
import engine.physics.PhysicsComponent;
import engine.physics.PhysicsManager;
import game.entities.PinkBox;
import engine.physics.PoseComponent;
import engine.physics.AABB;
import java.util.Arrays;
import static org.lwjgl.glfw.GLFW.*;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class MainPC {

    public static void main(String[] args) {
        Settings.SHOW_OPENGL_DEBUG_INFO = false;
        Settings.SHOW_CURSOR = false;
        Settings.ENABLE_VSYNC = false;
        Settings.ANTI_ALIASING = 4;
        Core.init();

        Core.ROOT.add(BEHAVIOR_SYSTEM);

        new FPSBehavior();
        new QuitOnEscapeBehavior();

        var world = new World();

        var p = new PlayerPC(new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 10, 10), world.manager);

        var updateSystem = AbstractSystem.of(() -> {
            if (Input.keyJustPressed(GLFW_KEY_F) || Input.keyDown(GLFW_KEY_T)) {
                Drone d = new Drone(new Vec3d(8 * BLOCK_WIDTH - 10, 2 * BLOCK_HEIGHT - 12, 2.5), world.manager);
            }
            if (Input.mouseDown(0)) {
                var v = world.manager.raycast(camera3d.position, camera3d.facing());
                v.ifPresent(t -> {
                    explosion(camera3d.position.add(camera3d.facing().mul(t)), new Vec3d(0, 0, 0), round(10000 * dt()));
                });
            }
            if (Input.keyJustPressed(GLFW_KEY_B)) {
                var b = new PinkBox(camera3d.position.add(new Vec3d(0, 0, -2)), world.manager);
            }

            if (Input.mouseDown(1)) {
                SDF shape2 = intersectionSmooth(3,
                        cylinder(camera3d.position, camera3d.facing(), .5),
                        halfSpace(camera3d.position.add(camera3d.facing()), camera3d.facing()),
                        halfSpace(camera3d.position, camera3d.facing()));
                AABB bounds2 = AABB.boundingBox(Arrays.asList(camera3d.position.sub(10), camera3d.position.add(10)));
                iceModel.unionSDF(shape2, bounds2);
            }
        });
        Core.ROOT.add(updateSystem);
        AssimpLoader.load("drone model/optimized.fbx");

        var rp = new RenderPipeline(false);
        Core.ROOT.add(rp);

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

    public static class PlayerPC extends Behavior {

        public final PoseComponent pose;
        public final PhysicsComponent physics;

        public boolean flyMode = true;

        public PlayerPC(Vec3d position, PhysicsManager manager) {
            pose = add(new PoseComponent(this, position));
            physics = add(new PhysicsComponent(this, manager));

            var mass = new DxMass();
            mass.setSphereTotal(100, 1);
            physics.setMass(mass);

            var geom = dCreateSphere(physics.manager.space, 1);
            physics.setGeom(geom);
        }

        private static Vec3d getMoveDir() {
            Vec3d vel = new Vec3d(0, 0, 0);
            if (Input.keyDown(GLFW_KEY_W)) {
                vel = vel.add(camera3d.facing().setLength(1));
            }
            if (Input.keyDown(GLFW_KEY_A)) {
                vel = vel.add(camera3d.facing().cross(camera3d.up).setLength(-1));
            }
            if (Input.keyDown(GLFW_KEY_S)) {
                vel = vel.add(camera3d.facing().setLength(-1));
            }
            if (Input.keyDown(GLFW_KEY_D)) {
                vel = vel.add(camera3d.facing().cross(camera3d.up).setLength(1));
            }
            if (Input.keyDown(GLFW_KEY_SPACE)) {
                vel = vel.add(camera3d.up.setLength(1));
            }
            if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {
                vel = vel.add(camera3d.up.setLength(-1));
            }
            return vel;
        }

        @Override
        public void onStep() {
            camera3d.horAngle -= Input.mouseDelta().x * 16. / 3;
            camera3d.vertAngle -= Input.mouseDelta().y * 3;
            camera3d.vertAngle = clamp(camera3d.vertAngle, -1.55, 1.55);
            camera3d.position = pose.position;

            if (Input.keyJustPressed(GLFW_KEY_Q)) {
                flyMode = !flyMode;
            }

            if (flyMode) {

                var vel = getMoveDir().mul(20);
                physics.applyForce(vel.sub(physics.velocity()).mul(1000));
                physics.applyForce(new Vec3d(0, 0, 9.81 * physics.getMass()));

            } else {

                var vel = getMoveDir().mul(5).setZ(0);
                if (vel.length() > 0) {
                    physics.applyForce(vel.sub(physics.velocity()).setZ(0).mul(400));
                }
                if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
                    physics.setVelocity(physics.velocity().setZ(5));
                }
            }
        }
    }
}