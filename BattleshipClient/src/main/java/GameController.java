import Assets.MaterialManager;
import Assets.Mesh3D;
import Assets.MeshManager;
import Data.*;
import GameScene.Components.*;
import GameScene.GameObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class GameController implements CustomController, Initializable {
    public Group root;

    private final ArrayList<UUID> enemyPlacingIndicators = new ArrayList<UUID>();
    private final ArrayList<UUID> friendlyPlacingIndicators = new ArrayList<UUID>();

    private final HashMap<UUID, GameObject> gameObjects = new HashMap<UUID, GameObject>();
    private final ArrayList<UUID> ships = new ArrayList<UUID>();

    private MeshView waitingMessage;
    private Game.Player ourPlayer = null;

    private UUID selectedSquare = null;
    private VisualPiece currentHeldPiece = null;
    private boolean isCurrentHeldPieceOverGridSpace = false;

    private UUID cameraID = null;

    public MediaPlayer mediaPlayer = null;
    private AudioClip actionSFX;
    private AudioClip fireSFX;
    private AudioClip hitSFX;

    public Game oldGame = null;

    @Override
    public void postInit() {
        Scene s = root.getScene();
        s.setFill(new Color(0.0, 0.0, 0.0, 1.0));

        s.addEventHandler(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED && keyEvent.getCode() == KeyCode.ESCAPE) {
                onEscapePressed();
            }
            for (Map.Entry<UUID, GameObject> pair : gameObjects.entrySet()) {
                if (pair.getValue().onKeyEvent(keyEvent)) {
                    break;
                }
            }
        });

        s.addEventHandler(MouseEvent.ANY, mouseEvent -> {
            PickResult res = mouseEvent.getPickResult();
            if (!GUIClient.isPlaying && mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED) {
                if (res != null) {
                    if (res.getIntersectedNode() != null) {
                        if (res.getIntersectedNode().getUserData() != null) {
                            GameObject userData = (GameObject) res.getIntersectedNode().getUserData();
                            if (userData != null && friendlyPlacingIndicators.contains(userData.id)) {
                                moveCurrentPiece(userData.getTranslation(), userData.id);
                            }
                        }
                    }
                }
            }
            if (!GUIClient.isPlaying && mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED && mouseEvent.isPrimaryButtonDown() && currentHeldPiece != null && isCurrentHeldPieceOverGridSpace) {
                triggerPiecePlace();
            } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED && mouseEvent.isSecondaryButtonDown()) {
                rotateCurrentPiece();
            } else {
                for (Map.Entry<UUID, GameObject> pair : gameObjects.entrySet()) {
                    if (pair.getValue().onMouseEvent(mouseEvent)) {
                        break;
                    }
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

    private void onEscapePressed() {
        if (currentHeldPiece != null) {
            removePieceFromHandError();
        }
    }

    private void moveCurrentPiece(Point3D to, UUID obj) {
        if (currentHeldPiece != null) {
            isCurrentHeldPieceOverGridSpace = true;
            currentHeldPiece.visual.setTranslation(to);
            int i = friendlyPlacingIndicators.indexOf(obj);
            currentHeldPiece.p.front = Coordinate.indexToCoordinates(i);
        }
    }

    private void rotateCurrentPiece() {
        if (currentHeldPiece != null) {
            currentHeldPiece.p.orientation = currentHeldPiece.p.orientation == Piece.PieceOrientation.RIGHT ? Piece.PieceOrientation.DOWN : Piece.PieceOrientation.RIGHT;
            if (currentHeldPiece.p.orientation == Piece.PieceOrientation.DOWN) {
                currentHeldPiece.visual.setYRotation(90.0);
            } else {
                currentHeldPiece.visual.setYRotation(0.0);
            }
        }
    }

    private class VisualPiece {
        public GameObject visual;
        public Piece p;
    }

    @Override
    public void updateUI(GUICommand command) {
        if (GUIClient.clientConnection.uuid != null && GUIClient.currentActiveGame != null && GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveGame)) {
            Game g = GUIClient.clientConnection.dataManager.getGame(GUIClient.clientConnection.uuid, GUIClient.currentActiveGame);
            switch (command.type) {
                case GAME_FOUND:
                    break;
                case PLACE_PIECE_SUCCESS:
                    placePiece(g);
                    break;
                case PLACE_PIECE_ERROR:
                    removePieceFromHandError();
                    break;
                case MOVE_SUCCESS_HIT:
                    break;
                case MOVE_SUCCESS_MISS:
                    break;
                case MOVE_FAIL:
                    break;
                case REFRESH:
                    updateBoard(g);
                    break;
                default:
                    break;
            }
        }
    }

    private void removePieceFromHand() {
        if (currentHeldPiece != null) {
            Mesh3DComponent m3dc = (Mesh3DComponent) currentHeldPiece.visual.getComponentOfType(Component.ComponentType.MESH3D);
            m3dc.meshView.setMaterial(MaterialManager.load("Team1.mat"));
        }
        isCurrentHeldPieceOverGridSpace = false;
    }

    private void removePieceFromHandError() {
        if (currentHeldPiece != null) {
            switch (currentHeldPiece.p.type) {
                case CARRIER:
                    currentHeldPiece.visual.setTranslation(toOurCoordinates(new Point3D(0.306967, -1.07643, 0.170897)));
                    break;
                case BATTLESHIP:
                    currentHeldPiece.visual.setTranslation(toOurCoordinates(new Point3D(0.307575, -0.159704, 0.170897)));
                    break;
                case CRUISER:
                    currentHeldPiece.visual.setTranslation(toOurCoordinates(new Point3D(0.460521, -1.07766, 0.170897)));
                    break;
                case SUBMARINE:
                    currentHeldPiece.visual.setTranslation(toOurCoordinates(new Point3D(0.460521, -0.465165, 0.170897)));
                    break;
                case DESTROYER:
                    currentHeldPiece.visual.setTranslation(toOurCoordinates(new Point3D(0.460269, 0.146188, 0.170897)));
                    break;
                default:
                    break;

            }
            currentHeldPiece.visual.setYRotation(0.0);
            currentHeldPiece.visual.childrenHolder.setMouseTransparent(false);
            removePieceFromHand();
            currentHeldPiece = null;
        }
    }

    private void updateBoard(Game g) {
        if (g.winner != Game.Player.NONE) {
            // Game over
            onGameEnded();
            return;
        }

        Game deltaGame = null;
        if (oldGame != null) {
            deltaGame = g.getDelta(oldGame);
            oldGame = g;
        } else {
            deltaGame = g;
            oldGame = deltaGame;
        }
        System.out.println(deltaGame);

        boolean isP1 = GUIClient.clientConnection.uuid.equals(g.player1);
        ourPlayer = isP1 ? Game.Player.PLAYER1 : Game.Player.PLAYER2;

        if (isP1) {
            if (g.player1Pieces.size() == 5) {
                if (g.player2Pieces.size() != 5) {
                    displayWaitingForEnemyMessage();
                } else {
                    GUIClient.isPlaying = true;
                    hideWaitingMessage();
                }
            } else {
                displayWaitingForYouMessage();
            }
            // We are player 1, player 2 moves displayed on our board
            for (Move m : g.player2Moves) {
                UUID indicatorID = friendlyPlacingIndicators.get(Coordinate.coordinatesToIndex(m.position));
                Box3DComponent b3dc = (Box3DComponent) gameObjects.get(indicatorID).getComponentOfType(Component.ComponentType.BOX3D);
                if (m.isHit) {
                    b3dc.box.setMaterial(MaterialManager.load("Hit.mat"));
                    b3dc.box.setHeight(0.35);
                } else {
                    b3dc.box.setMaterial(MaterialManager.load("Miss.mat"));
                }
            }

            for (Move m : g.player1Moves) {
                UUID indicatorID = enemyPlacingIndicators.get(Coordinate.coordinatesToIndex(m.position));
                Box3DComponent b3dc = (Box3DComponent) gameObjects.get(indicatorID).getComponentOfType(Component.ComponentType.BOX3D);
                if (m.isHit) {
                    b3dc.box.setMaterial(MaterialManager.load("Hit.mat"));
                } else {
                    b3dc.box.setMaterial(MaterialManager.load("Miss.mat"));
                }
            }
        } else {
            if (g.player2Pieces.size() == 5) {
                if (g.player1Pieces.size() != 5) {
                    displayWaitingForEnemyMessage();
                } else {
                    GUIClient.isPlaying = true;
                    hideWaitingMessage();
                }
            } else {
                displayWaitingForYouMessage();
            }
            // We are player 2, player 1 moves displayed on our board
            for (Move m : g.player1Moves) {
                UUID indicatorID = friendlyPlacingIndicators.get(Coordinate.coordinatesToIndex(m.position));
                Box3DComponent b3dc = (Box3DComponent) gameObjects.get(indicatorID).getComponentOfType(Component.ComponentType.BOX3D);
                if (m.isHit) {
                    b3dc.box.setMaterial(MaterialManager.load("Hit.mat"));
                    b3dc.box.setHeight(0.15);
                } else {
                    b3dc.box.setMaterial(MaterialManager.load("Miss.mat"));
                }
            }

            for (Move m : g.player2Moves) {
                UUID indicatorID = enemyPlacingIndicators.get(Coordinate.coordinatesToIndex(m.position));
                Box3DComponent b3dc = (Box3DComponent) gameObjects.get(indicatorID).getComponentOfType(Component.ComponentType.BOX3D);
                if (m.isHit) {
                    b3dc.box.setMaterial(MaterialManager.load("Hit.mat"));
                } else {
                    b3dc.box.setMaterial(MaterialManager.load("Miss.mat"));
                }
            }
        }
    }

    private void placePiece(Game g) {
        removePieceFromHand();
        currentHeldPiece = null;
        updateBoard(g);
    }

    private void hideWaitingMessage() {
        waitingMessage.setVisible(false);
    }

    private void displayWaitingForYouMessage() {
        waitingMessage.setMaterial(MaterialManager.load("WaitingForYou.mat"));
        waitingMessage.setVisible(true);
    }

    private void displayWaitingForEnemyMessage() {
        waitingMessage.setMaterial(MaterialManager.load("WaitingForEnemy.mat"));
        waitingMessage.setVisible(true);
    }

    @Override
    public void onResizeWidth(Number oldVal, Number newVal) {

    }

    @Override
    public void onResizeHeight(Number oldVal, Number newVal) {

    }

    private void onGameEnded() {
        oldGame = null;

        resetBoard();
        resetShipPositions();

        GUIClient.isPlaying = false;
        GUIClient.currentActiveGame = null;

        mediaPlayer.pause();

        HomeController homeController = (HomeController) GUIClient.viewMap.get("home").controller;
        homeController.mediaPlayer.play();

        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
    }

    private Point3D toOurCoordinates(Point3D blenderCoordinates) {
        return new Point3D(blenderCoordinates.getX(), -blenderCoordinates.getZ(), blenderCoordinates.getY());
    }

    private void generatePlacingIndicators() {
        // Generate enemy board
        {
            Point3D blenderTopLeft = new Point3D(-0.630109, -1.0764, 1.67897);
            Point3D blenderBottomRight = new Point3D(-0.041363, 0.298623, 0.436606);

            Point3D ourTopLeft = toOurCoordinates(blenderTopLeft);
            Point3D ourBottomRight = toOurCoordinates(blenderBottomRight);

            Point3D delta = ourBottomRight.subtract(ourTopLeft);
            Point3D totalDeltaAcrossColumns = new Point3D(0.0, 0.0, delta.getZ());
            Point3D totalDeltaAcrossRows = new Point3D(delta.getX(), delta.getY(), 0.0);

            Point3D deltaPerRow = totalDeltaAcrossRows.multiply(0.11111111111);
            Point3D deltaPerColumn = totalDeltaAcrossColumns.multiply(0.11111111111);

            for (int y = 0; y < 10; ++y) {
                Point3D thisRow = ourTopLeft.add(deltaPerRow.multiply(y));
                for (int x = 0; x < 10; ++x) {
                    Box3DComponent b3dc = new Box3DComponent();
                    b3dc.box.setWidth(0.05);
                    b3dc.box.setHeight(0.05);
                    b3dc.box.setDepth(0.05);
                    b3dc.box.setMaterial(MaterialManager.load("BoardPosition.mat"));
                    b3dc.onPressedCallback = ((UUID id) -> {
                        triggerSelect(id);
                    });
                    Point3D newLoc = thisRow.add(deltaPerColumn.multiply(x));
                    GameObject newGameObj = new GameObject();
                    newGameObj.addComponent(b3dc);
                    newGameObj.setTranslation(newLoc);
                    gameObjects.put(newGameObj.id, newGameObj);
                    enemyPlacingIndicators.add(newGameObj.id);
                }
            }
        }
        // Generate friendly board
        {
            Point3D blenderTopLeft = new Point3D(0.693604, -1.07643, 0.170897);
            Point3D blenderBottomRight = new Point3D(2.06903, 0.298549, 0.170897);

            Point3D ourTopLeft = toOurCoordinates(blenderTopLeft);
            Point3D ourBottomRight = toOurCoordinates(blenderBottomRight);

            Point3D delta = ourBottomRight.subtract(ourTopLeft);
            Point3D totalDeltaAcrossColumns = new Point3D(0.0, 0.0, delta.getZ());
            Point3D totalDeltaAcrossRows = new Point3D(delta.getX(), delta.getY(), 0.0);

            Point3D deltaPerRow = totalDeltaAcrossRows.multiply(0.11111111111);
            Point3D deltaPerColumn = totalDeltaAcrossColumns.multiply(0.11111111111);

            for (int y = 0; y < 10; ++y) {
                Point3D thisRow = ourTopLeft.add(deltaPerRow.multiply(y));
                for (int x = 0; x < 10; ++x) {
                    Box3DComponent b3dc = new Box3DComponent();
                    b3dc.box.setWidth(0.05);
                    b3dc.box.setHeight(0.05);
                    b3dc.box.setDepth(0.05);
                    b3dc.box.setMaterial(MaterialManager.load("BoardPosition.mat"));
                    b3dc.canSelect = false;
                    b3dc.onPressedCallback = ((UUID id) -> {
                        if (!GUIClient.isPlaying) {
                            triggerPiecePlace();
                        }
                    });
                    Point3D newLoc = thisRow.add(deltaPerColumn.multiply(x));
                    GameObject newGameObj = new GameObject();
                    newGameObj.addComponent(b3dc);
                    newGameObj.setTranslation(newLoc);
                    gameObjects.put(newGameObj.id, newGameObj);
                    friendlyPlacingIndicators.add(newGameObj.id);
                }
            }
        }
    }

    private void triggerPiecePlace() {
        if (currentHeldPiece != null) {
            PlacePiece p = new PlacePiece();
            p.piece = currentHeldPiece.p;
            p.opponent = GUIClient.currentActiveGame;
            GUIClient.clientConnection.send(new Packet(p));
        }
    }

    private void triggerSelect(UUID id) {
        selectedSquare = id;
        actionSFX.play();
    }

    private void resetShipPositions() {
        gameObjects.get(ships.get(0)).setTranslation(toOurCoordinates(new Point3D(0.306967, -1.07643, 0.170897)));
        gameObjects.get(ships.get(0)).childrenHolder.setMouseTransparent(false);
        gameObjects.get(ships.get(0)).setYRotation(0.0);
        gameObjects.get(ships.get(1)).setTranslation(toOurCoordinates(new Point3D(0.307575, -0.159704, 0.170897)));
        gameObjects.get(ships.get(1)).childrenHolder.setMouseTransparent(false);
        gameObjects.get(ships.get(1)).setYRotation(0.0);
        gameObjects.get(ships.get(2)).setTranslation(toOurCoordinates(new Point3D(0.460521, -1.07766, 0.170897)));
        gameObjects.get(ships.get(2)).childrenHolder.setMouseTransparent(false);
        gameObjects.get(ships.get(2)).setYRotation(0.0);
        gameObjects.get(ships.get(3)).setTranslation(toOurCoordinates(new Point3D(0.460521, -0.465165, 0.170897)));
        gameObjects.get(ships.get(3)).childrenHolder.setMouseTransparent(false);
        gameObjects.get(ships.get(3)).setYRotation(0.0);
        gameObjects.get(ships.get(4)).setTranslation(toOurCoordinates(new Point3D(0.460269, 0.146188, 0.170897)));
        gameObjects.get(ships.get(4)).childrenHolder.setMouseTransparent(false);
        gameObjects.get(ships.get(4)).setYRotation(0.0);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("game", new GUIView(null, this));

        actionSFX = new AudioClip(getClass().getResource("/audio/Ping.mp3").toExternalForm());
        actionSFX.setVolume(GUIClient.volumeGame);

        fireSFX = new AudioClip(getClass().getResource("/audio/EnemyExplosion.mp3").toExternalForm());
        fireSFX.setVolume(GUIClient.volumeGame);

        hitSFX = new AudioClip(getClass().getResource("/audio/FriendlyExplosion.mp3").toExternalForm());
        hitSFX.setVolume(GUIClient.volumeGame);

        Media media = new Media(getClass().getResource("/audio/InGameMusic.mp3").toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(GUIClient.volumeGameMusic);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            }
        });
        mediaPlayer.pause();


        GameObject cameraComp = new GameObject();
        OrbitalCameraComponent occ = new OrbitalCameraComponent();
        cameraComp.addComponent(occ);
        cameraComp.setTranslation(0.6, -0.75, 0.0);
        occ.camera.setTranslateZ(-3.4);
        cameraComp.setXRotation(-35.0);
        cameraComp.setYRotation(-90.0);
        cameraComp.setZRotation(0.0);

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
        //frames.add("Explosion/Explosion0.obj");
        //frames.add("Explosion/Explosion1.obj");
        //frames.add("Explosion/Explosion2.obj");
        //frames.add("Explosion/Explosion3.obj");
        //Mesh3D m = MeshManager.loadAnimated(frames);
        //GameObject g2 = new GameObject();
        //g2.setTranslation(0.0, 100.0, 0.0);
        //{
        //    Mesh3DComponent m3dc = new Mesh3DComponent();
        //    m3dc.mesh3D = m;
        //    g2.addComponent(m3dc);
        //    m3dc.meshView.setMaterial(MaterialManager.load("Explosion.mat"));
        //}
        //{
        //    AnimationControllerComponent acp = new AnimationControllerComponent();
        //    acp.setShouldLoop(true);
        //    acp.setTimeBetweenFrames(0.25);
        //    g2.addComponent(acp);
        //    //acp.startPlaying();
        //}
        //gameObjects.put(g2.id, g2);

        Mesh3D board = MeshManager.load("Board.obj");
        GameObject g3 = new GameObject();
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = board;
            g3.addComponent(m3dc);
            m3dc.meshView.setMouseTransparent(true);
            m3dc.meshView.setMaterial(MaterialManager.load("Board.mat"));

            Mesh3DComponent m3dc1 = new Mesh3DComponent();
            m3dc1.mesh3D = MeshManager.load("InstructionsLeft.obj");
            m3dc1.canSelect = false;
            g3.addComponent(m3dc1);
            m3dc1.meshView.setMouseTransparent(true);
            m3dc1.meshView.setMaterial(MaterialManager.load("InstructionsLeft.mat"));
            Mesh3DComponent m3dc2 = new Mesh3DComponent();
            m3dc2.mesh3D = MeshManager.load("InstructionsRight.obj");
            m3dc2.canSelect = false;
            g3.addComponent(m3dc2);
            m3dc2.meshView.setMouseTransparent(true);
            m3dc2.meshView.setMaterial(MaterialManager.load("InstructionsRight.mat"));
            Mesh3DComponent m3dc3 = new Mesh3DComponent();
            m3dc3.mesh3D = MeshManager.load("QuitButton.obj");
            m3dc3.canSelect = false;
            g3.addComponent(m3dc3);
            m3dc3.meshView.setMouseTransparent(false);
            m3dc3.onPressedCallback = (UUID id) -> {
                onLeaveGame();
            };
            m3dc3.meshView.setMaterial(MaterialManager.load("QuitButton.mat"));
        }
        gameObjects.put(g3.id, g3);

        Mesh3D labels = MeshManager.load("Labels.obj");
        GameObject g4 = new GameObject();
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = labels;
            g3.addComponent(m3dc);
            m3dc.meshView.setMouseTransparent(true);
            m3dc.meshView.setMaterial(MaterialManager.load("Labels.mat"));
        }
        gameObjects.put(g4.id, g4);

        Mesh3D fireButton = MeshManager.load("Button.obj");
        GameObject g5 = new GameObject();
        {
            Mesh3DComponent m3dc = new Mesh3DComponent();
            m3dc.mesh3D = fireButton;
            m3dc.canSelect = false;
            m3dc.onPressedCallback = ((UUID id) -> {
                if (GUIClient.isPlaying) {
                    triggerFire();
                }
            });
            g3.addComponent(m3dc);
            m3dc.meshView.setMouseTransparent(false);
            m3dc.meshView.setMaterial(MaterialManager.load("Button.mat"));
        }
        gameObjects.put(g5.id, g5);

        waitingMessage = new MeshView(MeshManager.load("WaitingMessage.obj").mesh);
        waitingMessage.setMaterial(MaterialManager.load("WaitingForYou.mat"));
        g3.childrenHolder.getChildren().add(waitingMessage);

        generatePlacingIndicators();

        createShips();
        resetShipPositions();

        gameObjects.forEach((k, v) -> {
            root.getChildren().add(v.childrenHolder);
        });

        root.setFocusTraversable(true);
    }

    private void onLeaveGame() {
        HomeController hc = (HomeController) GUIClient.viewMap.get("home").controller;
        hc.buttonPressed();

        LeaveGame lg = new LeaveGame();
        lg.otherUser = GUIClient.currentActiveGame;
        GUIClient.clientConnection.send(new Packet(lg));

        currentHeldPiece = null;
        onGameEnded();
    }

    private void triggerFire() {
        if (selectedSquare != null) {
            if (GUIClient.clientConnection.uuid != null) {
                if (GUIClient.currentActiveGame != null) {
                    fireSFX.play();
                    Game g = GUIClient.clientConnection.dataManager.getGame(GUIClient.clientConnection.uuid, GUIClient.currentActiveGame);
                    synchronized (g.turn) {
                        if (ourPlayer == g.turn){
                            int index = enemyPlacingIndicators.indexOf(selectedSquare);
                            MakeMove m = new MakeMove();
                            m.move = new Move(GUIClient.clientConnection.uuid);
                            m.move.position = Coordinate.indexToCoordinates(index);
                            m.otherUser = GUIClient.currentActiveGame;
                            GUIClient.clientConnection.send(new Packet(m));
                        }
                    }
                }
            }
        }
    }

    private void resetBoard() {
        for (UUID posID : enemyPlacingIndicators) {
            gameObjects.get(posID).childrenHolder.setMouseTransparent(false);
            Box3DComponent b3dc = (Box3DComponent) gameObjects.get(posID).getComponentOfType(Component.ComponentType.BOX3D);
            b3dc.box.setMaterial(MaterialManager.load("BoardPosition.mat"));
        }
        for (UUID posID : friendlyPlacingIndicators) {
            gameObjects.get(posID).childrenHolder.setMouseTransparent(false);
            Box3DComponent b3dc = (Box3DComponent) gameObjects.get(posID).getComponentOfType(Component.ComponentType.BOX3D);
            b3dc.box.setMaterial(MaterialManager.load("BoardPosition.mat"));
            b3dc.box.setHeight(0.05);
        }
        displayWaitingForYouMessage();
        selectedSquare = null;
    }

    private void createShip(String meshName, Piece.PieceType type) {
        GameObject ship = new GameObject();
        Mesh3DComponent m3dc = new Mesh3DComponent();
        m3dc.mesh3D = MeshManager.load(meshName);
        ship.addComponent(m3dc);
        m3dc.meshView.setMaterial(MaterialManager.load("Team1.mat"));
        m3dc.canSelect = false;
        m3dc.onPressedCallback = (UUID id) -> {
            currentHeldPiece = new VisualPiece();
            currentHeldPiece.visual = ship;
            currentHeldPiece.p = new Piece();
            currentHeldPiece.p.orientation = Piece.PieceOrientation.RIGHT;
            currentHeldPiece.p.type = type;
            currentHeldPiece.visual.childrenHolder.setMouseTransparent(true);
            m3dc.meshView.setMaterial(MaterialManager.load("PlacingShip.mat"));
            actionSFX.play();
        };
        gameObjects.put(ship.id, ship);
        ships.add(ship.id);
    }

    private void createShips() {
        createShip("AircraftCarrier.obj", Piece.PieceType.CARRIER);
        createShip("Battleship.obj", Piece.PieceType.BATTLESHIP);
        createShip("Cruiser.obj", Piece.PieceType.CRUISER);
        createShip("Submarine.obj", Piece.PieceType.SUBMARINE);
        createShip("Destroyer.obj", Piece.PieceType.DESTROYER);
    }

    @Override
    public void onRenderUpdate(double deltaTime) {
        gameObjects.forEach((k, v) -> {
            v.onRenderUpdate(deltaTime);
        });

        actionSFX.setVolume(GUIClient.volumeGame);
        hitSFX.setVolume(GUIClient.volumeGame);
        fireSFX.setVolume(GUIClient.volumeGame);
        mediaPlayer.setVolume(GUIClient.volumeGameMusic);
    }
}
