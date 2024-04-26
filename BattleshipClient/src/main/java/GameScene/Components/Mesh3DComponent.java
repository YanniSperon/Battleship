package GameScene.Components;

import Assets.Mesh3D;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;

public class Mesh3DComponent extends Component {
    public Mesh3D mesh3D = null;
    public MeshView meshView = null;
    public boolean isSelected = false;

    @Override
    public void onInit() {
        this.type = ComponentType.MESH3D;
    }

    @Override
    public void onAdded() {
        this.meshView = new MeshView(mesh3D.mesh);
        meshView.setCullFace(CullFace.BACK);
        gameObject.childrenHolder.getChildren().add(this.meshView);
    }

    @Override
    public void onRenderUpdate(double deltaTime) {
        Bounds b = getBounds();
        if (isPosXPressed) {
            System.out.println("Moving pos x with width: " + b.getWidth());
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
    public boolean onKeyPressed(KeyEvent keyEvent) {
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
            PickResult res = mouseEvent.getPickResult();
            System.out.println("res " + res);
            if (res.getIntersectedNode() != null && res.getIntersectedNode().equals(meshView) && !isSelected) {
                meshView.setMaterial(new PhongMaterial(Color.RED));
                isSelected = true;
            } else {
                meshView.setMaterial(new PhongMaterial(Color.LIGHTGRAY));
                isSelected = false;
            }
        }
        return false;
    }

    public Bounds getBounds() {
        return meshView.getBoundsInLocal();
    }
}
