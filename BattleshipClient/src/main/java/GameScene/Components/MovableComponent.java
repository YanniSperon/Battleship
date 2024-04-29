package GameScene.Components;

import Assets.Mesh3D;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

// Used as just selectable component
public class MovableComponent extends Component {
    public boolean isSelected = false;

    public Consumer<UUID> onPressedCallback = null;
    public boolean canSelect = true;

    public MovableComponent() {
        this.type = ComponentType.MOVABLE;
    }

    @Override
    public void onRenderUpdate(double deltaTime) {
        Bounds b = getBounds();
        if (isPosXPressed) {
            gameObject.setTranslation(gameObject.getTranslationX() + deltaTime * b.getWidth() * 100.0, gameObject.getTranslationY(), gameObject.getTranslationZ());
        }
        if (isNegXPressed) {
            gameObject.setTranslation(gameObject.getTranslationX() - deltaTime * b.getWidth() * 100.0, gameObject.getTranslationY(), gameObject.getTranslationZ());
        }
        if (isPosYPressed) {
            gameObject.setTranslation(gameObject.getTranslationX(), gameObject.getTranslationY() + deltaTime * b.getHeight() * 100.0, gameObject.getTranslationZ());
        }
        if (isNegYPressed) {
            gameObject.setTranslation(gameObject.getTranslationX(), gameObject.getTranslationY() - deltaTime * b.getHeight() * 100.0, gameObject.getTranslationZ());
        }
        if (isPosZPressed) {
            gameObject.setTranslation(gameObject.getTranslationX(), gameObject.getTranslationY(), gameObject.getTranslationZ() + deltaTime * b.getDepth() * 100.0);
        }
        if (isNegZPressed) {
            gameObject.setTranslation(gameObject.getTranslationX(), gameObject.getTranslationY(), gameObject.getTranslationZ() - deltaTime * b.getDepth() * 100.0);
        }
    }

    boolean isPosXPressed = false;
    boolean isNegXPressed = false;
    boolean isPosYPressed = false;
    boolean isNegYPressed = false;
    boolean isPosZPressed = false;
    boolean isNegZPressed = false;
    @Override
    public boolean onKeyEvent(KeyEvent keyEvent) {
        if (isSelected) {
            if (keyEvent.isShiftDown()) {
                if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                    switch (keyEvent.getCode()) {
                        case W:
                            isPosXPressed = true;
                            break;
                        case S:
                            isNegXPressed = true;
                            break;
                        case A:
                            isPosYPressed = true;
                            break;
                        case D:
                            isNegYPressed = true;
                            break;
                        case Q:
                            isNegZPressed = true;
                            break;
                        case E:
                            isPosZPressed = true;
                            break;
                    }
                } else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
                    switch (keyEvent.getCode()) {
                        case W:
                            isPosXPressed = false;
                            break;
                        case S:
                            isNegXPressed = false;
                            break;
                        case A:
                            isPosYPressed = false;
                            break;
                        case D:
                            isNegYPressed = false;
                            break;
                        case Q:
                            isNegZPressed = false;
                            break;
                        case E:
                            isPosZPressed = false;
                            break;
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean onMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            if (mouseEvent.isPrimaryButtonDown()) {
                PickResult res = mouseEvent.getPickResult();
                if (isPickableComponent(res.getIntersectedNode())) {
                    if (onPressedCallback != null) {
                        onPressedCallback.accept(this.gameObject.id);
                    }
                    if (!isSelected && canSelect) {
                        onSelected();
                    }
                } else {
                    if (canSelect) {
                        onDeselected();
                    }
                }

            }
        }
        return false;
    }

    private boolean isPickableComponent(Node value) {
        if (value == null) {
            return false;
        }
        ArrayList<Node> nodes = getPickableComponents();
        for (Node n : nodes) {
            if (value.equals(n)) {
                return true;
            }
        }
        return false;
    }

    public void onSelected() {
        isSelected = true;
    }

    public void onDeselected() {
        isSelected = false;
    }

    protected ArrayList<Node> getPickableComponents() {
        return new ArrayList<Node>();
    }

    public Bounds getBounds() {
        return new BoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    }
}
