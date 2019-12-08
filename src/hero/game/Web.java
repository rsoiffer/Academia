package hero.game;

import beige_engine.engine.Behavior;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.Mesh;
import hero.graphics.drawables.ParticlesDS;
import hero.graphics.loading.RawMeshBuilder;
import hero.graphics.materials.Material;
import hero.graphics.materials.PBRMaterial;
import hero.physics.PhysicsBehavior;
import hero.physics.PhysicsManager;
import hero.physics.PoseBehavior;
import hero.physics.SpringJoint;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.ode4j.ode.DJoint;
import static org.ode4j.ode.OdeHelper.createBallJoint;
import static org.ode4j.ode.OdeHelper.createDBallJoint;
import org.ode4j.ode.internal.DxMass;
import static org.ode4j.ode.internal.DxSphere.dCreateSphere;

public class Web extends Behavior {

    private static final int NUM_PARTS = 100;
    private static final double WEB_WIDTH = .03;

    private static final Material webMaterial;
    private static final Mesh webMesh;

    static {
        webMaterial = PBRMaterial.load("unused/rope");
        var RMB = new RawMeshBuilder().addCylinderUV(new Vec3d(0, 0, 0), new Vec3d(1, 0, 0), WEB_WIDTH, 6, 1, 0, 1);
        RMB.smoothVertexNormals();
        webMesh = RMB.toMesh();
    }

    public final ModelBehavior model = require(ModelBehavior.class);

    public Vec3d initialPos, initialBaseVel, initialEndVel;
    public PhysicsManager manager;
    public List<PhysicsBehavior> toIgnore = new ArrayList<>();
    public double prefLength = 20;

    private List<WebPart> parts = new ArrayList<>();
    private List<SpringJoint> springs = new ArrayList<>();
    private DJoint finalJoint;

    public DJoint attachTo(PhysicsBehavior pb) {
        var myJoint = createDBallJoint(pb.manager.world);
        myJoint.attach(pb.getBody(), parts.get(0).physics.getBody());
        return myJoint;
    }

    @Override
    public void createInner() {
        var webParticles = new ParticlesDS(webMesh, () -> IntStream.range(0, NUM_PARTS - 1).mapToObj(i -> {
            var pos = parts.get(i).pose.position;
            var forwards = parts.get(i + 1).pose.position.sub(pos);
            var side = forwards.cross(new Vec3d(0, 0, 1)).normalize();
            var up = forwards.cross(side).normalize();
            return Transformation.create(pos, forwards, side, up);
        }));
        model.node.addChild(webMaterial.buildRenderable(webParticles));

        for (int i = 0; i < NUM_PARTS; i++) {
            var wj = new WebPart();
            wj.pose.position = initialPos;
            wj.physics.manager = manager;
            wj.create();
            wj.physics.setVelocity(initialBaseVel.lerp(initialEndVel, (double) i / NUM_PARTS));
            parts.add(wj);
        }
        for (var wj : parts) {
            wj.physics.ignore.addAll(toIgnore);
            parts.stream().map(wp -> wp.physics).forEach(wj.physics.ignore::add);
        }

        for (int i = 0; i < NUM_PARTS - 1; i++) {
            var wp1 = parts.get(i);
            var wp2 = parts.get(i + 1);
            var s = new SpringJoint(manager);
            s.kp = 1000 * NUM_PARTS;
            s.kd = 100;
            s.attach(wp1.physics.getBody(), wp2.physics.getBody());
            springs.add(s);
        }
    }

    @Override
    public void step() {
//        double totalLength = IntStream.range(0, NUM_PARTS - 1)
//                .mapToDouble(i -> parts.get(i).pose.position.sub(parts.get(i + 1).pose.position).length())
//                .sum();
//        prefLength = MathUtils.clamp(totalLength, prefLength - 10 * dt(), prefLength + dt());

        for (var s : springs) {
            s.prefLength = prefLength / NUM_PARTS;
            s.updateParams();
        }
        if (finalJoint == null) {
            var wp = parts.get(NUM_PARTS - 1);
            if (!wp.physics.hit.isEmpty()) {
                wp.physics.setPosition(wp.physics.lastClearPos);
                finalJoint = createBallJoint(manager.world);
                finalJoint.attach(wp.physics.getBody(), null);
            }
        }
    }

    private static class WebPart extends Behavior {

        public final PoseBehavior pose = require(PoseBehavior.class);
        public final PhysicsBehavior physics = require(PhysicsBehavior.class);

        @Override
        public void createInner() {
            var mass = new DxMass();
            mass.setSphereTotal(1, WEB_WIDTH);
            physics.setMass(mass);

            var geom = dCreateSphere(physics.manager.space, WEB_WIDTH);
            physics.setGeom(geom);

            physics.getBody().setLinearDamping(PhysicsManager.STEP_SIZE);
        }
    }
}
