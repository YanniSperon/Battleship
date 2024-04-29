package GameScene.Components;

import GameScene.GameObject;
import javafx.geometry.Point3D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class Component {
    public static final Point3D FORWARD = new Point3D(0.0, 0.0, 1.0);
    public static final Point3D UP = new Point3D(0.0, -1.0, 0.0);
    public static final Point3D RIGHT = new Point3D(1.0, 0.0, 0.0);
    public enum ComponentType {
        ANIMATION_CONTROLLER, FP_CAMERA, MESH3D, MOVABLE, BOX3D, POINT_LIGHT, AMBIENT_LIGHT, ORBITAL_CAMERA, FOLLOW, SPAWNER, NONE
    }

    // Any arbitrary GameObject component
    public GameObject gameObject = null;
    public ComponentType type = ComponentType.NONE;

    public Component() {
    }

    // Returns true if it consumes the input
    public boolean onKeyEvent(KeyEvent keyEvent) {
        return false;
    }

    // Returns true if it consumes the input
    public boolean onMouseEvent(MouseEvent mouseEvent) {
        return false;
    }

    // Returns true if it consumes the input
    public boolean onScrollEvent(ScrollEvent scrollEvent) {
        return false;
    }

    // Called when the component is removed from a game object
    public void onRemoved() {

    }

    // Called when the component is added to a game object
    public void onAdded() {

    }

    // Called every tick
    public void onRenderUpdate(double deltaTime) {

    }
}
