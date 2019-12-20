package hero.game;

import beige_engine.core.AbstractEntity;
import beige_engine.util.Noise;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import static hero.game.movement.IceCaster.iceModel;
import hero.game.world.CityBlock;
import hero.game.world.ParkBlock;
import hero.game.world.Structure;
import hero.graphics.ModelNode;
import hero.graphics.Renderable;
import hero.graphics.loading.RawMeshBuilder;
import static hero.graphics.loading.VoxelModelLoader.DIRS;
import hero.graphics.materials.Material;
import hero.graphics.materials.PBRMaterial;
import hero.physics.PhysicsManager;
import hero.physics.shapes.AABB;
import java.util.*;

public class World extends AbstractEntity {

    public static final double SIDEWALK_WIDTH = 4.2;

    public static final double FLOOR_HEIGHT = 4;
    public static final double BUILDING_SIZE = 32;
    public static final double STREET_WIDTH = 20;
    public static final double BLOCK_WIDTH = 2 * BUILDING_SIZE + STREET_WIDTH;
    public static final double BLOCK_HEIGHT = 8 * BUILDING_SIZE + STREET_WIDTH;

    public final ModelBehavior modelNode = add(new ModelBehavior(this));
    public final PhysicsManager manager = new PhysicsManager();

    public Random random = new Random();
    public Noise noise = new Noise(random);

    private final List<Structure> structures = new ArrayList();

    private final List<AABB> intersections = new ArrayList();
    private final List<AABB> roads = new ArrayList();
    // private final List<CapsuleShape> poles = new ArrayList();

    private void addStructure(Structure s) {
        structures.add(s);
        s.getSubstructures().forEach(this::addStructure);
    }

    public World() {
        for (int i = 0; i < 2000; i += BLOCK_WIDTH) {
            for (int j = 0; j < 2000; j += BLOCK_HEIGHT) {
                double buffer1 = 4;
                intersections.add(new AABB(new Vec3d(i - STREET_WIDTH + buffer1, j - STREET_WIDTH + buffer1, -500), new Vec3d(i - buffer1, j - buffer1, 0)));
                roads.add(new AABB(new Vec3d(i - buffer1, j - STREET_WIDTH + buffer1, -500), new Vec3d(i + 2 * BUILDING_SIZE + buffer1, j - buffer1, 0)));
                roads.add(new AABB(new Vec3d(i - STREET_WIDTH + buffer1, j - buffer1, -500), new Vec3d(i + buffer1, j + 8 * BUILDING_SIZE + buffer1, 0)));
                if (random.nextDouble() < .2) {
                    addStructure(new ParkBlock(this, i, j));
                } else {
                    addStructure(new CityBlock(this, i, j));
                }
//                poles.add(new CapsuleShape(new Vec3d(i - STREET_WIDTH + 3, j - STREET_WIDTH + 3, 0), new Vec3d(0, 0, 5), .1));
//                poles.add(new CapsuleShape(new Vec3d(i - 3, j - STREET_WIDTH + 3, 0), new Vec3d(0, 0, 5), .1));
//                poles.add(new CapsuleShape(new Vec3d(i - STREET_WIDTH + 3, j - 3, 0), new Vec3d(0, 0, 5), .1));
//                poles.add(new CapsuleShape(new Vec3d(i - 3, j - 3, 0), new Vec3d(0, 0, 5), .1));
            }
        }

        modelNode.node = createModelNode();

        new SurfaceNetEntity(iceModel, PBRMaterial.load("ice"), manager);
    }

    public ModelNode createModelNode() {
        var intersectionsModel = new RawMeshBuilder();
        for (AABB b : intersections) {
            intersectionsModel.addRectangleUV(b.lower.setZ(b.upper.z), b.size().setY(0).setZ(0), b.size().setX(0).setZ(0),
                    new Vec2d(.5, .5), new Vec2d(b.size().x / 6, 0), new Vec2d(0, b.size().y / 3));
        }

        var roadsModel = new RawMeshBuilder();
        for (AABB b : roads) {
            if (b.size().x >= b.size().y) {
                roadsModel.addRectangleUV(b.lower.setZ(b.upper.z), b.size().setY(0).setZ(0), b.size().setX(0).setZ(0),
                        new Vec2d(.5, .5), new Vec2d(b.size().x / 6, 0), new Vec2d(0, b.size().y / 3));
            } else {
                roadsModel.addRectangleUV(b.lower.setZ(b.upper.z), b.size().setX(0).setZ(0), b.size().setY(0).setZ(0),
                        new Vec2d(.5, .5), new Vec2d(b.size().y / 6, 0), new Vec2d(0, b.size().x / 3));
            }
        }

//        var polesModel = new RawMeshBuilder();
//        for (CapsuleShape c : poles) {
//            polesModel.addCylinderUV(c.pos, c.dir, c.radius, 16, STREET_WIDTH, 1, c.dir.length() / (2 * Math.PI * c.radius));
//        }
//        polesModel.smoothVertexNormals();
        var renderables = new LinkedList<Renderable>();
        var m = new HashMap<Material, RawMeshBuilder>();
        for (var s : structures) {
            s.build(m);
            s.getRenderables().forEach(renderables::add);
        }
        for (var e : m.entrySet()) {
            renderables.add(e.getKey().buildRenderable(e.getValue()));
        }
        renderables.add(PBRMaterial.load("city/road_empty").buildRenderable(intersectionsModel));
        renderables.add(PBRMaterial.load("city/road").buildRenderable(roadsModel));
//        renderables.add(PBRMaterial.load("city/concrete_pole").buildRenderable(polesModel));
        return new ModelNode(renderables);
    }

    public static void createWalls(AABB b, double scale, double scaleX, RawMeshBuilder m) {
        for (int j = 0; j < 4; j++) {
            Vec3d dir = DIRS.get(j).mul(b.size());
            Vec3d dir2 = DIRS.get(j < 2 ? j + 2 : 3 - j).mul(b.size());
            Vec3d dir3 = DIRS.get(5).mul(b.size());
            Vec3d v = b.lower.add(b.size().div(2)).add(dir.div(2)).sub(dir2.div(2)).sub(dir3.div(2));
            float texW = (float) (Math.abs(dir2.x + dir2.y + dir2.z) / scale / scaleX);
            float texH = (float) (Math.abs(dir3.x + dir3.y + dir3.z) / scale);
            m.addRectangleUV(v, dir2, dir3, new Vec2d(0, 0), new Vec2d(texW, 0), new Vec2d(0, texH));
        }
    }

    public double raycastDown(Vec3d pos) {
        double d = Double.MAX_VALUE;
//        for (AABB b : buildings) {
//            if (b.contains(pos.setZ(b.center().z))) {
//                d = Math.min(d, pos.z - b.upper.z);
//            }
//        }
        return d;
    }
}
