package com.yandex.app.model;

public class Subtask extends Task {
    private final int idOfSubtaskEpic;

    public Subtask(String name, int id, int idOfSubtaskEpic) {
        super(name, id);
        this.idOfSubtaskEpic = idOfSubtaskEpic;
    }

    public int getIdOfSubtaskEpic() {
        return idOfSubtaskEpic;
    }

    @Override
    public String toString() {
        return this.getName() + "'" +  ", статус прогресса этой подзадачи: " + this.getStatus() + "\n";
    }
}