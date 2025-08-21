package com.project.dosmart;

import com.project.dosmart.controllers.AuthController;
import com.project.dosmart.controllers.MainController;
import com.project.dosmart.services.PinCodeService;
import com.project.dosmart.services.TodoService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        PinCodeService pinCodeService = new PinCodeService();

        if (pinCodeService.hasPinCode()) {
            if (!showAuthDialog(pinCodeService)) {
                return;
            }
        }

        TodoService todoService = new TodoService();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = fxmlLoader.load();
        MainController mainController = fxmlLoader.getController();
        mainController.setTodoService(todoService);
        mainController.setPinCodeService(pinCodeService);
        mainController.setPrimaryStage(primaryStage);
        primaryStage.setTitle("DoSmart");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);

        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Підтвердження");
            alert.setHeaderText("Ви впевнені, що хочете вийти?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() != ButtonType.OK) {
                event.consume();
            }
        });

        primaryStage.show();
    }

    private boolean showAuthDialog(PinCodeService pinCodeService) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("auth.fxml"));
        Parent root = fxmlLoader.load();
        AuthController authController = fxmlLoader.getController();
        Stage authStage = new Stage();
        authController.setStage(authStage);
        authController.setPinCodeService(pinCodeService);
        authStage.setTitle("Авторизація");
        authStage.setScene(new Scene(root, 700, 700));
        authStage.initModality(Modality.APPLICATION_MODAL);
        authStage.setResizable(true);
        authStage.showAndWait();

        return authController.isAuthenticated();
    }
}