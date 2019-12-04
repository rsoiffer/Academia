package hero.game.world;

import static beige_engine.util.math.MathUtils.floor;
import beige_engine.util.math.Vec2d;
import beige_engine.util.math.Vec3d;
import hero.game.World;
import static hero.game.World.*;
import hero.graphics.loading.RawMeshBuilder;
import hero.graphics.materials.Material;
import hero.graphics.materials.PBRMaterial;
import hero.physics.shapes.AABB;
import hero.physics.shapes.CollisionShape;
import java.util.Map;
import java.util.stream.Stream;

public class CityBlock extends Structure {

    private static final Material SIDEWALK_MAT = PBRMaterial.load("city/sidewalk");

    private final AABB bounds;

    public CityBlock(World world, double x, double y) {
        super(world);
        bounds = new AABB(new Vec3d(x - SIDEWALK_WIDTH, y - SIDEWALK_WIDTH, -500), new Vec3d(x + 2 * BUILDING_SIZE + SIDEWALK_WIDTH, y + 8 * BUILDING_SIZE + SIDEWALK_WIDTH, .1));

        for (int i2 = 0; i2 < 2; i2++) {
            for (int j2 = 0; j2 < 8; j2++) {
                double x2 = x + i2 * BUILDING_SIZE;
                double y2 = y + j2 * BUILDING_SIZE;
                double dist2 = new Vec2d(x2, y2).sub(1000).lengthSquared();
                double height = floor(Math.random() * (50 * Math.exp(-dist2 / 160000) + 50 * world.noise.noise2d(x2, y2, .005)) + 4) / 2 * 2 * FLOOR_HEIGHT;
                var bounds1 = new AABB(new Vec3d(x2, y2, 0), new Vec3d(x2 + BUILDING_SIZE, y2 + BUILDING_SIZE, height));
                addSubstructure(new Building(world, bounds1));

                if (Math.random() < .5) {
                    double height2 = height + height * .2 * (1 + Math.random());
                    double dist = 4;
                    var bounds2 = new AABB(new Vec3d(x2 + dist, y2 + dist, height),
                            new Vec3d(x2 + BUILDING_SIZE - dist, y2 + BUILDING_SIZE - dist, height2));
                    addSubstructure(new Building(world, bounds2));
                }
            }
        }
    }

    @Override
    public void build(Map<Material, RawMeshBuilder> models) {
        var rmb = getBuilder(models, SIDEWALK_MAT);
        rmb.addRectangleUV(bounds.lower.setZ(bounds.upper.z), bounds.size().setY(0).setZ(0), bounds.size().setX(0).setZ(0),
                new Vec2d(0, 0), new Vec2d(bounds.size().x / 2, 0), new Vec2d(0, bounds.size().y / 2));
        createWalls(new AABB(bounds.lower.setZ(0), bounds.upper), 2, 1, rmb);
    }

    @Override
    public Stream<CollisionShape> getCollisionShapes() {
        return Stream.of(bounds);
    }
}