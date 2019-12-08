package hero.game.controllers;

import beige_engine.engine.Behavior;
import beige_engine.engine.Layer;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import static beige_engine.vr.Vive.GRIP;
import static beige_engine.vr.Vive.TRIGGER;
import static hero.game.Player.POSTPHYSICS;
import hero.graphics.ModelNode;
import hero.graphics.Platonics;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.materials.ColorMaterial;
import hero.physics.PhysicsBehavior;
import hero.physics.PoseBehavior;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WebSlinger2 extends Behavior {

    public static final int NUM_JOINTS = 100;
    public static boolean godMode;

    public final ControllerBehavior controller = require(ControllerBehavior.class);

    public ModelNode webNode;
    public List<WebJoint> joints = new ArrayList<>();

    @Override
    public void createInner() {
        var material = new ColorMaterial();
        material.color = new Vec3d(1, 1, 1);
        var webParticles = new ParticlesDS(Platonics.cube, ()
                -> joints.stream().filter(wj -> wj.next != null).map(wj -> {
                    var pos = wj.pose.position;
                    var forwards = wj.next.pose.position.sub(pos);
                    var side = forwards.cross(new Vec3d(0, 0, 1)).setLength(.05);
                    var up = forwards.cross(side).setLength(.05);
                    var pos2 = pos.sub(side.div(2)).sub(up.div(2));
                    return Transformation.create(pos2, forwards, side, up);
                }));
        webNode = new ModelNode(material.buildRenderable(webParticles));
        controller.model.node.addChild(webNode);
    }

    @Override
    public void destroyInner() {
        joints.forEach(Behavior::destroy);
    }

    @Override
    public Layer layer() {
        return POSTPHYSICS;
    }

    @Override
    public void step() {
        if (controller.controller.buttonJustPressed(TRIGGER)) {
            joints.forEach(Behavior::destroy);
            joints.clear();
            for (int i = 0; i < NUM_JOINTS; i++) {
                var wj = new WebJoint();
                wj.pose.position = controller.pos();
                wj.physics.setVelocity(controller.player.physics.velocity().add(controller.forwards().mul(100. * i / NUM_JOINTS)));
                wj.physics.manager = controller.player.physics.manager;
                wj.create();
                joints.add(wj);
            }
            for (int i = 0; i < NUM_JOINTS - 1; i++) {
                joints.get(i).next = joints.get(i + 1);
            }
        }
        if (controller.controller.buttonJustReleased(TRIGGER)) {
            joints.forEach(Behavior::destroy);
            joints.clear();
        }

        double webForce = controller.controller.buttonDown(GRIP) ? 10 : .1;

        if (!joints.isEmpty()) {
            for (int iter = 0; iter < 100; iter++) {
                var prevVels = joints.stream().map(wj -> wj.physics.velocity()).collect(Collectors.toCollection(ArrayList::new));
                for (int i = 0; i < NUM_JOINTS - 1; i++) {
                    var wj = joints.get(i);
                    var dir = wj.next.pose.position.sub(wj.pose.position);
                    var dist = dir.length();
                    dir = dir.div(dist);

                    var minDist = 1e-20;
                    if (dist > minDist) {
                        var force = Math.min(webForce * 10 * (dist - minDist), 30);
                        wj.physics.applyForce(dir.mul(force));
                        wj.next.physics.applyForce(dir.mul(-force));
                    }

//                    var smoothing = .01;
//                    var velDiff = prevVels.get(i + 1).sub(prevVels.get(i));
//                    wj.physics.appl(velDiff.mul(smoothing), wj.pose.position);
//                    wj.next.physics.applyImpulse(velDiff.mul(-smoothing), wj.next.pose.position);
                }

//                var hand = joints.get(0);
//                hand.pose.position = controller.pos();
//                var momentum = hand.physics.momentum().add(controller.player.physics.momentum());
//                var newVel = momentum.div(hand.physics.mass + controller.player.physics.mass);
//                hand.physics.velocity = newVel;
//                controller.player.physics.velocity = newVel;
//
//                for (var wj : joints) {
//                    if (wj.stuck) {
//                        wj.physics.velocity = new Vec3d(0, 0, 0);
//                    }
//                }
            }
        }
        webNode.visible = !joints.isEmpty();
    }

    private class WebJoint extends Behavior {

        public final PoseBehavior pose = require(PoseBehavior.class);
        public final PhysicsBehavior physics = require(PhysicsBehavior.class);

        public WebJoint next;
        public boolean stuck;

        @Override
        public void createInner() {
//            physics.mass = 1;
//            physics.radius = .02;
//            physics.acceleration = new Vec3d(0, 0, -1);
        }

        @Override
        public void step() {
            if (next == null && !physics.hit.isEmpty()) {
                stuck = true;
//                physics.acceleration = new Vec3d(0, 0, 0);
            }
        }
    }
}
