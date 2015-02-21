package DesktopClient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            Alert wrongInputAlert = new Alert(Alert.AlertType.ERROR);
            wrongInputAlert.setTitle("Error");
            wrongInputAlert.setHeaderText("Unexpected error");
            wrongInputAlert.setContentText(e.toString());

            wrongInputAlert.showAndWait();
            Platform.exit();
        });

        primaryStage.setTitle("VK Spider");

        VKLoginPage vkLoginPage = new VKLoginPage();
        GridPane rootGridPane = new GridPane();
        rootGridPane.setMinSize(600, 800);
        rootGridPane.setAlignment(Pos.CENTER);
        rootGridPane.setHgap(10.0);
        rootGridPane.setVgap(10.0);
        rootGridPane.setPadding(new Insets(25.0, 25.0, 25.0, 25.0));
        rootGridPane.add(vkLoginPage, 0, 0);
        Scene scene = new Scene(rootGridPane);
        primaryStage.setResizable(false);

        vkLoginPage.addEventHandler(VKLoginPage.VKLoginSuccessEvent.VK_LOGIN_SUCCESS, event -> {
            SpyingUserPickerPage spyingUserPickerPage = new SpyingUserPickerPage(event.getAccessToken());
            spyingUserPickerPage.addEventHandler(SpyingUserPickerPage.SpyingUserPickEvent.SPYING_USER_PICK_EVENT,
                    event1 -> {
                        SearchAdjustmentPage searchAdjustmentPage = new SearchAdjustmentPage(event1.getSpyingId(),
                                event1.getAccessToken());
                        searchAdjustmentPage.addEventHandler(
                                SearchAdjustmentPage.SearchAdjustmentFinishEvent.SEARCH_ADJUSTMENT_FINISH_EVENT,
                                event2 -> {
                                    SearchPage searchPage = new SearchPage(event2.getSpyingId(), event2.getAccessToken(),
                                            event2.getChosenUsersList(), event2.getChosenGroupsList(),
                                            event2.getMaxUserPostsCount(), event2.getMaxUserCommentsCount(),
                                            event2.getMaxGroupPostsCount(), event2.getMaxGroupCommentsCount());
                                    rootGridPane.getChildren().clear();
                                    rootGridPane.add(searchPage, 0, 0);
                                });
                        rootGridPane.getChildren().clear();
                        rootGridPane.add(searchAdjustmentPage, 0, 0);
                    });
            rootGridPane.getChildren().clear();
            rootGridPane.add(spyingUserPickerPage, 0, 0);
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
