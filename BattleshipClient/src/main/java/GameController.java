import Assets.AnimatedMesh3D;
import Assets.Mesh3D;
import Assets.MeshLoader;
import GameScene.Components.AnimationControllerComponent;
import GameScene.Components.CameraComponent;
import GameScene.Components.Component;
import GameScene.Components.Mesh3DComponent;
import GameScene.GameObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class GameController implements CustomController, Initializable {
    public Group root;

    private long currentTime = 0;


    private void refreshGUI() {
        synchronized (GUIClient.clientConnection.dataManager) {
        }
    }

    @Override
    public void postInit() {
        Scene s = root.getScene();

        s.addEventHandler(KeyEvent.ANY, keyEvent -> {
            for (Map.Entry<UUID, GameObject> pair : gameObjects.entrySet()) {
                if (pair.getValue().onKeyPressed(keyEvent)) {
                    break;
                }
            }
        });

        s.addEventHandler(MouseEvent.ANY, mouseEvent -> {
            for (Map.Entry<UUID, GameObject> pair : gameObjects.entrySet()) {
                if (pair.getValue().onMouseEvent(mouseEvent)) {
                    break;
                }
            }
        });
        ((CameraComponent) gameObjects.get(cameraID).getComponentOfType(Component.ComponentType.CAMERA)).focusCamera();
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

    HashMap<UUID, GameObject> gameObjects = new HashMap<UUID, GameObject>();
    UUID cameraID = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("game", new GUIView(null, this));
        currentTime = System.nanoTime();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16.66666666), e -> renderUpdate()));
        timeline.setCycleCount(Animation.INDEFINITE);

        GameObject g1 = new GameObject();
        g1.addComponent(new CameraComponent());
        g1.setTranslation(0.0, 0.0, -50.0);
        g1.setZRotation(0);
        gameObjects.put(g1.id, g1);
        cameraID = g1.id;

        ArrayList<String> frames = new ArrayList<String>();
        frames.add("Explosion/PeakFrame.obj");
        frames.add("Explosion/DissipateFrame.obj");
        frames.add("Explosion/FinalFrame.obj");
        Mesh3D m = MeshLoader.loadAnimated(frames);
        GameObject g2 = new GameObject();
        g2.setTranslation(100.0, 0.0, 0.0);
        g2.setZRotation(180);
        g2.setScale(50.0, 50.0, 50.0);
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = m;
            g2.addComponent(m3dc);
        }
        {
            AnimationControllerComponent acp = new AnimationControllerComponent();
            acp.setShouldLoop(true);
            acp.startPlaying();
            g2.addComponent(acp);
        }
        gameObjects.put(g2.id, g2);

        Mesh3D board = MeshLoader.load("Board.obj", true);
        GameObject g3 = new GameObject();
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = board;
            g3.addComponent(m3dc);
        }
        gameObjects.put(g3.id, g3);


        gameObjects.forEach((k, v) -> {
            root.getChildren().add(v.childrenHolder);
        });

        root.setFocusTraversable(true);

        timeline.playFromStart();
    }

    double animationProgress = 0.0;

    // Graphics
    public void renderUpdate() {
        // Compute deltaTime
        long newTime = System.nanoTime();
        long timeDiff = newTime - currentTime;
        double deltaTime = timeDiff * 0.000000001;
        currentTime = newTime;

        gameObjects.forEach((k, v) -> {
            v.onRenderUpdate(deltaTime);
        });
    }
}
