package GameScene;

import Assets.Mesh3D;
import GameScene.Components.Component;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.*;

import java.util.ArrayList;
import java.util.UUID;

public class GameObject {
    public UUID id;
    public Group childrenHolder;
    private final Translate translate = new Translate(0, 0, 0);
    private final Scale scale = new Scale();
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final ArrayList<Component> components = new ArrayList<Component>();

    public GameObject() {
        childrenHolder = new Group();
        this.childrenHolder.getTransforms().addAll(translate, rotateX, rotateY, rotateZ, scale);
        id = UUID.randomUUID();
    }

    public void addComponent(Component c) {
        c.gameObject = this;
        components.add(c);
        c.onAdded();
    }

    public void removeComponent(Component c) {
        c.gameObject = null;
        components.remove(c);
        c.onRemoved();
    }

    public void onRenderUpdate(double deltaTime) {
        for (Component c : components) {
            c.onRenderUpdate(deltaTime);
        }
    }

    // Returns true if it consumes the input
    public boolean onKeyPressed(KeyEvent keyEvent) {
        for (Component c : components) {
            if (c.onKeyPressed(keyEvent)) {
                return true;
            }
        }
        return false;
    }

    // Returns true if it consumes the input
    public boolean onMouseEvent(MouseEvent mouseEvent) {
        for (Component c : components) {
            if (c.onMouseEvent(mouseEvent)) {
                return true;
            }
        }
        return false;
    }

    public void setTranslation(double x, double y, double z) {
        translate.setX(x);
        translate.setY(y);
        translate.setZ(z);
    }

    public double getTranslationX() {
        return translate.getX();
    }

    public double getTranslationY() {
        return translate.getY();
    }

    public double getTranslationZ() {
        return translate.getZ();
    }

    public void setScale(double x, double y, double z) {
        scale.setX(x);
        scale.setY(y);
        scale.setZ(z);
    }

    public double getScaleX() {
        return scale.getX();
    }

    public double getScaleY() {
        return scale.getY();
    }

    public double getScaleZ() {
        return scale.getZ();
    }

    // Rotation about x-axis (yaw)
    public void setXRotation(double yaw) {
        rotateX.setAngle(yaw);
    }

    public double getXRotation() {
        return rotateX.getAngle();
    }

    // Rotation about y-axis (pitch)
    public void setYRotation(double pitch) {
        rotateY.setAngle(pitch);
    }

    public double getYRotation() {
        return rotateY.getAngle();
    }

    // Rotation about z-axis (roll)
    public void setZRotation(double roll) {
        rotateZ.setAngle(roll);
    }

    public double getZRotation() {
        return rotateZ.getAngle();
    }

    // Returns the component if one exists attached to this game object
    public Component getComponentOfType(Component.ComponentType type) {
        for (Component c : components) {
            if (c.type == type) {
                return c;
            }
        }
        return null;
    }
}
