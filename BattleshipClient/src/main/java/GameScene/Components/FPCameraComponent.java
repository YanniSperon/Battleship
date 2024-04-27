package GameScene.Components;

import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Transform;

import static com.sun.javafx.util.Utils.clamp;

public class FPCameraComponent extends Component {
    public PerspectiveCamera camera;

    public FPCameraComponent() {
        this.type = ComponentType.FP_CAMERA;
    }

    @Override
    public void onAdded() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.05);
        camera.setFarClip(1000.0);
        camera.setFieldOfView(90);
        camera.setVerticalFieldOfView(true);
        gameObject.childrenHolder.getChildren().add(camera);
    }

    public void focusCamera() {
        gameObject.childrenHolder.getScene().setCamera(camera);
    }

    boolean isForwardPressed = false;
    boolean isLeftPressed = false;
    boolean isRightPressed = false;
    boolean isBackwardsPressed = false;
    @Override
    public void onRenderUpdate(double deltaTime) {
        if (isForwardPressed) {
            moveForward(deltaTime);
        }
        if (isLeftPressed) {
            strafeLeft(deltaTime);
        }
        if (isRightPressed) {
            strafeRight(deltaTime);
        }
        if (isBackwardsPressed) {
            moveBack(deltaTime);
        }
    }

    public double mouseSensitivityX = 1.0;
    public double mouseSensitivityY = 1.0;
    public double movementSpeed = 1.0;
    @Override
    public boolean onKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
            switch (keyEvent.getCode()) {
                case W:
                    isForwardPressed = true;
                    break;
                case S:
                    isBackwardsPressed = true;
                    break;
                case A:
                    isLeftPressed = true;
                    break;
                case D:
                    isRightPressed = true;
                    break;
            }
        } else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
            switch (keyEvent.getCode()) {
                case W:
                    isForwardPressed = false;
                    break;
                case S:
                    isBackwardsPressed = false;
                    break;
                case A:
                    isLeftPressed = false;
                    break;
                case D:
                    isRightPressed = false;
                    break;
            }
        }
        return !keyEvent.isShiftDown();
    }

    private double mouseX;
    private double mouseY;

    @Override
    public boolean onMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            mouseX = mouseEvent.getSceneX();
            mouseY = mouseEvent.getSceneY();
        } else if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            double oldMouseX = mouseX;
            double oldMouseY = mouseY;

            mouseX = mouseEvent.getSceneX();
            mouseY = mouseEvent.getSceneY();

            if (mouseEvent.isPrimaryButtonDown()) {
                gameObject.setXRotation(clamp(-85, (gameObject.getXRotation() - (mouseY - oldMouseY) * (mouseSensitivityY)), 85));
                gameObject.setYRotation(((gameObject.getYRotation() + (mouseX - oldMouseX) * (mouseSensitivityX))));
            }
        }
        return false;
    }

    private void moveForward(double deltaTime) {
        Point3D n = getLookDirectionWS();
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * n.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * n.getY()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * n.getZ())
        );
    }

    private void strafeLeft(double deltaTime) {
        // -y is the up direction
        Point3D rightDir = getRightDirectionWS();
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * -rightDir.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * -rightDir.getX()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * -rightDir.getX())
        );
    }

    private void strafeRight(double deltaTime) {
        // -y is the up direction
        Point3D rightDir = getRightDirectionWS();
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * rightDir.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * rightDir.getX()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * rightDir.getX())
        );
    }

    private void moveBack(double deltaTime) {
        Point3D n = getLookDirectionWS();
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * -n.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * -n.getY()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * -n.getZ())
        );
    }

    public Point3D getLookDirectionWS() {
        return gameObject.getRotationTransform().transform(Component.FORWARD);
    }

    public Point3D getUpDirectionWS() {
        return gameObject.getRotationTransform().transform(Component.UP);
    }

    public Point3D getRightDirectionWS() {
        return gameObject.getRotationTransform().transform(Component.RIGHT);
    }

}