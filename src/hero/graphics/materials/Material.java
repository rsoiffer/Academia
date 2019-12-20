package hero.graphics.materials;

import engine.util.Mutable;
import hero.graphics.Renderable;
import hero.graphics.drawables.DrawableSupplier;
import hero.graphics.loading.RawMeshBuilder;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Material<T extends DrawableSupplier> {

    public Renderable buildModularRenderable(Supplier<Stream<T>> meshes) {
        var rendMap = new Mutable<>(new HashMap<T, Renderable>());
        return (t, pass) -> {
            var newRendMap = new HashMap<T, Renderable>();
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

    public abstract Renderable buildRenderable(T t);

    public Renderable buildRenderable(RawMeshBuilder rawMeshBuilder) {
        return buildRenderable((T) rawMeshBuilder.toMesh());
    }
}
