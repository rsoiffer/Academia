package hero.game.world;

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

public class ParkBlock extends Structure {

    private static final Material GRASS_MAT = PBRMaterial.load("city/grass");

    private final AABB bounds;

    public ParkBlock(World world, double x, double y) {
        super(world);
        bounds = new AABB(new Vec3d(x - SIDEWALK_WIDTH, y - SIDEWALK_WIDTH, -500), new Vec3d(x + 2 * BUILDING_SIZE + SIDEWALK_WIDTH, y + 8 * BUILDING_SIZE + SIDEWALK_WIDTH, .1));

        for (int k = 0; k < 15; k++) {
            double x2 = x + world.random.nextDouble() * 2 * BUILDING_SIZE;
            double y2 = y + world.random.nextDouble() * 8 * BUILDING_SIZE;
            addSubstructure(new Tree(world, new Vec3d(x2, y2, 0)));
        }
    }

    @Override
    public void build(Map<Material, RawMeshBuilder> models) {
        var rmb = getBuilder(models, GRASS_MAT);
        rmb.addRectangleUV(bounds.lower.setZ(bounds.upper.z), bounds.size().setY(0).setZ(0), bounds.size().setX(0).setZ(0),
                new Vec2d(0, 0), new Vec2d(bounds.size().x / 2, 0), new Vec2d(0, bounds.size().y / 2));
        createWalls(new AABB(bounds.lower.setZ(0), bounds.upper), 2, 1, rmb);
    }

    @Override
    public Stream<CollisionShape> getCollisionShapes() {
        return Stream.of(bounds);
    }
}
