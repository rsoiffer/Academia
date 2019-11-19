package hero.graphics.materials;

import beige_engine.util.Mutable;
import hero.graphics.Mesh;
import hero.graphics.Renderable;
import hero.graphics.loading.RawMeshBuilder;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Material {

    public Renderable buildModularRenderable(Supplier<Stream<Mesh>> meshes) {
        var rendMap = new Mutable<>(new HashMap<Mesh, Renderable>());
        return (t, pass) -> {
            var newRendMap = new HashMap<Mesh, Renderable>();
            meshes.get().forEach(m -> {
                if (rendMap.o.containsKey(m)) {
                    newRendMap.put(m, rendMap.o.get(m));
                } else {
                    newRendMap.put(m, buildRenderable(m));
                }
            });
            rendMap.o = newRendMap;
            for (var s : rendMap.o.values()) {
                s.render(t, pass);
            }
        };
    }

    public abstract Renderable buildRenderable(Mesh mesh);

    public Renderable buildRenderable(RawMeshBuilder rawMeshBuilder) {
        return buildRenderable(rawMeshBuilder.toMesh());
    }
}
