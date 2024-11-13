package com.yandex.taskTrackerApp.model;

public class Subtask extends Task {
    private final int idOfSubtaskEpic;

    public Subtask(String name, Progress status, int idOfSubtaskEpic) {
        super(name, status);
        this.idOfSubtaskEpic = idOfSubtaskEpic;
    }

    @Override
    public String toString() {
        return this.getName() + "'" + ", статус прогресса этой подзадачи: " + this.getStatus() + "\n";
    }
}
