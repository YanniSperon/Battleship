
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Data.Group;
import Data.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIServer extends Application {

    HashMap<String, Scene> sceneMap;
    public static Server serverConnection;

    ListView<String> logListView, usersListView;
    //ListView<String> groupsListView, chatListView;

//    Label activeChatLabel;
    UUID activeChat;
//    UUID activeChat2;

    public static void main(String[] args) {
        launch(args);
    }

//    private void updateChat() {
//        synchronized (serverConnection.dataManager) {
//            chatListView.getItems().clear();
////            if (serverConnection.dataManager.isValidGroup(activeChat)) {
////                activeChatLabel.setText("Group \"" + serverConnection.dataManager.groups.get(activeChat).name + "\" chat");
////                for (Data.Message m : serverConnection.dataManager.getGroupChat(activeChat).messages) {
////                    String senderName = serverConnection.dataManager.users.get(m.sender).username;
////                    if (senderName == null || senderName.equals("Server")) {
////                        chatListView.getItems().add(m.content);
////                    } else {
////                        chatListView.getItems().add(senderName + ": " + m.content);
////                    }
////                }
////            } else
//            if (serverConnection.dataManager.isValidUser(activeChat)) {
//                if (serverConnection.dataManager.isValidUser(activeChat2)) {
//                    activeChatLabel.setText("Game between \"" + serverConnection.dataManager.users.get(activeChat).username + "\" and \"" + serverConnection.dataManager.users.get(activeChat2).username + "\"");
//                    for (Data.Message m : serverConnection.dataManager.getDM(activeChat, activeChat2).messages) {
//                        String senderName = serverConnection.dataManager.users.get(m.sender).username;
//                        if (senderName == null || senderName.equals("Server")) {
//                            chatListView.getItems().add(m.content);
//                        } else {
//                            chatListView.getItems().add(senderName + ": " + m.content);
//                        }
//                    }
//                } else {
//                    activeChatLabel.setText("Global status (select another user to view game)");
//                    for (Data.Message m : serverConnection.dataManager.getGroupChat(serverConnection.globalChat.uuid).messages) {
//                        String senderName = serverConnection.dataManager.users.get(m.sender).username;
//                        if (senderName == null || senderName.equals("Server")) {
//                            chatListView.getItems().add(m.content);
//                        } else {
//                            chatListView.getItems().add(senderName + ": " + m.content);
//                        }
//                    }
//                }
//            } else {
//                activeChatLabel.setText("Global status (select two users to view game)");
//                for (Data.Message m : serverConnection.dataManager.getGroupChat(serverConnection.globalChat.uuid).messages) {
//                    String senderName = serverConnection.dataManager.users.get(m.sender).username;
//                    if (senderName == null || senderName.equals("Server")) {
//                        chatListView.getItems().add(m.content);
//                    } else {
//                        chatListView.getItems().add(senderName + ": " + m.content);
//                    }
//                }
//            }
//            if (chatListView.getItems().isEmpty()) {
//                chatListView.getItems().add("");
//            }
//        }
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logListView = new ListView<String>();
        usersListView = new ListView<String>();
        //groupsListView = new ListView<String>();
        //chatListView = new ListView<String>();

        sceneMap = new HashMap<String, Scene>();

        sceneMap.put("server", createServerGUI());

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        primaryStage.setScene(sceneMap.get("server"));
        primaryStage.setTitle("Server");
        primaryStage.show();


        serverConnection = new Server(data -> {
            Platform.runLater(() -> {
                logListView.getItems().add(data.toString());
                usersListView.getItems().clear();
                boolean anyAdded = false;
                synchronized (serverConnection.dataManager) {
                    for (Map.Entry<UUID, User> pair : serverConnection.dataManager.users.entrySet()) {
                        if (pair.getValue().username != null && !pair.getValue().username.equals("Server")) {
                            usersListView.getItems().add(pair.getValue().toDisplayString());
                            anyAdded = true;
                        }
                    }
                    if (!anyAdded) {
                        usersListView.getItems().add("");
                    }
//                    groupsListView.getItems().clear();
//                    for (Map.Entry<UUID, Group> pair : serverConnection.dataManager.groups.entrySet()) {
//                        groupsListView.getItems().add(pair.getValue().toDisplayString());
//                    }
                }
//                updateChat();
            });
        });

        activeChat = serverConnection.globalChat.uuid;
    }

    public Scene createServerGUI() {

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(70));
        pane.setStyle("-fx-background-color: coral");

        Label centerLLabel = new Label("Log");
        centerLLabel.setFont(Font.font("serif", FontWeight.BOLD, 16));
        centerLLabel.setAlignment(Pos.CENTER);
        VBox leftVBox = new VBox(10, centerLLabel, logListView);
        leftVBox.setAlignment(Pos.CENTER);
        pane.setCenter(leftVBox);
//
//        activeChatLabel = new Label("Group status (select two users to view game)");
//        activeChatLabel.setAlignment(Pos.CENTER);
//        activeChatLabel.setFont(Font.font("serif", FontWeight.BOLD, 16));
//        VBox centerVBox = new VBox(10, activeChatLabel, chatListView);
//        centerVBox.setAlignment(Pos.CENTER);
//        pane.setCenter(centerVBox);
//
        Label rightLabel = new Label("Users");
        rightLabel.setFont(Font.font("serif", FontWeight.BOLD, 16));
        rightLabel.setAlignment(Pos.CENTER);
        VBox rightVbox = new VBox(10, rightLabel, usersListView);
//        usersListView.setOnMouseClicked((e) -> {
//            String selectedItem = usersListView.getSelectionModel().getSelectedItem();
//            if (selectedItem != null && !selectedItem.isEmpty()) {
//                if (activeChat != null && activeChat2 != null) {
//                    synchronized (serverConnection.dataManager) {
//                        activeChat = serverConnection.dataManager.getByUsername(selectedItem);
//                    }
//                    activeChat2 = null;
//                } else if (activeChat != null) {
//                    synchronized (serverConnection.dataManager) {
//                        if (serverConnection.dataManager.isValidUser(activeChat)) {
//                            activeChat2 = serverConnection.dataManager.getByUsername(selectedItem);
//                            if (activeChat.equals(activeChat2)) {
//                                activeChat2 = null;
//                            }
//                        } else {
//                            activeChat = serverConnection.dataManager.getByUsername(selectedItem);
//                        }
//                    }
//                } else {
//                    synchronized (serverConnection.dataManager) {
//                        activeChat = serverConnection.dataManager.getByUsername(selectedItem);
//                    }
//                }
//                updateChat();
//            }
//        });
        rightVbox.setAlignment(Pos.CENTER);
        pane.setRight(rightVbox);

        pane.setStyle("-fx-font-family: 'serif'");
        return new Scene(pane, 1400, 800);
    }
}
