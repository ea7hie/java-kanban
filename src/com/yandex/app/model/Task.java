package com.yandex.app.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    public static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy; HH:mm");
    public static final String NEW_LINE = System.lineSeparator();
    protected static final String HEADER_OF_TASK = "* * * * * * * * * * * * * * * * * * * * * * * * * * * ";
    protected static final String defaultStartTime = "01.01.0001; 00:00";
    protected static final int defaultDuration = -1;

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
        return String.format("%S%S%SЗадача '%s', %sописание: '%s', %sid=%d, %sстатус прогресса: %s, %s" +
                        "начало: %s, %sпродолжительность: %s мин, %sконец выполнения эпика: %s%s%s",
                HEADER_OF_TASK, HEADER_OF_TASK, NEW_LINE, this.getName(), NEW_LINE, this.getDescription(), NEW_LINE,
                this.getId(), NEW_LINE, this.getStatus(), NEW_LINE, this.getStartTime(), NEW_LINE, this.getDuration(),
                NEW_LINE, this.getEndTime(), NEW_LINE, NEW_LINE);
    }
}