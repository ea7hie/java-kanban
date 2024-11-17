package com.yandex.app.model;

public class Subtask extends Task {
    private int idOfSubtaskEpic;

    public Subtask(String name, int id, int idOfSubtaskEpic) {
        super(name, id);
        this.idOfSubtaskEpic = idOfSubtaskEpic;
    }

    public int getIdOfSubtaskEpic() {
        return idOfSubtaskEpic;
    }

    public void setIdOfSubtaskEpic(int idOfSubtaskEpic) {
        this.idOfSubtaskEpic = idOfSubtaskEpic;
    }

    @Override
    public String toString() {
        return this.getName() + "'" +  ", статус прогресса этой подзадачи: " + this.getStatus() + "\n";
    }
}