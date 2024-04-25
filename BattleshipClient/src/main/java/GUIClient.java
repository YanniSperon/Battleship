
import java.util.HashMap;
import java.util.UUID;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIClient extends Application {
    private static final int WIDTH = 700;
    private static final int HEIGHT = 400;

    public static HashMap<String, GUIView> viewMap;
    public static Client clientConnection;

    public static UUID currentActiveChat = null;
    public static Stage primaryStage = null;

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
        createCreateGroupGUI();

        viewMap.forEach((k,v) -> {
            v.scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                v.controller.onResizeHeight(oldVal, newVal);
            });
            v.scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                v.controller.onResizeWidth(oldVal, newVal);
            });
        });

        primaryStage.setScene(viewMap.get("login").scene);
        primaryStage.setTitle("Not logged in");
        primaryStage.show();

    }

    public void createLoginGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/login.fxml"));
            Scene s1 = new Scene(root, WIDTH, HEIGHT);
            s1.getStylesheets().add("/styles/shared.css");
            viewMap.get("login").scene = s1;
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }

    public void createHomeGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/home.fxml"));
            Scene s1 = new Scene(root, WIDTH, HEIGHT);
            s1.getStylesheets().add("/styles/home.css");
            viewMap.get("home").scene = s1;
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }

    public void createCreateGroupGUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/createGroup.fxml"));
            Scene s1 = new Scene(root, WIDTH, HEIGHT);
            s1.getStylesheets().add("/styles/createGroup.css");
            viewMap.get("createGroup").scene = s1;
        } catch (Exception e) {
            System.out.println("Missing resources!");
            System.exit(1);
        }
    }
}
