package Assets;

import javafx.scene.shape.TriangleMesh;

public class Mesh3D {
    public TriangleMesh mesh;

    public enum Type {
        ANIMATED, STATIC;
    }

    public Type type = Type.STATIC;

    public Mesh3D(TriangleMesh mesh) {
        this.mesh = mesh;
    }
}
