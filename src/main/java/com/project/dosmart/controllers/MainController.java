package com.project.dosmart.controllers;

import com.project.dosmart.models.Todo;
import com.project.dosmart.services.PinCodeService;
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
    private Button _pinCodeButton;
    @FXML
    private Button _infoButton;
    @FXML
    private ListView<Todo> _todoList;
    @FXML
    private TextArea _description;

    private TodoService _todoService;
    private Stage _primaryStage;
    private PinCodeService _pinCodeService;

    public void setTodoService(TodoService todoService) {
        _todoService = todoService;
        _todoList.setItems(_todoService.getTodos());
    }

    public void setPinCodeService(PinCodeService pinCodeService) {
        _pinCodeService = pinCodeService;
        updatePinButtonText();
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
        _pinCodeButton.setOnAction(e -> handlePinCodeAction());
        _infoButton.setOnAction(e -> showInfoDialog());
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

    private void showInfoDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/project/dosmart/about.fxml"));
            Parent root = fxmlLoader.load();
            AboutController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            stage.setResizable(true);
            stage.setTitle("Про програму");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.initOwner(_primaryStage);
            stage.show();
        } catch (IOException ignored) {
        }
    }

    private void updatePinButtonText() {
        if (_pinCodeService != null) {
            _pinCodeButton.setText(_pinCodeService.hasPinCode() ? "ЗНЯТИ ПІН-КОД" : "ВСТАНОВИТИ ПІН-КОД");
        }
    }

    private void handlePinCodeAction() {
        if (_pinCodeService.hasPinCode()) {
            openPinSetupDialog(true);
        } else {
            openPinSetupDialog(false);
        }
    }

    private void openPinSetupDialog(boolean removing) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/project/dosmart/pinSetup.fxml"));
            Parent root = fxmlLoader.load();
            PinSetupController controller = fxmlLoader.getController();
            controller.setPinCodeService(_pinCodeService);
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setRemovingMode(removing);
            stage.setResizable(true);
            stage.setTitle(removing ? "Зняти пін-код" : "Встановити пін-код");
            stage.setScene(new Scene(root, 700, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(_primaryStage);
            stage.showAndWait();
            updatePinButtonText();
        } catch (IOException ignored) {
        }
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
            alert.setTitle("Попередження");
            alert.setHeaderText(null);
            alert.setContentText("Спочатку виберіть елемент для оновлення!");
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
            stage.setResizable(true);

            if (todoToEdit != null) {
                controller.setTodoForEditing(todoToEdit, editIndex);
                stage.setTitle("Оновити завдання");
            } else {
                stage.setTitle("Додати завдання");
            }

            stage.setScene(new Scene(root, 700, 700));
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
            stage.setResizable(true);
            stage.setTitle("Додати завдання");
            stage.setScene(new Scene(root, 700, 700));
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
            showWarningAlert("Спочатку виберіть елемент для видалення!");
            return;
        }

        if (showConfirmationAlert("Ви впевнені, що хочете видалити \"" + selectedTodo.getName() + "\"?")) {
            _todoService.deleteTodo(selectedIndex);
            _description.clear();
        }
    }

    private void deleteSelectedTodos() {
        ObservableList<Integer> selectedIndices = _todoList.getSelectionModel().getSelectedIndices();

        if (selectedIndices.isEmpty()) {
            showWarningAlert("Спочатку виберіть елементи для видалення!");
            return;
        }

        if (showConfirmationAlert("Ви впевнені, що хочете видалити " + selectedIndices.size() + " елементів?")) {
            _todoService.deleteSelectedTodos(new ArrayList<>(selectedIndices));
            _description.clear();
        }
    }

    private void deleteAllTodos() {
        if (_todoService.getTodos().isEmpty()) {
            showWarningAlert("Немає елементів для видалення!");

            return;
        }

        if (showConfirmationAlert("Ви впевнені, що хочете очистити список? Всі завдання будуть видалені!")) {
            _todoService.deleteAllTodos();
            _description.clear();
        }
    }

    private void showWarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Попередження");
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