package com.project.dosmart.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.dosmart.models.Todo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TodoService {

    private static final String FILE_PATH = "todos.json";
    private final ObjectMapper _objectMapper;
    private final ObservableList<Todo> _todos;

    public TodoService() {
        _objectMapper = new ObjectMapper();
        _objectMapper.registerModule(new JavaTimeModule());
        _todos = FXCollections.observableArrayList();
        loadTodos();
    }

    public ObservableList<Todo> getTodos() {
        return _todos;
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

    private void loadTodos() {
        File file = new File(FILE_PATH);

        if (file.exists()) {
            try {
                List<Todo> todoList = _objectMapper.readValue(file,
                        _objectMapper
                                .getTypeFactory()
                                .constructCollectionType(List.class, Todo.class));
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
}