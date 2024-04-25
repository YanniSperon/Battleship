import Data.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

public class GameController implements CustomController, Initializable {
    private class UpdateThread extends Thread {
        GameController controller;

        UpdateThread(GameController controller) {
            this.controller = controller;
        }

        public void run() {
            while (true) {
                controller.update();
            }
        }
    }

    private UpdateThread updateThread;

    public PerspectiveCamera firstPersonCamera;
    public Group group3D;

    private enum KeyState {
        PRESSED, HELD, RELEASED, INACTIVE
    }

    private HashMap<KeyCode, KeyState> keyMap = new HashMap<KeyCode, KeyState>();
    private long currentTime = 0;
    private double mouseX = 0.0, mouseY = 0.0;


    private void refreshGUI() {
        synchronized (GUIClient.clientConnection.dataManager) {
        }
    }

    @Override
    public void postInit() {
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case REFRESH:
                refreshGUI();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResizeWidth(Number oldVal, Number newVal) {

    }

    @Override
    public void onResizeHeight(Number oldVal, Number newVal) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("game", new GUIView(null, this));
        currentTime = System.nanoTime();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16.66666666), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();

        updateThread = new UpdateThread(this);
        updateThread.start();

        firstPersonCamera.setFieldOfView(70.0);
        firstPersonCamera.setNearClip(1.0);
        firstPersonCamera.setFarClip(1000.0);

        MeshView peg = new MeshView(MeshLoader.load("Peg.obj"));
        peg.setScaleX(50);
        peg.setScaleY(50);
        peg.setScaleZ(50);
        peg.setCullFace(CullFace.BACK);
        group3D.getChildren().add(peg);

        group3D.setFocusTraversable(true);
        group3D.setOnMouseClicked((event)->{
            PickResult res = event.getPickResult();
            System.out.println("res "+ res);
            if (res.getIntersectedNode() instanceof Box){
                ((Box)res.getIntersectedNode()).setMaterial(
                        new PhongMaterial(event.isShiftDown() ? Color.BLACK : Color.RED));
            }
        });
    }

    // Graphics
    public void tick() {
        // Compute deltaTime
        long newTime = System.nanoTime();
        long timeDiff = newTime - currentTime;
        double deltaTime = timeDiff * 0.000000001;
        currentTime = newTime;
        // Meant for updating rendering calculations if necessary
    }

    // Run on a different thread, cannot access graphics
    public void update() {
        // Meant for physics calculations if necessary
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getSceneX();
        mouseY = mouseEvent.getSceneY();
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        keyMap.put(keyEvent.getCode(), KeyState.PRESSED);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        keyMap.put(keyEvent.getCode(), KeyState.RELEASED);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {

    }
}
