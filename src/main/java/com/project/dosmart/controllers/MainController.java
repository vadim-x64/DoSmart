package com.project.dosmart.controllers;

import com.project.dosmart.models.Todo;
import com.project.dosmart.services.TodoService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane _rootPane;
    @FXML
    private Button _addTodoButton;
    @FXML
    private ListView<Todo> _todoList;
    @FXML
    private TextArea _description;

    private TodoService _todoService;
    private Stage _primaryStage;

    public void setTodoService(TodoService todoService) {
        _todoService = todoService;
        _todoList.setItems(_todoService.getTodos());
    }

    public void setPrimaryStage(Stage primaryStage) {
        _primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        _addTodoButton.setOnAction(e -> openAddTodoDialog());

        _todoList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                _description.setText(newValue.getDescription());
            } else {
                _description.clear();
            }
        });

        _rootPane.setOnMouseClicked(event -> {
            if (event.getTarget().equals(_rootPane) || !(event.getTarget() instanceof Parent)) {
                _todoList.getSelectionModel().clearSelection();
            }
        });
    }

    private void openAddTodoDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/project/dosmart/addTodo.fxml"));
            Parent root = fxmlLoader.load();

            AddTodoController addTodoController = fxmlLoader.getController();
            addTodoController.setTodoService(_todoService);

            Stage stage = new Stage();
            addTodoController.setStage(stage);
            stage.setTitle("Додати справу");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(_primaryStage);
            stage.showAndWait();
        } catch (IOException ignored) {
        }
    }
}