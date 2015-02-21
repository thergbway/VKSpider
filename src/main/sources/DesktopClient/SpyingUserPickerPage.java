package DesktopClient;

import VKSDK.DataTypes.User;
import VKSDK.Exceptions.VKException;
import VKSDK.Methods.FriendsGet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.controlsfx.dialog.ProgressDialog;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

public class SpyingUserPickerPage extends Region {
    private final String accessToken;

    public SpyingUserPickerPage(String accessToken) {
        this.accessToken = accessToken;

        setBorder(new Border(new BorderStroke(null,BorderStrokeStyle.SOLID,null,null, new Insets(-5.0))));

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER_LEFT);
        gridPane.setHgap(10.0);
        gridPane.setVgap(10.0);
        gridPane.setPadding(new Insets(25.0, 25.0, 25.0, 25.0));

        Text aboutTaskText = new Text("Pick the user to check or type user`s id:");
        aboutTaskText.setFont(Font.font(null, FontWeight.NORMAL, 20.0));
        gridPane.add(aboutTaskText, 0, 0, 2, 1);

        Text userOrIdText = new Text("type custom id or choose");
        HBox hbText = new HBox(10.0);
        hbText.setAlignment(Pos.CENTER_RIGHT);
        hbText.getChildren().add(userOrIdText);
        gridPane.add(hbText, 0, 1);

        ComboBox idsComboBox = new ComboBox<>();
        idsComboBox.setEditable(true);
        idsComboBox.setPromptText("user`s id");
        HBox hbBtn = new HBox(10.0);
        hbBtn.setAlignment(Pos.CENTER_RIGHT);
        hbBtn.getChildren().add(idsComboBox);
        gridPane.add(hbBtn, 1, 1);

        Button btn = new Button("Next");
        HBox hbBtn1 = new HBox(10.0);
        hbBtn1.setAlignment(Pos.CENTER_RIGHT);
        hbBtn1.getChildren().add(btn);
        gridPane.add(hbBtn1, 1, 2);

        LinkedList<User> friends = new LinkedList<>();
        btn.setOnAction(event -> {
            String value = (String) idsComboBox.getValue();
            Long chosenId = null;
            boolean isRightInput = false;
            try {
                chosenId = Long.parseLong(value);
                isRightInput = true;
            } catch (NumberFormatException e) {
                for (User friend : friends) {
                    String currentString = friend.getFirstName() + " " + friend.getLastName();
                    if (value != null && value.equals(currentString)) {
                        chosenId = friend.getId();
                        isRightInput = true;
                        break;
                    }
                }
            }

            if (!isRightInput) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid id");
                alert.showAndWait();
                return;
            } else {
                fireEvent(new SpyingUserPickEvent(chosenId, accessToken));
            }
        });

        Task<Object> worker = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                FriendsGet friendsGet = new FriendsGet(accessToken);

                try {
                    friends.addAll(friendsGet.invoke());
                } catch (VKException e) {
                    throw new RuntimeException(e);
                }
                ObservableList<String> items = FXCollections.observableArrayList();
                for (int i = 0; i < friends.size(); i++) {
                    User friend = friends.get(i);
                    Thread.sleep(3L);
                    updateProgress(i, friends.size());
                    items.add(friend.getFirstName() + " " + friend.getLastName());
                }
                Collections.sort(items);

                Platform.runLater(() -> {
                    idsComboBox.setItems(items);
                    getChildren().add(gridPane);
                });

                return null;
            }
        };
        ProgressDialog progressDialog = new ProgressDialog(worker);
        new Thread(worker).start();
    }

    public static class SpyingUserPickEvent extends Event {
        public static final EventType<SpyingUserPickEvent> SPYING_USER_PICK_EVENT =
                new EventType<>(ANY, "SPYING_USER_PICKED");
        private final Long spyingId;
        private final String accessToken;

        public SpyingUserPickEvent(Long spyingId, String accessToken) {
            super(SPYING_USER_PICK_EVENT);
            this.spyingId = spyingId;
            this.accessToken = accessToken;

        }

        public Long getSpyingId() {
            return spyingId;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
