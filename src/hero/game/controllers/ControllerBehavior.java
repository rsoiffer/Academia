package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import static beige_engine.engine.Layer.POSTUPDATE;
import static hero.game.RenderableBehavior.createRB;

import beige_engine.graphics.opengl.Texture;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Vec2d;
import beige_engine.vr.Vive;
import hero.game.Player;
import hero.game.RenderableBehavior;
import hero.graphics.models.CustomModel;
import hero.graphics.models.OpenVRModel;
import hero.graphics.models.VoxelModel2;
import hero.graphics.renderables.ColorModel;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.ViveController;
import hero.graphics.renderables.ColorModelParticles;
import hero.graphics.renderables.DiffuseModel;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerBehavior extends Behavior {

    private static final Vec3d OFFSET = new Vec3d(0, 0, 1.2);

    public final RenderableBehavior renderable = require(RenderableBehavior.class);

    public ViveController controller;
    public Player player;
    public int myNum;
    public Transformation postTransform = Transformation.IDENTITY;
    public List<Behavior> children = new LinkedList();

    public Transformation controllerPose() {
        return controller.pose();
    }

    public Transformation controllerPose(double exaggeration) {
        return vrCoords()
                .translate(OFFSET.mul(-1))
                .scale(exaggeration)
                .translate(OFFSET)
                .mul(controller.poseRaw());
    }

    @Override
    public void createInner() {
        var ovrModel = new OpenVRModel(controller);
        var diffModel = new DiffuseModel(ovrModel, ovrModel.diffuseTexture);
        diffModel.roughness = .5;
        renderable.renderable = diffModel;

        var square = new CustomModel();
        square.addRectangle(new Vec3d(-.5, -.5, 0), new Vec3d(1, 0, 0), new Vec3d(0, 1, 0),
                new Vec2d(0, 0), new Vec2d(1, 0), new Vec2d(0, 1));
        square.createVAO();

        String[] textures = {"iron_man_icon.png", "spiderman_icon.png", "teleport_icon.png",
                "hulk_icon.png", "wings_icon.png", "frozone_icon.png"};
        for (int i = 0; i < 6; i++) {
            var icon = new DiffuseModel(square, Texture.load(textures[i]));
            icon.enableShadows = false;
            var iconRB = createRB(icon);
            var offset = new Vec3d(Math.cos(i * Math.PI / 3) - .5, Math.sin(i * Math.PI / 3), .25);
            if (controller == Vive.LEFT) {
                offset = offset.mul(new Vec3d(1, -1, 1));
            }
            var trans = Transformation.create(offset.mul(.08), Quaternion.fromAngleAxis(new Vec3d(0, 0, -Math.PI / 2)), .05);
            iconRB.beforeRender = () -> icon.t = getTransform().mul(trans);
            children.add(iconRB);
        }

        var selected = new DiffuseModel(square, Texture.load("selected_icon.png"));
        selected.enableShadows = false;
        var selectedRB = createRB(selected);
        selectedRB.beforeRender = () -> {
            var i = (7 - myNum) % 6;
            var offset = new Vec3d(Math.cos(i * Math.PI / 3) - .5, Math.sin(i * Math.PI / 3), .24);
            if (controller == Vive.LEFT) {
                offset = offset.mul(new Vec3d(1, -1, 1));
            }
            var trans = Transformation.create(offset.mul(.08), Quaternion.fromAngleAxis(new Vec3d(0, 0, -Math.PI / 2)), .05);
            selected.t = getTransform().mul(trans);
        };
        children.add(selectedRB);
    }

    @Override
    public void destroyInner() {
        children.forEach(Behavior::destroy);
    }

    public Vec3d forwards() {
        return controllerPose().applyRotation(new Vec3d(1, 0, 0));
    }

    public Transformation getTransform() {
        return postTransform.mul(controllerPose());
    }

    @Override
    public Layer layer() {
        return POSTUPDATE;
    }

    public Vec3d pos() {
        return controllerPose().position();
    }

    public Vec3d pos(double exaggeration) {
        return controllerPose(exaggeration).position();
    }

    public Vec3d sideways() {
        return controllerPose().applyRotation(new Vec3d(0, 1, 0));
    }

    @Override
    public void step() {
        ((DiffuseModel) renderable.renderable).t = getTransform();
    }

    public Vec3d upwards() {
        return controllerPose().applyRotation(new Vec3d(0, 0, 1));
    }

    public Transformation vrCoords() {
        return player.pose.getTransform().translate(new Vec3d(0, 0, -1));
    }
}
