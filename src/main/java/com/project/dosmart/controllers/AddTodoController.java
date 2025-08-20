package com.project.dosmart.controllers;

import com.project.dosmart.models.Todo;
import com.project.dosmart.services.TodoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import javafx.stage.Stage;

public class AddTodoController {

    @FXML
    private TextField _name;
    @FXML
    private TextArea _description;
    @FXML
    private DatePicker _deadlineDate;
    @FXML
    private Button _addButton;

    private TodoService _todoService;
    private Stage _stage;
    private boolean _isEditing = false;
    private int _editIndex = -1;

    public void setTodoService(TodoService todoService) {
        _todoService = todoService;
    }

    public void setStage(Stage stage) {
        _stage = stage;
    }

    public void setTodoForEditing(Todo todo, int index) {
        _isEditing = true;
        _editIndex = index;
        _name.setText(todo.getName());
        _description.setText(todo.getDescription());
        _deadlineDate.setValue(todo.getDeadlineDate());
        _addButton.setText("Оновити");
    }

    @FXML
    private void initialize() {
        _addButton.setOnAction(e -> saveTodo());
    }

    private void saveTodo() {
        String name = _name.getText();
        String description = _description.getText();
        LocalDate deadlineDate = _deadlineDate.getValue();

        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка");
            alert.setHeaderText(null);
            alert.setContentText("Назва справи є обов'язковою!");
            alert.showAndWait();
            return;
        }

        Todo todo = new Todo(name, description, deadlineDate);

        if (_isEditing) {
            _todoService.updateTodo(_editIndex, todo);
        } else {
            _todoService.addTodo(todo);
        }

        _stage.close();
    }
}