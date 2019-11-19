package hero.graphics;

public enum VertexAttrib {
    POSITIONS(3), TEX_COORDS(2), NORMALS(3), TANGENTS(3), BITANGENTS(3);

    public final int size;

    VertexAttrib(int size) {
        this.size = size;
    }
}
