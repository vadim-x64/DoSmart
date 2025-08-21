package com.project.dosmart.controllers;

import com.project.dosmart.models.Todo;
import com.project.dosmart.services.TodoService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class MainController {

    @FXML
    private BorderPane _rootPane;
    @FXML
    private Button _addTodoButton;
    @FXML
    private Button _updateTodoButton;
    @FXML
    private Button _deleteTodoButton;
    @FXML
    private Button _deleteSelectedButton;
    @FXML
    private Button _deleteAllButton;
    @FXML
    private Button _refreshButton;
    @FXML
    private Button _exportButton;
    @FXML
    private Button _importButton;
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
        _updateTodoButton.setOnAction(e -> openUpdateTodoDialog());
        _deleteTodoButton.setOnAction(e -> deleteSingleTodo());
        _deleteSelectedButton.setOnAction(e -> deleteSelectedTodos());
        _deleteAllButton.setOnAction(e -> deleteAllTodos());
        _refreshButton.setOnAction(e -> refreshTodos());
        _exportButton.setOnAction(e -> exportTodos());
        _importButton.setOnAction(e -> importTodos());

        _todoList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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

    private void refreshTodos() {
        _todoService.refreshTodos();
        _description.clear();
        _todoList.getSelectionModel().clearSelection();
    }

    private void exportTodos() {
        _todoService.exportTodos(_primaryStage);
    }

    private void importTodos() {
        _todoService.importTodos(_primaryStage);
    }

    private void openUpdateTodoDialog() {
        int selectedIndex = _todoList.getSelectionModel().getSelectedIndex();
        Todo selectedTodo = _todoList.getSelectionModel().getSelectedItem();

        if (selectedTodo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Увага");
            alert.setHeaderText(null);
            alert.setContentText("Спочатку виберіть справу для оновлення!");
            alert.showAndWait();
            return;
        }

        openTodoDialog(selectedTodo, selectedIndex);
    }

    private void openTodoDialog(Todo todoToEdit, int editIndex) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/project/dosmart/addTodo.fxml"));
            Parent root = fxmlLoader.load();
            AddTodoController controller = fxmlLoader.getController();
            controller.setTodoService(_todoService);
            Stage stage = new Stage();
            controller.setStage(stage);

            if (todoToEdit != null) {
                controller.setTodoForEditing(todoToEdit, editIndex);
                stage.setTitle("Оновити справу");
            } else {
                stage.setTitle("Додати справу");
            }

            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(_primaryStage);
            stage.showAndWait();
        } catch (IOException ignored) {
        }
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

    private void deleteSingleTodo() {
        Todo selectedTodo = _todoList.getSelectionModel().getSelectedItem();
        int selectedIndex = _todoList.getSelectionModel().getSelectedIndex();

        if (selectedTodo == null) {
            showWarningAlert("Спочатку виберіть справу для видалення!");
            return;
        }

        if (showConfirmationAlert("Ви впевнені, що хочете видалити справу \"" + selectedTodo.getName() + "\"?")) {
            _todoService.deleteTodo(selectedIndex);
            _description.clear();
        }
    }

    private void deleteSelectedTodos() {
        ObservableList<Integer> selectedIndices = _todoList.getSelectionModel().getSelectedIndices();

        if (selectedIndices.isEmpty()) {
            showWarningAlert("Спочатку виберіть справи для видалення!");
            return;
        }

        if (showConfirmationAlert("Ви впевнені, що хочете видалити " + selectedIndices.size() + " справ(и)?")) {
            _todoService.deleteSelectedTodos(new ArrayList<>(selectedIndices));
            _description.clear();
        }
    }

    private void deleteAllTodos() {
        if (_todoService.getTodos().isEmpty()) {
            showWarningAlert("Немає справ для видалення!");
            return;
        }

        if (showConfirmationAlert("Ви впевнені, що хочете видалити ВСІ справи? Цю дію не можна скасувати!")) {
            _todoService.deleteAllTodos();
            _description.clear();
        }
    }

    private void showWarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Увага");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}