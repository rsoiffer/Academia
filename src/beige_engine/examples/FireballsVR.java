package beige_engine.examples;

import beige_engine.behaviors.FPSBehavior;
import beige_engine.behaviors.QuitOnEscapeBehavior;
import beige_engine.behaviors._3d.AccelerationBehavior3d;
import beige_engine.behaviors._3d.ModelBehavior;
import beige_engine.behaviors._3d.PositionBehavior3d;
import beige_engine.engine.Behavior;
import beige_engine.engine.Core;
import static beige_engine.engine.Layer.RENDER3D;
import static beige_engine.engine.Layer.UPDATE;
import beige_engine.engine.Settings;
import beige_engine.graphics.Camera;
import beige_engine.graphics.Color;
import beige_engine.graphics.voxels.VoxelModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joml.Matrix4d;
import beige_engine.util.math.Transformation;
import beige_engine.vr.Vive;
import static beige_engine.vr.Vive.MENU;

public class FireballsVR {

    public static List<Fireball> grabbed = new ArrayList();

    public static void main(String[] args) {
        Settings.BACKGROUND_COLOR = new Color(.4, .7, 1);
        Core.init();

        new FPSBehavior().create();
        new QuitOnEscapeBehavior().create();
        Camera.current = Camera.camera3d;
        Vive.init();
        // Vive.initRender(new Vec4d(.4, .7, 1, 1));

        UPDATE.onStep(() -> {
            Vive.update();
            if (Vive.LEFT.buttonDown(MENU) && Vive.RIGHT.buttonDown(MENU)) {
                Vive.resetRightLeft();
                Vive.resetSeatedZeroPose();
            }

//            if (Vive.RIGHT.buttonDown(TRACKPAD)) {
//                for (int i = 0; i < 10; i++) {
//                    Fireball f = new Fireball();
//                    f.position.position = Vive.RIGHT.position();
//                    f.acceleration.velocity.velocity = Vive.RIGHT.forwards().setLength(10).add(MathUtils.randomInSphere(new Random()));
//                    f.create();
//                }
//            }
//
//            if (Vive.RIGHT.buttonJustPressed(TRIGGER)) {
//                for (Fireball f : Fireball.ALL) {
//                    Vec3d delta = f.position.position.sub(Vive.RIGHT.position());
//                    double along = Vive.RIGHT.forwards().normalize().dot(delta);
//                    double side = Math.sqrt(delta.lengthSquared() - along * along);
//                    if (side < along * .2 + 1) {
//                        grabbed.add(f);
//                    }
//                }
//            }
//            if (Vive.RIGHT.buttonDown(TRIGGER)) {
//                Vec3d desiredVelocity = Vive.RIGHT.deltaPosition().div(dt()).mul(10);
//                for (Fireball f : grabbed) {
//                    f.acceleration.velocity.velocity = f.acceleration.velocity.velocity.lerp(desiredVelocity, .1);
//                }
//            }
//            if (Vive.RIGHT.buttonJustReleased(TRIGGER)) {
//                grabbed.clear();
//            }
        });

        RENDER3D.onStep(() -> {
            VoxelModel.load("dagger.vox").render(new Transformation(new Matrix4d()
                    .translate(Camera.camera3d.position.toJOML())
                    .mul(Vive.RIGHT.pose().matrix())
                    .translate(-.125, -.125, -.125)
                    .scale(1 / 32.)
            ), Color.WHITE);
            VoxelModel.load("dagger.vox").render(new Transformation(new Matrix4d()
                    .translate(Camera.camera3d.position.toJOML())
                    .mul(Vive.LEFT.pose().matrix())
                    .translate(-.125, -.125, -.125)
                    .scale(1 / 32.)
            ), Color.WHITE);
        });

        Core.run();
    }

    public static class Fireball extends Behavior {

        public static final Collection<Fireball> ALL = track(Fireball.class);

        public final PositionBehavior3d position = require(PositionBehavior3d.class);
        public final AccelerationBehavior3d acceleration = require(AccelerationBehavior3d.class);
        public final ModelBehavior model = require(ModelBehavior.class);

        @Override
        public void createInner() {
            model.model = VoxelModel.load("fireball.vox");
            model.scale = 1 / 32.;
        }

        @Override
        public void step() {
            acceleration.acceleration = acceleration.velocity.velocity.mul(-.2);
        }
    }
}
