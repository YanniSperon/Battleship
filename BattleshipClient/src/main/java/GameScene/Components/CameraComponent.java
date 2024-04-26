package GameScene.Components;

import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Transform;

import static com.sun.javafx.util.Utils.clamp;

public class CameraComponent extends Component {
    public PerspectiveCamera camera;

    @Override
    public void onInit() {
        this.type = ComponentType.CAMERA;
    }

    @Override
    public void onAdded() {
        this.type = ComponentType.CAMERA;
        camera = new PerspectiveCamera(true);
        camera.setNearClip(1.0);
        camera.setFarClip(100000.0);
        camera.setFieldOfView(90);
        camera.setVerticalFieldOfView(false);
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
    public double movementSpeed = 1000.0;
    @Override
    public boolean onKeyPressed(KeyEvent keyEvent) {
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
                System.out.println("Current rotation: (" + gameObject.getXRotation() + ", " + gameObject.getYRotation() + ", " + gameObject.getZRotation() + ")");
                gameObject.setXRotation(clamp(-85, ((gameObject.getXRotation() - (mouseY - oldMouseY) * (mouseSensitivityY)) % 360 + 540) % 360 - 180, 85));
                gameObject.setYRotation(clamp(-360, ((gameObject.getYRotation() + (mouseX - oldMouseX) * (mouseSensitivityX)) % 360 + 540) % 360 - 180, 360));
            } else if (mouseEvent.isSecondaryButtonDown()) {
                // Right click
            } else if (mouseEvent.isMiddleButtonDown()) {
                // Middle mouse
            }
        }
        return false;
    }

    private void moveForward(double deltaTime) {
        Point3D n = getViewDirection();
        System.out.println("Forward direction is " + n);
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * n.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * n.getY()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * n.getZ())
        );
    }

    private void strafeLeft(double deltaTime) {
        // -y is the up direction
        Point3D n = getViewDirection();
        System.out.println("Forward direction is " + n);
        Point3D upDir = getUpDirection();
        System.out.println("Up direction is " + upDir);
        Point3D rightDir = n.crossProduct(upDir);
        System.out.println("Left direction is therefore -" + rightDir);
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * -rightDir.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * -rightDir.getX()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * -rightDir.getX())
        );
    }

    private void strafeRight(double deltaTime) {
        // -y is the up direction
        Point3D n = getViewDirection();
        System.out.println("Forward direction is " + n);
        Point3D upDir = getUpDirection();
        System.out.println("Up direction is " + upDir);
        Point3D rightDir = n.crossProduct(upDir);
        System.out.println("Right direction is therefore " + rightDir);
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * rightDir.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * rightDir.getX()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * rightDir.getX())
        );
    }

    private void moveBack(double deltaTime) {
        Point3D n = getViewDirection();
        System.out.println("Backwards direction is -" + n);
        gameObject.setTranslation(gameObject.getTranslationX() + (movementSpeed * deltaTime * -n.getX()),
                gameObject.getTranslationY() + (movementSpeed * deltaTime * -n.getY()),
                gameObject.getTranslationZ() + (movementSpeed * deltaTime * -n.getZ())
        );
    }

    // Orientation of affine taken from JavaFX website
    //   | R | Up| F |  | P|
    // U |mxx|mxy|mxz|  |tx|
    // V |myx|myy|myz|  |ty|
    // N |mzx|mzy|mzz|  |tz|
    private Point3D getViewDirection() {
        Transform t = camera.getLocalToSceneTransform();
        return new Point3D(t.getMxz(), t.getMyz(), t.getMzz());
    }
    private Point3D getUpDirection() {
        return new Point3D(0.0, -1.0, 0.0);
    }
    private Point3D getVDirection() {
        Transform t = camera.getLocalToSceneTransform();
        return new Point3D(t.getMxy(), t.getMyy(), t.getMzy());
    }
    private Point3D getRDirection() {
        Transform t = camera.getLocalToSceneTransform();
        return new Point3D(t.getMxx(), t.getMxy(), t.getMxz());
    }
    private Point3D getUDirection() {
        Transform t = camera.getLocalToSceneTransform();
        return new Point3D(t.getMxx(), t.getMyx(), t.getMzx());
    }

}