package com.project.dosmart.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.dosmart.models.Todo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TodoService {

    private static final String APP_DIR = ".dosmart";
    private static final String FILE_NAME = "tasks.json";
    private final String FILE_PATH;
    private final ObjectMapper _objectMapper;
    private final ObservableList<Todo> _todos;
    private FilteredList<Todo> _filteredTodos;

    public TodoService() {
        _objectMapper = new ObjectMapper();
        _objectMapper.registerModule(new JavaTimeModule());
        _todos = FXCollections.observableArrayList();
        _filteredTodos = new FilteredList<>(_todos);
        String userHome = System.getProperty("user.home");
        Path appDirPath = Paths.get(userHome, APP_DIR);
        FILE_PATH = Paths.get(userHome, APP_DIR, FILE_NAME).toString();

        try {
            Files.createDirectories(appDirPath);
        } catch (IOException ignored) {
        }

        loadTodos();
    }

    public ObservableList<Todo> getTodos() {
        return _filteredTodos;
    }

    public void addTodo(Todo todo) {
        _todos.add(todo);
        saveTodo();
    }

    public void updateTodo(int index, Todo updatedTodo) {
        if (index >= 0 && index < _todos.size()) {
            _todos.set(index, updatedTodo);
            saveTodo();
        }
    }

    public void refreshTodos() {
        _todos.clear();
        loadTodos();
    }

    private void loadTodos() {
        File file = new File(FILE_PATH);

        if (file.exists()) {
            try {
                List<Todo> todoList = _objectMapper.readValue(file, _objectMapper.getTypeFactory().constructCollectionType(List.class, Todo.class));
                _todos.addAll(todoList);
            } catch (IOException ignored) {
            }
        }
    }

    private void saveTodo() {
        try {
            _objectMapper.writeValue(new File(FILE_PATH), new ArrayList<>(_todos));
        } catch (IOException ignored) {
        }
    }

    public void deleteTodo(int index) {
        if (index >= 0 && index < _todos.size()) {
            _todos.remove(index);
            saveTodo();
        }
    }

    public void deleteSelectedTodos(List<Integer> indexes) {
        indexes.stream().sorted((a, b) -> Integer.compare(b, a)).forEach(index -> {
                    if (index >= 0 &&  index < _todos.size()) {
                        _todos.remove((int) index);
                    }
                });
        saveTodo();
    }

    public void deleteAllTodos() {
        _todos.clear();
        saveTodo();
    }

    public void exportTodos(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Експорт");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON файли", "*.json"));
        fileChooser.setInitialFileName("export.json");
        File file = fileChooser.showSaveDialog(ownerStage);

        if (file != null) {
            try {
                _objectMapper.writeValue(file, new ArrayList<>(_todos));
            } catch (IOException ignored) {
            }
        }
    }

    public void importTodos(Stage ownerStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Імпорт");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON файли", "*.json"));
        File file = fileChooser.showOpenDialog(ownerStage);

        if (file != null && file.exists()) {
            try {
                List<Todo> importedTodos = _objectMapper.readValue(file, _objectMapper.getTypeFactory().constructCollectionType(List.class, Todo.class));
                _todos.addAll(importedTodos);
                saveTodo();
            } catch (IOException ignored) {
            }
        }
    }

    public void filterTodos(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            _filteredTodos.setPredicate(null);
        } else {
            String lowerCaseFilter = searchText.toLowerCase().trim();
            _filteredTodos.setPredicate(todo -> {
                if (todo.getName() != null && todo.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (todo.getDescription() != null && todo.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        }
    }
}