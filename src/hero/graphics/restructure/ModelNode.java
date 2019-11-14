package hero.graphics.restructure;

import beige_engine.util.math.Transformation;
import hero.graphics.renderables.Renderable;
import hero.graphics.renderables.RenderableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelNode {

    private final Transformation transform;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

    public ModelNode(Transformation transform, List<Mesh> meshes, List<ModelNode> children) {
        this.transform = transform;
        this.meshes = meshes;
        this.children = children;
    }

    public Renderable buildRenderable() {
        var renderable = new RenderableList(Stream.concat(
                meshes.stream().map(Mesh::buildRenderable),
                children.stream().map(ModelNode::buildRenderable)
        ).collect(Collectors.toList()));
        renderable.t = transform;
        return renderable;
    }
}
