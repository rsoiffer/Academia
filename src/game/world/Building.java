package game.world;

import engine.physics.AABB;
import static engine.physics.StaticShape.box;
import engine.rendering.loading.RawMeshBuilder;
import engine.rendering.materials.DiffuseMaterial;
import engine.rendering.materials.Material;
import engine.rendering.materials.PBRMaterial;
import engine.util.math.Vec2d;
import static game.world.World.FLOOR_HEIGHT;
import static game.world.World.createWalls;
import java.util.Map;

public class Building extends Structure {

    private static final int NUM_WALL_TYPES = 15;
    private static final double[] WALL_SCALES = {2, 9, 3, 8, 4, 3, 3, 4, 12, 12, 10, 6, 12, 15, 6};
    private static final double[] WALL_SCALES_X = {1, 1, 1, 1, 1, 1, 1, 1, 1, .5, 1, 1, 1, 1, 1};
    private static final String[] WALL_DIFFUSE_TEXTURES = {"tower.png", "glass_0.png", "glass_1.png",
        "highrise_0.png", "highrise_1.png", "highrise_2.png", "highrise_3.png", "highrise_4.png"};
    private static final String[] WALL_PBR_TEXTURES = {"highrise_facade_1", "highrise_facade_2", "highrise_facade_3",
        "highrise_facade_4", "highrise_facade_5", "highrise_facade_6", "highrise_facade_7"};

    private static final Material ROOF_MATERIAL = PBRMaterial.load("city/concrete_floor");
    private static final Material[] WALL_MATERIALS = new Material[NUM_WALL_TYPES];

    static {
        for (int i = 0; i < NUM_WALL_TYPES; i++) {
            if (i < WALL_DIFFUSE_TEXTURES.length) {
                WALL_MATERIALS[i] = DiffuseMaterial.load("highrise/" + WALL_DIFFUSE_TEXTURES[i]);
            } else {
                WALL_MATERIALS[i] = PBRMaterial.load("highrise/" + WALL_PBR_TEXTURES[i - WALL_DIFFUSE_TEXTURES.length]);
            }
        }
    }

    private final AABB bounds;
    private final int type;

    public Building(World world, AABB bounds) {
        super(world);
        this.bounds = bounds;
        this.type = world.random.nextInt(NUM_WALL_TYPES);

        world.manager.addStatic(box(bounds.size()), bounds.center());
    }

    @Override
    public void build(Map<Material, RawMeshBuilder> models) {
        var b1 = getBuilder(models, ROOF_MATERIAL);
        b1.addRectangleUV(bounds.lower.setZ(bounds.upper.z), bounds.size().setY(0).setZ(0), bounds.size().setX(0).setZ(0),
                new Vec2d(0, 0), new Vec2d(bounds.size().x / 4, 0), new Vec2d(0, bounds.size().y / 4));

        var b2 = getBuilder(models, WALL_MATERIALS[type]);
        createWalls(bounds, FLOOR_HEIGHT * WALL_SCALES[type], WALL_SCALES_X[type], b2);
    }
}
