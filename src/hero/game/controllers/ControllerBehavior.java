package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import static beige_engine.engine.Layer.POSTUPDATE;
import hero.game.Player;
import hero.game.RenderableBehavior;
import hero.graphics.models.OpenVRModel;
import hero.graphics.renderables.ColorModel;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.ViveController;
import hero.graphics.renderables.DiffuseModel;

public class ControllerBehavior extends Behavior {

    private static final Vec3d OFFSET = new Vec3d(0, 0, 1.2);

    public final RenderableBehavior renderable = require(RenderableBehavior.class);

    public ViveController controller;
    public Player player;
    public Transformation postTransform = Transformation.IDENTITY;

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
