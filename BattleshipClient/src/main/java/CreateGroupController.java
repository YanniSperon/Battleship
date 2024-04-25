import Data.GroupCreate;
import Data.Payload;
import com.sun.org.apache.bcel.internal.generic.Select;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.UUID;

public class CreateGroupController implements CustomController, Initializable {
    public Label errorIndicator;
    public TextField groupNameEntryField;
    public CheckBox privateCheckbox;
    public CheckBox allowInvitesCheckbox;
    public ListView<String> userSelector;
    private final HashMap<UUID, SelectedUserCell> selectedUsers = new HashMap<UUID, SelectedUserCell>();

    public void cancelButtonPressed(ActionEvent actionEvent) {
        GUIClient.primaryStage.setScene(GUIClient.viewMap.get("home").scene);
    }

    public class SelectedUserCell {
        public final BooleanProperty observable;
        private final UUID id;

        public SelectedUserCell(String item, UUID id) {
            this.observable = new SimpleBooleanProperty();
            this.id = id;
            this.observable.addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    selectedUsers.put(this.id, this);
                } else if (wasSelected) {
                    selectedUsers.remove(this.id);
                }
            });
        }
    }

    public void onNameEntryKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            createButtonPressed(new ActionEvent());
        }
    }

    public void privateCheckboxToggled(ActionEvent actionEvent) {
        boolean isPrivate = privateCheckbox.isSelected();
        if (isPrivate) {
            userSelector.setDisable(false);
            //allowInvitesCheckbox.setDisable(false);
        } else {
            userSelector.setDisable(true);
            //allowInvitesCheckbox.setDisable(true);
        }
    }

    public void allowInvitesCheckboxToggled(ActionEvent actionEvent) {
    }

    public void createButtonPressed(ActionEvent actionEvent) {
        String groupName = groupNameEntryField.getText();
        if (!groupName.isEmpty()) {
            String cleanedGroupName = groupName.trim();
            if (groupName.equals(cleanedGroupName)) {
                GUIClient.clientConnection.lastOperation = Payload.Type.GROUP_CREATE;
                GroupCreate d = new GroupCreate();
                d.creatorID = GUIClient.clientConnection.uuid;
                d.groupName = cleanedGroupName;
                d.members = new ArrayList<UUID>();
                selectedUsers.forEach((k, v) -> {
                    d.members.add(k);
                });
                boolean isPrivate = privateCheckbox.isSelected();
                if (isPrivate) {
                    d.isPrivate = true;
                    d.allowInvites = false;
                    //d.allowInvites = allowInvitesCheckbox.isSelected();
                } else {
                    d.isPrivate = false;
                    d.allowInvites = false;
                }
                GUIClient.clientConnection.send(new Packet(d));
            } else {
                errorIndicator.setText("Group name cannot have leading or trailing whitespace");
                errorIndicator.setVisible(true);
            }
        } else {
            errorIndicator.setText("Please enter a group name");
            errorIndicator.setVisible(true);
        }
    }

    public void onGroupCreateError() {
        System.out.println("Group create failed");
        errorIndicator.setText("Group name already taken");
        errorIndicator.setVisible(true);
    }

    public void onGroupCreateSuccess() {
        System.out.println("Group create success");
        errorIndicator.setVisible(false);
        groupNameEntryField.setText("");
        cancelButtonPressed(new ActionEvent());
    }

    public void onRefresh() {
        synchronized (GUIClient.clientConnection.dataManager) {
            userSelector.getItems().clear();
            GUIClient.clientConnection.dataManager.users.forEach((k, v) -> {
                if (v.username != null && !v.uuid.equals(GUIClient.clientConnection.uuid) && !v.username.equals("Server")) {
                    userSelector.getItems().add(v.username);
                }
            });
            ArrayList<UUID> idsToKeep = new ArrayList<UUID>();
            ArrayList<UUID> idsToRemove = new ArrayList<UUID>();
            selectedUsers.forEach((k, v) -> {
                if (GUIClient.clientConnection.dataManager.isValidUser(k)) {
                    idsToKeep.add(k);
                } else {
                    idsToRemove.add(k);
                }
            });

            for (UUID id : idsToRemove) {
                selectedUsers.remove(id);
            }
            for (UUID id : idsToKeep) {
                selectedUsers.get(id).observable.set(true);
            }
        }
    }

    @Override
    public void updateUI(GUICommand command) {
        switch (command.type) {
            case GROUP_CREATE_ERROR:
                onGroupCreateError();
                break;
            case GROUP_CREATE_SUCCESS:
                onGroupCreateSuccess();
                break;
            case REFRESH:
                onRefresh();
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
        GUIClient.viewMap.put("createGroup", new GUIView(null, this));
        userSelector.setCellFactory(CheckBoxListCell.forListView(new Callback<String, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(String item) {
                SelectedUserCell cell = new SelectedUserCell(item, GUIClient.clientConnection.dataManager.getByUsername(item));
                return cell.observable;
            }
        }));
    }
}
