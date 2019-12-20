package game.world;

import engine.util.math.Quaternion;
import engine.util.math.Transformation;
import engine.util.math.Vec3d;
import engine.rendering.ModelNode;
import engine.rendering.Renderable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Tree extends Structure {

    private static final int NUM_TYPES = 4;
    private static final List<Stem> STEMS = new ArrayList();

    static {
        System.out.print("Generating trees... ");
        for (int i = 0; i < NUM_TYPES; i++) {
            STEMS.add(Stem.generateTree());
        }
        System.out.println("Done!");
    }

    private final Vec3d pos;
    private final Stem stem;

    public Tree(World world, Vec3d pos) {
        super(world);
        this.pos = pos;
        stem = STEMS.get(world.random.nextInt(NUM_TYPES));
    }

    @Override
    public Stream<Renderable> getRenderables() {
        var node = new ModelNode();
        node.transform = Transformation.create(pos, Quaternion.IDENTITY, 1);
        node.addChild(stem.getRenderable());
        node.addChild(stem.getRenderableLeaves());
        return Stream.of(node);
    }
}
