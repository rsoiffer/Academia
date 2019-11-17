package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import beige_engine.vr.Vive;
import beige_engine.vr.ViveController;
import hero.game.ModelNodeBehavior;
import hero.game.Player;
import hero.graphics.restructure.Mesh;
import hero.graphics.restructure.ModelNode;
import hero.graphics.restructure.loading.OpenVRLoader;
import hero.graphics.restructure.loading.RawMeshBuilder;
import hero.graphics.restructure.materials.DiffuseMaterial;

import java.util.LinkedList;
import java.util.List;

import static beige_engine.engine.Layer.POSTUPDATE;

public class ControllerBehavior extends Behavior {

    private static final Vec3d OFFSET = new Vec3d(0, 0, 1.2);

    public final ModelNodeBehavior modelNode = require(ModelNodeBehavior.class);

    public ViveController controller;
    public Player player;
    public int myNum;
    public Transformation postTransform = Transformation.IDENTITY;
    public List<Behavior> children = new LinkedList();
    public ModelNode ovrNode;

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
        ovrNode = new OpenVRLoader(controller).rootNode;
        modelNode.node.addChild(ovrNode);

        var squareRM = new RawMeshBuilder()
                .addRectangleUV(new Vec3d(-.5, -.5, 0), new Vec3d(1, 0, 0), new Vec3d(0, 1, 0),
                        new Vec2d(0, 0), new Vec2d(1, 0), new Vec2d(0, 1))
                .toRawMesh();

        String[] textures = {"iron_man_icon.png", "spiderman_icon.png", "teleport_icon.png",
                "hulk_icon.png", "wings_icon.png", "frozone_icon.png"};
        for (int i = 0; i < 6; i++) {
            var iconMat = DiffuseMaterial.load(textures[i]);
            iconMat.hasShadows = false;
            var icon = new ModelNode(new Mesh(squareRM, iconMat));
            ovrNode.addChild(icon);

            var offset = new Vec3d(Math.cos(i * Math.PI / 3) - .5, Math.sin(i * Math.PI / 3), .25);
            if (controller == Vive.LEFT) {
                offset = offset.mul(new Vec3d(1, -1, 1));
            }
            var trans = Transformation.create(offset.mul(.08), Quaternion.fromAngleAxis(new Vec3d(0, 0, -Math.PI / 2)), .05);
            icon.transform = trans;
        }

        var selectedMat = DiffuseMaterial.load("selected_icon.png");
        selectedMat.hasShadows = false;
        var selected = new ModelNode(new Mesh(squareRM, selectedMat));
        ovrNode.addChild(selected);

        modelNode.beforeRender = () -> {
            ovrNode.transform = getTransform();

            var i = (7 - myNum) % 6;
            var offset = new Vec3d(Math.cos(i * Math.PI / 3) - .5, Math.sin(i * Math.PI / 3), .24);
            if (controller == Vive.LEFT) {
                offset = offset.mul(new Vec3d(1, -1, 1));
            }
            var trans = Transformation.create(offset.mul(.08), Quaternion.fromAngleAxis(new Vec3d(0, 0, -Math.PI / 2)), .05);
            selected.transform = trans;
        };
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

    public Vec3d upwards() {
        return controllerPose().applyRotation(new Vec3d(0, 0, 1));
    }

    public Transformation vrCoords() {
        return player.pose.getTransform().translate(new Vec3d(0, 0, -1));
    }
}
