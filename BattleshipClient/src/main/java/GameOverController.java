import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GameOverController implements CustomController, Initializable {
    public Button mainMenuButton;
    public AnchorPane root;
    public ImageView defeatDisplay;
    public ImageView victoryDisplay;
    public MediaPlayer defeatMusic = null;
    public MediaPlayer victoryMusic = null;

    @Override
    public void postInit() {
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
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
    public void onRenderUpdate(double deltaTime) {
        defeatMusic.setVolume(GUIClient.volumeMenuMusic);
        victoryMusic.setVolume(GUIClient.volumeMenuMusic);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("gameover", new GUIView(null, this));

        Media victoryMedia = new Media(getClass().getResource("/audio/GameWin.mp3").toExternalForm());
        victoryMusic = new MediaPlayer(victoryMedia);
        victoryMusic.setVolume(GUIClient.volumeMenuMusic);
        victoryMusic.setAutoPlay(true);
        victoryMusic.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                victoryMusic.seek(Duration.ZERO);
                victoryMusic.play();
            }
        });
        victoryMusic.pause();

        Media defeatMedia = new Media(getClass().getResource("/audio/GameLoss.mp3").toExternalForm());
        defeatMusic = new MediaPlayer(defeatMedia);
        defeatMusic.setVolume(GUIClient.volumeMenuMusic);
        defeatMusic.setAutoPlay(true);
        defeatMusic.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                defeatMusic.seek(Duration.ZERO);
                defeatMusic.play();
            }
        });
        defeatMusic.pause();
    }

    public void displayWin() {
        defeatDisplay.setVisible(false);
        victoryDisplay.setVisible(true);
        root.getStylesheets().clear();
        root.getStylesheets().add("styles/shared.css");
        root.getStyleClass().clear();
        root.getStyleClass().add("background5");
        victoryMusic.play();
    }

    public void displayLoss() {
        defeatDisplay.setVisible(true);
        victoryDisplay.setVisible(false);
        root.getStylesheets().clear();
        root.getStylesheets().add("styles/shared.css");
        root.getStyleClass().clear();
        root.getStyleClass().add("background7");
        defeatMusic.play();
    }

    public void mainMenuButtonPressed(ActionEvent actionEvent) {
        GUIView v = GUIClient.viewMap.get("home");
        Scene s = v.scene;
        HomeController hc = (HomeController) v.controller;
        hc.buttonPressed();

        victoryMusic.pause();
        defeatMusic.pause();
        GUIClient.primaryStage.setScene(s);

        hc.mediaPlayer.play();
    }

    public void onMouseEnteredMainMenu(MouseEvent mouseEvent) {
        mainMenuButton.requestFocus();
    }
}
