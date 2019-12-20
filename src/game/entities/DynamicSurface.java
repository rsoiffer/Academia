package game.entities;

import engine.samples.Behavior;
import engine.rendering.Mesh;
import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import static engine.rendering.VertexAttrib.POSITIONS;
import engine.rendering.materials.Material;
import engine.rendering.utils.SurfaceNet;
import engine.physics.PhysicsManager;
import java.util.HashMap;
import java.util.Map;
import org.ode4j.ode.DGeom;
import static org.ode4j.ode.OdeHelper.createTriMesh;
import static org.ode4j.ode.OdeHelper.createTriMeshData;

public class DynamicSurface extends Behavior {

    public final SurfaceNet surfaceNet;
    public final PhysicsManager manager;

    private Map<Mesh, DGeom> meshes = new HashMap();

    public DynamicSurface(SurfaceNet surfaceNet, Material material, PhysicsManager manager) {
        this.surfaceNet = surfaceNet;
        this.manager = manager;

        var mnb = add(new ModelComponent(this));
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
