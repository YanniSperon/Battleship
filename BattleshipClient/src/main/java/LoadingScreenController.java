import Data.*;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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

public class LoadingScreenController implements CustomController, Initializable {
    public Button mainMenuButton;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("loadingscreen", new GUIView(null, this));
    }
}
