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

public class HomeController implements CustomController, Initializable {
    public ProgressBar levelProgressBar;
    public Button levelIndicator;
    public Button findGameButton;
    public Button customGameButton;
    public Button settingsButton;
    public Button startButton;
    public Button joinButton;
    public ImageView subcategoryIndicator;
    public Button cancelFindGameButton;
    public ImageView loadingIcon;
    public Button playAIButton;

    public MediaPlayer mediaPlayer;
    private AudioClip navigationSFX;

    public void sendButtonPressed(ActionEvent actionEvent) {
        synchronized (GUIClient.clientConnection.dataManager) {
            //String message = messageEntryField.getText();
            /*String message = "TODO";
            if (GUIClient.clientConnection.dataManager.isValidGroup(GUIClient.currentActiveChat)) {
                GroupMessage m = new GroupMessage();
                m.message.content = message;
                m.message.sender = GUIClient.clientConnection.uuid;
                m.receivingGroup = GUIClient.currentActiveChat;
                GUIClient.clientConnection.send(new Packet(m));
            } else if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
                DirectMessage m = new DirectMessage();
                m.message.content = message;
                m.message.sender = GUIClient.clientConnection.uuid;
                m.receiver = GUIClient.currentActiveChat;
                GUIClient.clientConnection.send(new Packet(m));
            }*/
            //messageEntryField.clear();
        }
    }

    private void onLoginSuccess() {
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
        synchronized (GUIClient.clientConnection.dataManager) {
            GUIClient.globalChat = GUIClient.clientConnection.dataManager.getGlobalGroup();
        }
        focusCurrentButton();
    }

    private long getCurrentLevel(User u) {
        return Math.min((u.xp / 100) + 1, 10);
    }

    private double getDecimalToNextLevelRepresentation(User u) {
        if (getCurrentLevel(u) == 10) {
            return 1.0;
        } else {
            return Math.max((((double) (u.xp % 100)) * 0.01), 0.05);
        }
    }

    private void refreshGUI() {
        synchronized (GUIClient.clientConnection.dataManager) {
            User u = GUIClient.clientConnection.dataManager.users.get(GUIClient.clientConnection.uuid);
            GUIClient.primaryStage.setTitle(u.username);
            long level = getCurrentLevel(u);
            levelIndicator.getStyleClass().clear();
            levelIndicator.getStyleClass().add("level" + level + "Indicator");
            double ratioToNextLevel = getDecimalToNextLevelRepresentation(u);
            levelProgressBar.setProgress(ratioToNextLevel);
        }
    }

    @Override
    public void postInit() {
        Scene s = levelProgressBar.getScene();
        if (s == null) {
            System.out.println("Scene is null");
        } else {
            s.setOnKeyPressed(this::priorityKeyPress);
        }
    }

    private void onGameFound() {
        mediaPlayer.pause();
        GameController gc = (GameController) GUIClient.viewMap.get("game").controller;
        gc.mediaPlayer.play();
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("game").scene);

        synchronized (GUIClient.clientConnection.isSearchingForGame) {
            GUIClient.clientConnection.isSearchingForGame = true;
        }

        findGameButton.setVisible(true);
        cancelFindGameButton.setVisible(false);
        loadingIcon.setVisible(false);
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case LOGIN_SUCCESS:
                onLoginSuccess();
                break;
            case REFRESH:
            case GROUP_CREATE_SUCCESS:
                refreshGUI();
                break;
            case GAME_FOUND:
                onGameFound();
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
    public void onRenderUpdate(double deltaTime) {
        navigationSFX.setVolume(GUIClient.volumeMenuSFX);
        mediaPlayer.setVolume(GUIClient.volumeMusic);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("home", new GUIView(null, this));
        Media media = new Media(getClass().getResource("/audio/TitleScreenMusic.mp3").toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(GUIClient.volumeMusic);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            }
        });

        navigationSFX = new AudioClip(getClass().getResource("/audio/MenuNavigationSFX.mp3").toExternalForm());
        navigationSFX.setVolume(GUIClient.volumeMenuSFX);

        focusCurrentButton();

        RotateTransition rt = new RotateTransition(Duration.millis(1000), loadingIcon);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.play();
    }

    public void buttonPressed() {
        navigationSFX.play();
    }

    private void setHasSelectedCustomGame(boolean hasSelected) {
        hasSelectedCustomGame = hasSelected;

        startButton.setVisible(hasSelected);
        subcategoryIndicator.setVisible(hasSelected);
        joinButton.setVisible(hasSelected);

        if (hasSelected) {
            if (currFocus == 2) {
                currFocus = 3;
            }
            currFocusMin = 3;
            currFocusMax = 4;
        } else {
            if (currFocus == 3 || currFocus == 4) {
                currFocus = 1;
            }
            currFocusMin = 0;
            currFocusMax = 2;
        }

        focusCurrentButton();
    }

    public void findGameButtonPressed(ActionEvent actionEvent) {
        setHasSelectedCustomGame(false);
        buttonPressed();
        synchronized (GUIClient.clientConnection.isSearchingForGame) {
            GUIClient.clientConnection.isSearchingForGame = true;
        }
        FindGame m = new FindGame();
        m.shouldFindGame = true;
        GUIClient.clientConnection.send(new Packet(m));
        findGameButton.setVisible(false);
        cancelFindGameButton.setVisible(true);
        loadingIcon.setVisible(true);
        focusCurrentButton();
    }

    public void customGameButtonPressed(ActionEvent actionEvent) {
        setHasSelectedCustomGame(!hasSelectedCustomGame);
        buttonPressed();
    }

    public void settingsButtonPressed(ActionEvent actionEvent) {
        setHasSelectedCustomGame(false);
        buttonPressed();
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("settings").scene);
    }

    private boolean hasSelectedCustomGame = false;
    private int currFocus = 0;
    private int currFocusMin = 0;
    private int currFocusMax = 2;

    private void selectCurrentFocus() {
        switch (currFocus) {
            case 0:
                if (findGameButton.isVisible()) {
                    findGameButtonPressed(new ActionEvent());
                } else {
                    cancelFindGameButtonPressed(new ActionEvent());
                }
                break;
            case 1:
                playAIButtonPressed(new ActionEvent());
                break;
            case 2:
                settingsButtonPressed(new ActionEvent());
                break;
            case 3:
                //startButtonPressed(new ActionEvent());
                break;
            case 4:
                //joinButtonPressed(new ActionEvent());
                break;
            default:
                break;
        }
    }

    private void focusCurrentButton() {
        if (currFocus < currFocusMin) {
            currFocus = currFocusMax;
        } else if (currFocus > currFocusMax) {
            currFocus = currFocusMin;
        }
        switch (currFocus) {
            case 0:
                if (findGameButton.isVisible()) {
                    findGameButton.requestFocus();
                } else {
                    cancelFindGameButton.requestFocus();
                }
                break;
            case 1:
                playAIButton.requestFocus();
                break;
            case 2:
                settingsButton.requestFocus();
                break;
            case 3:
                startButton.requestFocus();
                break;
            case 4:
                joinButton.requestFocus();
            default:
                break;
        }
    }

    public void onMouseEnteredFindGame(MouseEvent mouseEvent) {
        if (!hasSelectedCustomGame) {
            currFocus = 0;
            focusCurrentButton();
        }
    }

    public void onMouseEnteredCustomGame(MouseEvent mouseEvent) {
        if (!hasSelectedCustomGame) {
            currFocus = 1;
            focusCurrentButton();
        }
    }

    public void onMouseEnteredSettings(MouseEvent mouseEvent) {
        if (!hasSelectedCustomGame) {
            currFocus = 2;
            focusCurrentButton();
        }
    }

    public void priorityKeyPress(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case UP:
            case W: {
                currFocus--;
                focusCurrentButton();
                break;
            }
            case DOWN:
            case S: {
                currFocus++;
                focusCurrentButton();
                break;
            }
            case D:
            case SPACE:
            case ENTER: {
                selectCurrentFocus();
                break;
            }
            case A: {
                setHasSelectedCustomGame(false);
                break;
            }
            default:
                break;
        }
    }

    public void startButtonPressed(ActionEvent actionEvent) {
        buttonPressed();
    }

    public void onMouseEnteredStartButton(MouseEvent mouseEvent) {
        if (hasSelectedCustomGame) {
            currFocus = 3;
            focusCurrentButton();
        }
    }

    public void onMouseEnteredJoinButton(MouseEvent mouseEvent) {
        if (hasSelectedCustomGame) {
            currFocus = 4;
            focusCurrentButton();
        }
    }

    public void joinButtonPressed(ActionEvent actionEvent) {
        buttonPressed();
    }

    public void cancelFindGameButtonPressed(ActionEvent actionEvent) {
        buttonPressed();
        setHasSelectedCustomGame(false);

        synchronized (GUIClient.clientConnection.isSearchingForGame) {
            GUIClient.clientConnection.isSearchingForGame = false;
        }
        FindGame m = new FindGame();
        m.shouldFindGame = false;
        GUIClient.clientConnection.send(new Packet(m));

        findGameButton.setVisible(true);
        cancelFindGameButton.setVisible(false);
        loadingIcon.setVisible(false);
        focusCurrentButton();
    }

    public void playAIButtonPressed(ActionEvent actionEvent) {
        currFocus = 1;
        setHasSelectedCustomGame(false);
        buttonPressed();
        synchronized (GUIClient.clientConnection.isSearchingForGame) {
            GUIClient.clientConnection.isSearchingForGame = true;
        }
        FindGame m = new FindGame();
        m.shouldFindGame = true;
        m.vsAI = true;
        GUIClient.clientConnection.send(new Packet(m));
        focusCurrentButton();
    }

    public void onMouseEnteredPlayAI(MouseEvent mouseEvent) {
        if (!hasSelectedCustomGame) {
            currFocus = 1;
            focusCurrentButton();
        }
    }
}
