package GameScene.Components;

import GameScene.GameObject;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class Component {
    public enum ComponentType {
        ANIMATION_CONTROLLER, CAMERA, MESH3D, NONE
    }

    // Any arbitrary GameObject component
    public GameObject gameObject = null;
    public ComponentType type = ComponentType.NONE;

    public Component() {
        onInit();
    }

    // Returns true if it consumes the input
    public boolean onKeyPressed(KeyEvent keyEvent) {
        return false;
    }

    // Returns true if it consumes the input
    public boolean onMouseEvent(MouseEvent mouseEvent) {
        return false;
    }

    // Called when a component is initialized
    public void onInit() {

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
