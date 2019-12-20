package game.entities;

import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import engine.rendering.Platonics;
import engine.rendering.materials.DiffuseMaterial;
import engine.samples.Behavior;
import engine.util.Resources;
import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import engine.vr.VrController;
import engine.vr.VrCore;
import static engine.vr.VrCore.footTransform;
import engine.vr.VrSide;
import static engine.vr.VrSide.LEFT_HAND;

public class Controller extends Behavior {

    private static final Vec3d OFFSET = new Vec3d(0, 0, 1.2);

    public final ModelComponent model;

    public final VrSide side;
    public final VrController controller;

    public int myNum = 0;

    private ModelNode selected;

    public Controller(VrSide side) {
        model = add(new ModelComponent(this));

        this.side = side;
        controller = VrCore.getController(side);

        model.node.addChild(Resources.loadOpenVRModel(controller));

        String[] textures = {"iron_man_icon.png", "spiderman_icon.png", "teleport_icon.png",
            "hulk_icon.png", "wings_icon.png", "frozone_icon.png"};
        for (int i = 0; i < 6; i++) {
            var iconMat = DiffuseMaterial.load("icons/" + textures[i]);
            iconMat.hasShadows = false;
            var icon = new ModelNode(iconMat.buildRenderable(Platonics.square));
            model.node.addChild(icon);

            var offset = new Vec3d(Math.cos(i * Math.PI / 3) - .5, Math.sin(i * Math.PI / 3), .25)
                    .mul(new Vec3d(1, side == LEFT_HAND ? -1 : 1, 1));
            var quat = Quaternion.fromAngleAxis(new Vec3d(0, 0, -Math.PI / 2));
            var trans = Transformation.create(offset.mul(.08), quat, .05);
            icon.transform = trans;
        }

        var selectedMat = DiffuseMaterial.load("icons/selected_icon.png");
        selectedMat.hasShadows = false;
        selected = new ModelNode(selectedMat.buildRenderable(Platonics.square));
        model.node.addChild(selected);
    }

    public Transformation controllerPose() {
        return controller.pose();
    }

    public Transformation controllerPose(double exaggeration) {
        return footTransform.get()
                .translate(OFFSET)
                .scale(exaggeration)
                .translate(OFFSET.mul(-1))
                .mul(controller.poseRaw());
    }

    public Vec3d forwards() {
        return controllerPose().applyRotation(new Vec3d(1, 0, 0));
    }

    @Override
    public void onStep() {
        model.node.transform = controllerPose();

        var i = (7 - myNum) % 6;
        var offset = new Vec3d(Math.cos(i * Math.PI / 3) - .5, Math.sin(i * Math.PI / 3), .24)
                .mul(new Vec3d(1, side == LEFT_HAND ? -1 : 1, 1));
        var quat = Quaternion.fromAngleAxis(new Vec3d(0, 0, -Math.PI / 2));
        var trans = Transformation.create(offset.mul(.08), quat, .05);
        selected.transform = trans;
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

    public Vec3d upwards() {
        return controllerPose().applyRotation(new Vec3d(0, 0, 1));
    }
}
