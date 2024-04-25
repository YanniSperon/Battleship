import Data.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

public class HomeController implements CustomController, Initializable {
    public TextField messageEntryField;
    public Button sendButton;
    public Label groupsLabel;
    public Label chatNameIndicator;
    public Button createGroupButton;
    public ListView<String> contentView;
    public ListView<String> userDisplay;
    public ListView<String> groupDisplay;

    public void groupDisplayClicked(MouseEvent mouseEvent) {
        String selectedItem = groupDisplay.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.isEmpty()) {
            System.out.println("List item " + selectedItem + " was clicked");
            GUIClient.currentActiveChat = GUIClient.clientConnection.dataManager.getByGroupName(selectedItem);
            updateUI(new GUICommand(GUICommand.Type.REFRESH));
        }
    }

    public void userDisplayClicked(MouseEvent mouseEvent) {
        String selectedItem = userDisplay.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.isEmpty()) {
            System.out.println("List item " + selectedItem + " was clicked");
            UUID selected = GUIClient.clientConnection.dataManager.getByUsername(selectedItem);
            System.out.println("Client connection is " + GUIClient.clientConnection.uuid);
            System.out.println("Selected is " + selected);
            if (!selected.equals(GUIClient.clientConnection.uuid)) {
                GUIClient.currentActiveChat = selected;
            }
            updateUI(new GUICommand(GUICommand.Type.REFRESH));
        }
    }

    public void onMessageEntryKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendButtonPressed(new ActionEvent());
        }
    }

    public void sendButtonPressed(ActionEvent actionEvent) {
        synchronized (GUIClient.clientConnection.dataManager) {
            String message = messageEntryField.getText();
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
            }
            messageEntryField.clear();
        }
    }

    public void createGroupButtonPressed(ActionEvent actionEvent) {
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("createGroup").scene);
    }

    private void onLoginSuccess() {
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
        synchronized (GUIClient.clientConnection.dataManager) {
            GUIClient.currentActiveChat = GUIClient.clientConnection.dataManager.getGlobalGroup();
        }
    }

    private void refreshGUI() {
        synchronized (GUIClient.clientConnection.dataManager) {
            if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
                chatNameIndicator.setText("DM with \"" + GUIClient.clientConnection.dataManager.users.get(GUIClient.currentActiveChat).username + "\"");
            } else if (GUIClient.currentActiveChat != null) {
                if (GUIClient.clientConnection.dataManager.isValidGroup(GUIClient.currentActiveChat)) {
                    chatNameIndicator.setText("Group \"" + GUIClient.clientConnection.dataManager.groups.get(GUIClient.currentActiveChat).name + "\"");
                } else {
                    GUIClient.currentActiveChat = null;
                }
            }
            String username = GUIClient.clientConnection.dataManager.users.get(GUIClient.clientConnection.uuid).username;
            if (username != null) {
                GUIClient.primaryStage.setTitle("Logged in as \"" + username + "\"");
            }

            groupDisplay.getItems().clear();
            for (Map.Entry<UUID, Group> pair : GUIClient.clientConnection.dataManager.groups.entrySet()) {
                groupDisplay.getItems().add(pair.getValue().toDisplayString());
            }

            boolean didAddAny = false;
            userDisplay.getItems().clear();
            for (Map.Entry<UUID, User> pair : GUIClient.clientConnection.dataManager.users.entrySet()) {
                if ((!pair.getKey().equals(GUIClient.clientConnection.uuid)) && (pair.getValue().username != null) && (!pair.getValue().username.equals("Server"))) {
                    userDisplay.getItems().add(pair.getValue().toDisplayString());
                    didAddAny = true;
                }
            }
            if (!didAddAny) {
                userDisplay.getItems().add("");
            }


            contentView.getItems().clear();
            if (GUIClient.clientConnection.dataManager.isValidGroup(GUIClient.currentActiveChat)) {
                Group gt = GUIClient.clientConnection.dataManager.groups.get(GUIClient.currentActiveChat);
                if (!gt.isPrivate || gt.users.contains(GUIClient.clientConnection.uuid) || (gt.creator != null && gt.creator.equals(GUIClient.clientConnection.uuid))) {
                    for (Data.Message m : GUIClient.clientConnection.dataManager.getGroupChat(GUIClient.currentActiveChat).messages) {
                        String senderName = GUIClient.clientConnection.dataManager.users.get(m.sender).username;
                        if (senderName == null || senderName.equals("Server")) {
                            contentView.getItems().add(m.content);
                        } else {
                            contentView.getItems().add(senderName + ": " + m.content);
                        }
                    }
                } else {
                    contentView.getItems().add("This is a private group chat that");
                    contentView.getItems().add("you do not have permission to view");
                }
            } else if (GUIClient.clientConnection.dataManager.isValidUser(GUIClient.currentActiveChat)) {
                if (GUIClient.clientConnection.dataManager.getDM(GUIClient.clientConnection.uuid, GUIClient.currentActiveChat) != null) {
                    for (Data.Message m : GUIClient.clientConnection.dataManager.getDM(GUIClient.clientConnection.uuid, GUIClient.currentActiveChat).messages) {
                        String senderName = GUIClient.clientConnection.dataManager.users.get(m.sender).username;
                        if (senderName == null || senderName.equals("Server")) {
                            contentView.getItems().add(m.content);
                        } else {
                            contentView.getItems().add(senderName + ": " + m.content);
                        }
                    }
                }
            } else {
                contentView.getItems().add("");
            }
        }
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
        GUIClient.viewMap.put("home", new GUIView(null, this));
    }
}
