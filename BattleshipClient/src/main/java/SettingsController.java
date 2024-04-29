import Data.*;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

public class SettingsController implements CustomController, Initializable {
    public Button backButton;
    public Slider gameVolumeSlider;
    public Slider musicVolumeSlider;
    public Slider menuVolumeSlider;
    public Slider gameMusicVolumeSlider;

    public void sendButtonPressed(ActionEvent actionEvent) {
        synchronized (GUIClient.clientConnection.dataManager) {
        }
    }

    private void refreshGUI() {
        synchronized (GUIClient.clientConnection.dataManager) {
            User u = GUIClient.clientConnection.dataManager.users.get(GUIClient.clientConnection.uuid);
            GUIClient.primaryStage.setTitle(u.username);
        }
    }

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

    }

    public void buttonPressed() {
        ((HomeController) GUIClient.viewMap.get("home").controller).buttonPressed();
    }

    public void backButtonPressed(ActionEvent actionEvent) {
        buttonPressed();
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
    }

    public void onMouseEnteredBackButton(MouseEvent mouseEvent) {
        backButton.requestFocus();
    }

    public void onMouseInteractedSlider(MouseEvent mouseEvent) {
        buttonPressed();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("settings", new GUIView(null, this));
        gameVolumeSlider.setValue(GUIClient.volumeGame * 500.0);
        musicVolumeSlider.setValue(GUIClient.volumeMusic * 500.0);
        menuVolumeSlider.setValue(GUIClient.volumeMenuSFX * 500.0);
        gameMusicVolumeSlider.setValue(GUIClient.volumeGameMusic * 500.0);
        gameVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                GUIClient.volumeGame = newVal.doubleValue() * 0.002;
            }
        });
        musicVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                GUIClient.volumeMusic = newVal.doubleValue() * 0.002;
            }
        });
        menuVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                GUIClient.volumeMenuSFX = newVal.doubleValue() * 0.002;
            }
        });
        gameMusicVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                GUIClient.volumeGameMusic = newVal.doubleValue() * 0.002;
            }
        });
    }
}
