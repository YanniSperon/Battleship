
import java.util.HashMap;
import java.util.UUID;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class GUIClient extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;

    public static HashMap<String, GUIView> viewMap;
    public static Client clientConnection;

    public static UUID currentActiveGame = null;
    public static UUID globalChat = null;
    public static Stage primaryStage = null;
    public static boolean isPlaying = false;

    public static double volumeMenuMusic = 0.03;
    public static double volumeMenuSFX = 0.05;
    public static double volumeGame = 0.05;
    public static double volumeGameMusic = 0.02;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage pStage) throws Exception {
        primaryStage = pStage;
        clientConnection = new Client(data -> {
            Platform.runLater(() -> {
                GUICommand c = (GUICommand) data;
                viewMap.forEach((k, v) -> {
                    v.controller.updateUI(c);
                });
            });
        });

        viewMap = new HashMap<String, GUIView>();

        clientConnection.start();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        createLoginGUI();
        createHomeGUI();
        createGameGUI();
        createSettingsGUI();
        createGameOverGUI();

        viewMap.forEach((k, v) -> {
            v.scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                v.controller.onResizeHeight(oldVal, newVal);
            });
            v.scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                v.controller.onResizeWidth(oldVal, newVal);
            });
            v.controller.postInit();
        });

        primaryStage.setScene(viewMap.get("login").scene);
        //primaryStage.setScene(viewMap.get("game").scene);
        primaryStage.setTitle("Not logged in");
        primaryStage.show();


        currentTime = System.nanoTime();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16.66666666), e -> onRenderUpdate()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }

    public void createLoginGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/login.fxml"));
            viewMap.get("login").scene = new Scene(root, WIDTH, HEIGHT);
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }

    public void createHomeGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/home.fxml"));
            viewMap.get("home").scene = new Scene(root, WIDTH, HEIGHT);
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }

    public void createGameGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/game.fxml"));
            viewMap.get("game").scene = new Scene(root, WIDTH, HEIGHT, true);
        } catch (Exception e) {
            System.out.println("Missing resources!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void createSettingsGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/settings.fxml"));
            viewMap.get("settings").scene = new Scene(root, WIDTH, HEIGHT, true);
        } catch (Exception e) {
            System.out.println("Missing resources!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void createGameOverGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/gameover.fxml"));
            viewMap.get("gameover").scene = new Scene(root, WIDTH, HEIGHT, true);
        } catch (Exception e) {
            System.out.println("Missing resources!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private long currentTime = 0;

    public void onRenderUpdate() {
        // Compute deltaTime
        long newTime = System.nanoTime();
        long timeDiff = newTime - currentTime;
        double deltaTime = timeDiff * 0.000000001;
        currentTime = newTime;

        viewMap.forEach((k, v) -> {
            v.controller.onRenderUpdate(deltaTime);
        });
    }
}
