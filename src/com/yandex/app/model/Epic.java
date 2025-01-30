package com.yandex.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIDs = new ArrayList<>();
    private LocalDateTime endTime = LocalDateTime.MIN;

    public Epic(String name, String description, int id) {
        super(name, description, id, -1, "01.01.0001; 00:00");
    }

    public ArrayList<Integer> getSubtasksIDs() {
        return subtaskIDs;
    }

    public void saveNewSubtaskIDs(Integer idOfNewSubtask) {
        this.subtaskIDs.add(idOfNewSubtask);
    }

    public void setEndTime(String endTime) {
        this.endTime = LocalDateTime.parse(endTime, FORMAT_DATE);
    }

    public String getEndTimeForEpic() {
        return this.endTime.format(FORMAT_DATE);
    }

    @Override
    public String toString() {
        return "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * \n" +
                "Эпик '" + this.getName() + "'" +
                ", \nописание: '" + this.getDescription() + "'" +
                ", \nid=" + this.getId() +
                ", \nстатус прогресса всего эпика: " + this.getStatus() +
                ", \nначало выполнения эпика: " + this.getStartTime() +
                ", \nпродолжительность: " + this.getDuration() + "мин." +
                ", \nконец выполнения эпика: " + this.getEndTimeForEpic() + "\n";
    }
}