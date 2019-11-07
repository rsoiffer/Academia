package beige_engine.graphics.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import beige_engine.util.math.Transformation;
import beige_engine.util.math.Vec2d;

public abstract class GUIItem {

    public Vec2d offset = new Vec2d(0, 0);
    public Vec2d size = new Vec2d(0, 0);

    private GUIItem parent;
    private final List<GUIItem> children = new LinkedList();

    public final void add(GUIItem... arr) {
        for (GUIItem i : arr) {
            children.add(i);
            i.parent = this;
        }
    }

    public Stream<GUIItem> allChildren() {
        return Stream.concat(children.stream().flatMap(GUIItem::allChildren), Stream.of(this));
    }

    public Vec2d center() {
        if (parent == null) {
            return offset;
        }
        return parent.center().add(offset);
    }

    public void destroy() {
        parent.children.remove(this);
        parent = null;
    }

    public Vec2d getLowerLeft() {
        return center().sub(size.div(2));
    }

    public Vec2d getUpperRight() {
        return center().add(size.div(2));
    }

    public void onClick() {
    }

    public void onHoverStart() {
    }

    public void onHoverStop() {
    }

    protected abstract void render();

    public void renderOuter() {
        render();
        children.forEach(GUIItem::renderOuter);
    }

    protected Transformation transformationCenter() {
        return Transformation.create(center(), 0, size);
    }

    protected Transformation transformationLL() {
        return Transformation.create(getLowerLeft(), 0, size);
    }
}
