package hero.game;

import engine.samples.Behavior;
import hero.graphics.Mesh;
import hero.graphics.ModelNode;
import static hero.graphics.VertexAttrib.POSITIONS;
import hero.graphics.materials.Material;
import hero.graphics.utils.SurfaceNet;
import hero.physics.PhysicsManager;
import java.util.HashMap;
import java.util.Map;
import org.ode4j.ode.DGeom;
import static org.ode4j.ode.OdeHelper.createTriMesh;
import static org.ode4j.ode.OdeHelper.createTriMeshData;

public class SurfaceNetEntity extends Behavior {

    public final SurfaceNet surfaceNet;
    public final PhysicsManager manager;

    private Map<Mesh, DGeom> meshes = new HashMap();

    public SurfaceNetEntity(SurfaceNet surfaceNet, Material material, PhysicsManager manager) {
        this.surfaceNet = surfaceNet;
        this.manager = manager;

        var mnb = add(new ModelBehavior(this));
        mnb.node = new ModelNode(material.buildModularRenderable(surfaceNet::getMeshes));
    }

    @Override
    public void onStep() {
        var newMeshes = new HashMap<Mesh, DGeom>();
        surfaceNet.getMeshes().forEach(m -> {
            if (meshes.containsKey(m)) {
                newMeshes.put(m, meshes.get(m));
            } else {
                // add a new mesh to the world (and the map)
                var t = createTriMeshData();
                t.build(m.data.get(POSITIONS), m.indices);
                t.preprocess();
                var t2 = createTriMesh(manager.staticSpace, t, null, null, null);
                System.out.println("Created mesh!");
                newMeshes.put(m, t2);
            }
        });
        for (var m : meshes.keySet()) {
            if (!newMeshes.containsKey(m)) {
                // remove a mesh from the world
                meshes.get(m).destroy();
                System.out.println("Destroyed mesh!");
                System.out.println(newMeshes.size());
            }
        }
        meshes = newMeshes;
    }
}
