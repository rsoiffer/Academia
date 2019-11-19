package hero.game.trees;

import beige_engine.util.math.Quaternion;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec3d;
import hero.graphics.restructure.ModelNode;
import hero.physics.shapes.CollisionShape;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static beige_engine.util.math.MathUtils.floor;

public class StemGenerator {

    private final List<Stem> treeInstances = new ArrayList();
    private final List<List<Vec3d>> treePlacements = new ArrayList();

    public List<CollisionShape> collisionShapes() {
        List<CollisionShape> r = new LinkedList();
        for (int i = 0; i < treeInstances.size(); i++) {
            Stem s = treeInstances.get(i);
            for (Vec3d v : treePlacements.get(i)) {
                s.getCollisionShapes(v).forEach(r::add);
            }
        }
        return r;
    }

    public void generateInstances(int num) {
        num = 8;
        for (int i = 0; i < num; i++) {
            Stem s = Stem.generateTree();
            treeInstances.add(s);
            treePlacements.add(new LinkedList());
        }
        System.out.println("Done generating");
    }

    public void placeTree(Vec3d pos) {
        int chosen = floor(Math.random() * treeInstances.size());
        treePlacements.get(chosen).add(pos);
    }

    public List<ModelNode> modelNodes() {
        List<ModelNode> r = new LinkedList<>();
        for (int i = 0; i < treeInstances.size(); i++) {
            Stem s = treeInstances.get(i);
            for (var v : treePlacements.get(i)) {
                var mn = new ModelNode(s.getStrategy());
                mn.transform = Transformation.create(v, Quaternion.IDENTITY, 1);
                r.add(mn);
            }
        }
        for (int i = 0; i < treeInstances.size(); i++) {
            Stem s = treeInstances.get(i);
            for (var v : treePlacements.get(i)) {
                var mn = new ModelNode(s.getStrategyLeaves());
                mn.transform = Transformation.create(v, Quaternion.IDENTITY, 1);
                r.add(mn);
            }
        }
        return r;
    }
}
