package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    public static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy; HH:mm");

    private String name;
    private String description;
    private Integer id;
    private Progress status = Progress.NEW;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String name, String description, int id, int duration, String startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.duration = Duration.ofMinutes(duration);
        this.startTime = LocalDateTime.parse(startTime, FORMAT_DATE);
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

    public int getDuration() {
        return (int) duration.toMinutes();
    }

    public String getStartTime() {
        return startTime.format(FORMAT_DATE);
    }

    public String getEndTime() {
        return startTime.plus(duration).format(FORMAT_DATE);
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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDuration(int duration) {
        this.duration = Duration.ofMinutes(duration);
    }

    public void setStartTime(String startTime) {
        this.startTime = LocalDateTime.parse(startTime, FORMAT_DATE);
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
        return "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * \n" +
                "Задача '" + name + "'" +
                ", \nописание: '" + description + "'" +
                ", \nid=" + id +
                ", \nстатус прогресса: " + status +
                ", \nначало: " + getStartTime() +
                ", \nпродолжительность: " + duration.toMinutes() + "мин." +
                ", \nконец: " + getEndTime() + "\n" + "\n";
    }
}