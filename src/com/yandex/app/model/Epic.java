package com.yandex.app.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIDs = new ArrayList<>();
    private final ArrayList<Subtask> allSubtasksOfEpic = new ArrayList<>();

    public Epic(String name, String description, int id) {
        super(name, description, id);
    }

    public List<Integer> getSubtasksIDs() {
        return subtaskIDs;
    }

    public void saveNewSubtaskIDs(Integer idOfNewSubtask) {
        this.subtaskIDs.add(idOfNewSubtask);
    }

    public void addNewSubtaskInallSubtasksOfEpic(Subtask newSubtask) {
        allSubtasksOfEpic.add(newSubtask);
    }

    @Override
    public String toString() {
        String fullDescriptionAboutEpic = "Эпик '" + this.getName() + "'" +
                ", \nописание: '" + this.getDescription() + "'" +
                ", \nid=" + this.getId() +
                ", \nстатус прогресса всего эпика: " + this.getStatus() + "\n";

        for (int i = 0; i < subtaskIDs.size(); i++) {
            fullDescriptionAboutEpic = fullDescriptionAboutEpic + "Подзадача № " + (i + 1)
                    + allSubtasksOfEpic.get(i);
        }
        return fullDescriptionAboutEpic + "\n";
    }
}