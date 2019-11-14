package hero.graphics.restructure;

import hero.graphics.renderables.ColorModel;
import hero.graphics.renderables.DiffuseModel;
import hero.graphics.renderables.Renderable;

public class Mesh {

    public final RawMesh rawMesh;
    public final Material material;

    public Mesh(RawMesh rawMesh, Material material) {
        this.rawMesh = rawMesh;
        this.material = material;
    }

    public Renderable buildRenderable() {
        if (material.opacity != 1) return null;

        if (material.texture == null) {
            String[] attribNames = {"positions", "normals"};
            var model = rawMesh.buildModel(attribNames);
            var renderable = new ColorModel(model);
            renderable.color = material.getColor();
            renderable.metallic = material.getMetallic();
            renderable.roughness = material.getRoughness();
            return renderable;
        } else {
            String[] attribNames = {"positions", "texCoords", "normals"};
            var model = rawMesh.buildModel(attribNames);
            var renderable = new DiffuseModel(model, material.texture);
            renderable.metallic = material.getMetallic();
            renderable.roughness = material.getRoughness();
            return renderable;
        }
    }
}
