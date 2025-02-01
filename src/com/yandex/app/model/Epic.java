package com.yandex.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.yandex.app.model.Subtask.FOOTER_OF_SUBTASK;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIDs = new ArrayList<>();
    private LocalDateTime endTime = LocalDateTime.MIN;

    public Epic(String name, String description, int id) {
        super(name, description, id, Task.defaultDuration, Task.defaultStartTime);
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
        return String.format("%S%S%SЭпик '%s', %sописание: '%s', %sid=%d, %sстатус прогресса всего эпика: %s, %s" +
                        "начало выполнения эпика: %s, %sпродолжительность: %s мин, %sконец выполнения эпика: %s%s%s%s",
                HEADER_OF_TASK, HEADER_OF_TASK, NEW_LINE, this.getName(), NEW_LINE, this.getDescription(), NEW_LINE,
                this.getId(), NEW_LINE, this.getStatus(), NEW_LINE, this.getStartTime(), NEW_LINE, this.getDuration(),
                NEW_LINE, this.getEndTimeForEpic(), NEW_LINE, FOOTER_OF_SUBTASK, NEW_LINE);
    }
}