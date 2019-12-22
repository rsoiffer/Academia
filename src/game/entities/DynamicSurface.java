package game.entities;

import engine.physics.PhysicsManager;
import static engine.physics.StaticShape.triMesh;
import engine.rendering.Mesh;
import engine.rendering.ModelComponent;
import engine.rendering.ModelNode;
import engine.rendering.materials.Material;
import engine.rendering.surfaces.SDFSurface;
import engine.samples.Behavior;
import engine.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import org.ode4j.ode.DGeom;

public class DynamicSurface extends Behavior {

    public final SDFSurface surfaceNet;
    public final PhysicsManager manager;

    private Map<Mesh, DGeom> meshes = new HashMap();

    public DynamicSurface(SDFSurface surfaceNet, Material material, PhysicsManager manager) {
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
                var t = manager.addStatic(triMesh(m), new Vec3d(0, 0, 0));
                newMeshes.put(m, t);
            }
        });
        for (var m : meshes.keySet()) {
            if (!newMeshes.containsKey(m)) {
                // remove a mesh from the world
                meshes.get(m).destroy();
            }
        }
        meshes = newMeshes;
    }
}
