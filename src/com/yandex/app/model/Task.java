package com.yandex.app.model;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Integer id;
    private Progress status = Progress.NEW;
//конструкторы
    public Task(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public Task(String name, int id) {
        this.name = name;
        this.id = id;
    }
//геттеры
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Progress getStatus() {
        return status;
    }
//сеттеры
    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Progress status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }
//переопределения
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) && Objects.equals(description, task.description)
                && Objects.equals(id, task.id) && status == task.status;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        if (name != null) {
            hash += name.hashCode();
        }
        hash *= 31;

        if (description != null) {
            hash += description.hashCode();
        }
        hash *= 31;

        if (id != null) {
            hash += id.hashCode();
        }
        hash *= 31;

        if (status != null) {
            hash += status.hashCode();
        }
        hash *= 31;

        return hash;
    }

    @Override
    public String toString() {
        return "Задача '" + name + "'" +
                ", \nописание: '" + description + "'" +
                ", \nid=" + id +
                ", \nстатус прогресса: " + status + "\n" + "\n";
    }
}
