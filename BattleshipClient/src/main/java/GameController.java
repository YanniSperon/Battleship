import Assets.MaterialManager;
import Assets.Mesh3D;
import Assets.MeshManager;
import GameScene.Components.*;
import GameScene.GameObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class GameController implements CustomController, Initializable {
    public Group root;


    private void refreshGUI() {
        synchronized (GUIClient.clientConnection.dataManager) {
        }
    }

    @Override
    public void postInit() {
        Scene s = root.getScene();

        s.addEventHandler(KeyEvent.ANY, keyEvent -> {
            for (Map.Entry<UUID, GameObject> pair : gameObjects.entrySet()) {
                if (pair.getValue().onKeyEvent(keyEvent)) {
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

        s.addEventHandler(ScrollEvent.ANY, scrollEvent -> {
            for (Map.Entry<UUID, GameObject> pair : gameObjects.entrySet()) {
                if (pair.getValue().onScrollEvent(scrollEvent)) {
                    break;
                }
            }
        });

        //((FPCameraComponent) gameObjects.get(cameraID).getComponentOfType(Component.ComponentType.FP_CAMERA)).focusCamera();
        ((OrbitalCameraComponent) gameObjects.get(cameraID).getComponentOfType(Component.ComponentType.ORBITAL_CAMERA)).focusCamera();
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

    private Point3D toOurCoordinates(Point3D blenderCoordinates) {
        //return new Point3D(blenderCoordinates.getY(), -blenderCoordinates.getZ(), -blenderCoordinates.getX());
        return new Point3D(blenderCoordinates.getX(), -blenderCoordinates.getZ(), blenderCoordinates.getY());
    }

    private void generateBoardPegs(GameObject holder) {
        // Enemy pegs
        {
            // Peg hole is 0.0322 in diameter
            // Coordinates from blender which is +z up, we are -y up

            // Blender is y right
            // We are x right

            // Blender is -x back
            // We are +z back
            Point3D blenderTopLeft = new Point3D(-0.630109, -1.0764, 1.67897);
            Point3D blenderBottomRight = new Point3D(-0.106787, 0.451513, 0.57453);

            Point3D ourTopLeft = toOurCoordinates(blenderTopLeft);
            Point3D ourBottomRight = toOurCoordinates(blenderBottomRight);

            Point3D delta = ourBottomRight.subtract(ourTopLeft);
            Point3D totalDeltaAcrossColumns = new Point3D(0.0, 0.0, delta.getZ());
            Point3D totalDeltaAcrossRows = new Point3D(delta.getX(), delta.getY(), 0.0);

            Point3D deltaPerRow = totalDeltaAcrossRows.multiply(0.125);
            Point3D deltaPerColumn = totalDeltaAcrossColumns.multiply(0.1);

            for (int y = 0; y < 9; ++y) {
                Point3D thisRow = ourTopLeft.add(deltaPerRow.multiply(y));
                for (int x = 0; x < 11; ++x) {
                    Box3DComponent b3dc = new Box3DComponent();
                    b3dc.box.setWidth(0.05);
                    b3dc.box.setHeight(0.05);
                    b3dc.box.setDepth(0.05);
                    b3dc.box.setMaterial(new PhongMaterial(Color.GREEN));
                    Point3D newLoc = thisRow.add(deltaPerColumn.multiply(x));
                    b3dc.box.setTranslateX(newLoc.getX());
                    b3dc.box.setTranslateY(newLoc.getY());
                    b3dc.box.setTranslateZ(newLoc.getZ());
                    holder.addComponent(b3dc);
                }
            }
        }

        // Ship pegs
        {
            // Peg hole is 0.0322 in diameter
            // Coordinates from blender which is +z up, we are -y up

            // Blender is y right
            // We are x right

            // Blender is -x back
            // We are +z back
            Point3D blenderTopLeft = new Point3D(0.306967, -1.07643, 0.170897);
            Point3D blenderBottomRight = new Point3D(0.459894, 0.451241, 0.170897);

            Point3D ourTopLeft = toOurCoordinates(blenderTopLeft);
            Point3D ourBottomRight = toOurCoordinates(blenderBottomRight);

            Point3D delta = ourBottomRight.subtract(ourTopLeft);
            Point3D totalDeltaAcrossColumns = new Point3D(0.0, 0.0, delta.getZ());
            Point3D totalDeltaAcrossRows = new Point3D(delta.getX(), 0.0, 0.0);

            Point3D deltaPerRow = totalDeltaAcrossRows;
            Point3D deltaPerColumn = totalDeltaAcrossColumns.multiply(0.1);

            for (int y = 0; y < 2; ++y) {
                Point3D thisRow = ourTopLeft.add(deltaPerRow.multiply(y));
                for (int x = 0; x < 11; ++x) {
                    Box3DComponent b3dc = new Box3DComponent();
                    b3dc.box.setWidth(0.05);
                    b3dc.box.setHeight(0.05);
                    b3dc.box.setDepth(0.05);
                    b3dc.box.setMaterial(new PhongMaterial(Color.ORANGE));
                    Point3D newLoc = thisRow.add(deltaPerColumn.multiply(x));
                    b3dc.box.setTranslateX(newLoc.getX());
                    b3dc.box.setTranslateY(newLoc.getY());
                    b3dc.box.setTranslateZ(newLoc.getZ());
                    holder.addComponent(b3dc);
                }
            }
        }

        // Friendly pegs
        {
            // Peg hole is 0.0322 in diameter
            // Coordinates from blender which is +z up, we are -y up

            // Blender is y right
            // We are x right

            // Blender is -x back
            // We are +z back
            Point3D blenderTopLeft = new Point3D(0.693604, -1.07643, 0.170897);
            Point3D blenderBottomRight = new Point3D(1.91595, 0.451241, 0.170897);

            Point3D ourTopLeft = toOurCoordinates(blenderTopLeft);
            Point3D ourBottomRight = toOurCoordinates(blenderBottomRight);

            Point3D delta = ourBottomRight.subtract(ourTopLeft);
            Point3D totalDeltaAcrossColumns = new Point3D(0.0, 0.0, delta.getZ());
            Point3D totalDeltaAcrossRows = new Point3D(delta.getX(), 0.0, 0.0);

            Point3D deltaPerRow = totalDeltaAcrossRows.multiply(0.125);
            Point3D deltaPerColumn = totalDeltaAcrossColumns.multiply(0.1);

            for (int y = 0; y < 9; ++y) {
                Point3D thisRow = ourTopLeft.add(deltaPerRow.multiply(y));
                for (int x = 0; x < 11; ++x) {
                    Box3DComponent b3dc = new Box3DComponent();
                    b3dc.box.setWidth(0.05);
                    b3dc.box.setHeight(0.05);
                    b3dc.box.setDepth(0.05);
                    b3dc.box.setMaterial(new PhongMaterial(Color.RED));
                    Point3D newLoc = thisRow.add(deltaPerColumn.multiply(x));
                    b3dc.box.setTranslateX(newLoc.getX());
                    b3dc.box.setTranslateY(newLoc.getY());
                    b3dc.box.setTranslateZ(newLoc.getZ());
                    holder.addComponent(b3dc);
                }
            }
        }
    }

    HashMap<UUID, GameObject> gameObjects = new HashMap<UUID, GameObject>();
    UUID cameraID = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("game", new GUIView(null, this));

        GameObject cameraComp = new GameObject();

        //g1.addComponent(new FPCameraComponent());
        OrbitalCameraComponent occ = new OrbitalCameraComponent();
        cameraComp.addComponent(occ);
        cameraComp.setTranslation(0.0, -5.0, -5.0);

        gameObjects.put(cameraComp.id, cameraComp);
        cameraID = cameraComp.id;

        GameObject lightHolder = new GameObject();
        {
            AmbientLightComponent alc = new AmbientLightComponent();
            alc.light.setColor(new Color(0.2, 0.2, 0.2, 1.0));
            lightHolder.addComponent(alc);
            PointLightComponent plc = new PointLightComponent();
            plc.light.setColor(new Color(1.0, 1.0, 1.0, 1.0));
            lightHolder.addComponent(plc);
            FollowComponent fc = new FollowComponent();
            fc.objectToFollow = occ.camera;
            lightHolder.addComponent(fc);
        }
        gameObjects.put(lightHolder.id, lightHolder);

        //ArrayList<String> frames = new ArrayList<String>();
        //frames.add("Explosion/PeakFrame.obj");
        //frames.add("Explosion/DissipateFrame.obj");
        //frames.add("Explosion/FinalFrame.obj");
        //Mesh3D m = MeshManager.loadAnimated(frames);
        //GameObject g2 = new GameObject();
        //g2.setTranslation(100.0, 0.0, 0.0);
        //g2.setZRotation(180);
        //g2.setScale(50.0, 50.0, 50.0);
        //{
        //    Mesh3DComponent m3dc = new Mesh3DComponent();
        //    m3dc.mesh3D = m;
        //    g2.addComponent(m3dc);
        //}
        //{
        //    AnimationControllerComponent acp = new AnimationControllerComponent();
        //    acp.setShouldLoop(true);
        //    acp.startPlaying();
        //    g2.addComponent(acp);
        //}
        //gameObjects.put(g2.id, g2);

        Mesh3D board = MeshManager.load("Board.obj");
        GameObject g3 = new GameObject();
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = board;
            g3.addComponent(m3dc);
            m3dc.meshView.setMaterial(MaterialManager.load("Board.mat"));
            generateBoardPegs(g3);
        }
        gameObjects.put(g3.id, g3);

        GameObject ground = new GameObject();
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = MeshManager.load("Ground.obj");
            ground.addComponent(m3dc);
            m3dc.meshView.setMaterial(MaterialManager.load("Grid.mat"));
            m3dc.meshView.setMouseTransparent(true);

            ground.setTranslation(0.0, 0.0, 0.0);
            ground.setScale(1000.0, 1.0, 1000.0);
        }
        gameObjects.put(ground.id, ground);

        //root.setEffect(new GaussianBlur());
        //root.setEffect(null);

        gameObjects.forEach((k, v) -> {
            root.getChildren().add(v.childrenHolder);
        });

        root.setFocusTraversable(true);
    }

    // Graphics
    @Override
    public void onRenderUpdate(double deltaTime) {
        gameObjects.forEach((k, v) -> {
            v.onRenderUpdate(deltaTime);
        });
    }
}
