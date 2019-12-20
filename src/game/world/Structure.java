package game.world;

import engine.rendering.Renderable;
import engine.rendering.loading.RawMeshBuilder;
import engine.rendering.materials.Material;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Structure {

    public final World world;
    private final List<Structure> substructures = new ArrayList();

    public Structure(World world) {
        this.world = world;
    }

    protected void addSubstructure(Structure s) {
        substructures.add(s);
    }

    public void build(Map<Material, RawMeshBuilder> models) {
    }

    protected RawMeshBuilder getBuilder(Map<Material, RawMeshBuilder> models, Material material) {
        models.putIfAbsent(material, new RawMeshBuilder());
        return models.get(material);
    }

    public Stream<Renderable> getRenderables() {
        return Stream.empty();
    }

    public Stream<Structure> getSubstructures() {
        return substructures.stream();
    }
}
