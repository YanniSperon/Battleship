import Data.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

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
        musicVolumeSlider.setValue(GUIClient.volumeMenuMusic * 500.0);
        menuVolumeSlider.setValue(GUIClient.volumeMenuSFX * 500.0);
        gameMusicVolumeSlider.setValue(GUIClient.volumeGameMusic * 500.0);
        gameVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                GUIClient.volumeGame = newVal.doubleValue() * 0.002;
            }
        });
        musicVolumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) {
                GUIClient.volumeMenuMusic = newVal.doubleValue() * 0.002;
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
