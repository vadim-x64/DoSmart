package com.project.dosmart.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class Todo {

    private String _name;
    private String _description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate _deadlineDate;

    public Todo() {}

    public Todo(String name, String description, LocalDate deadlineDate) {
        _name = name;
        _description = description;
        _deadlineDate = deadlineDate;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public LocalDate getDeadlineDate() {
        return _deadlineDate;
    }

    public void setDeadlineDate(LocalDate deadlineDate) {
        _deadlineDate = deadlineDate;
    }

    @Override
    public String toString() {
        String deadlineString = _deadlineDate != null ? " (дедлайн: " + _deadlineDate + ")" : "";
        return _name + deadlineString;
    }
}