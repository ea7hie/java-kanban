package com.yandex.app.model;

public class Subtask extends Task {
    private int idOfSubtaskEpic;

    public Subtask(String name, String description, int id, int idOfSubtaskEpic, int duration, String startTime) {
        super(name, description, id, duration, startTime);
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
        return " '" + this.getName() + "'" + ", описание: " + this.getDescription() + ", id=" + this.getId()
                + ", статус прогресса этой подзадачи: " + this.getStatus() +
                ", \nначало выполнения этой подзадачи: " + getStartTime() +
                ", \nпродолжительность: " + getDuration() + "мин." +
                ", \nконец выполнения этой подзадачи: " + getEndTime() + "\n";
    }
}