import Data.LoginAttempt;
import Data.Payload;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements CustomController, Initializable {
    public Label invalidUsernameIndicator;
    public TextField usernameEntryField;
    public Button loginButton;

    public void loginButtonPressed(ActionEvent actionEvent) {
        synchronized (GUIClient.clientConnection.dataManager) {
            String originalEntry = usernameEntryField.getText();
            if (!originalEntry.isEmpty()) {
                String usernameEntry = originalEntry.trim();
                if (originalEntry.equals(usernameEntry)) {
                    GUIClient.clientConnection.lastOperation = Payload.Type.LOGIN_ATTEMPT;
                    System.out.println("Attempting login to server");

                    LoginAttempt m = new LoginAttempt();
                    m.username = usernameEntry;

                    GUIClient.clientConnection.send(new Packet(m));
                } else {
                    invalidUsernameIndicator.setText("Username cannot have trailing or leading whitespace");
                    invalidUsernameIndicator.setVisible(true);
                }
            } else {
                invalidUsernameIndicator.setText("Please enter a username");
                invalidUsernameIndicator.setVisible(true);
            }
        }
    }

    public void onUsernameEntryKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            loginButtonPressed(new ActionEvent());
        }
    }

    public void onInvalidLogin() {
        invalidUsernameIndicator.setText("Username taken");
        invalidUsernameIndicator.setVisible(true);
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case LOGIN_ERROR:
                onInvalidLogin();
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GUIClient.viewMap.put("login", new GUIView(null, this));
    }
}
