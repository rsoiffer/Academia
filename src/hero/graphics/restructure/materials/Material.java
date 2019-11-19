package hero.graphics.restructure.materials;

import beige_engine.util.Mutable;
import hero.graphics.restructure.Mesh;
import hero.graphics.restructure.Strategy;
import hero.graphics.restructure.loading.RawMeshBuilder;

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Material {

    public Strategy buildModularStrategy(Supplier<Stream<Mesh>> meshes) {
        var stratMap = new Mutable<>(new HashMap<Mesh, Strategy>());
        return (t, pass) -> {
            var newStratMap = new HashMap<Mesh, Strategy>();
            meshes.get().forEach(m -> {
                if (stratMap.o.containsKey(m)) {
                    newStratMap.put(m, stratMap.o.get(m));
                } else {
                    newStratMap.put(m, buildStrategy(m));
                }
            });
            stratMap.o = newStratMap;
            for (var s : stratMap.o.values()) {
                s.render(t, pass);
            }
        };
    }

    public abstract Strategy buildStrategy(Mesh mesh);

    public Strategy buildStrategy(RawMeshBuilder rawMeshBuilder) {
        return buildStrategy(rawMeshBuilder.toRawMesh());
    }
}
