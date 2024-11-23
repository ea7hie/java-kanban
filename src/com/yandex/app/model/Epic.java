package com.yandex.app.model;

import com.yandex.app.Main;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIDs = new ArrayList<>();

    public Epic(String name, String description, int id) {
        super(name, description, id);
    }

    public ArrayList<Integer> getSubtasksIDs() {
        return subtaskIDs;
    }

    public void saveNewSubtaskIDs(Integer idOfNewSubtask ) {
        if (Main.inMemoryTaskManager.isSubtaskAddedByID(idOfNewSubtask)) {
            this.subtaskIDs.add(idOfNewSubtask);
        }
    }

    @Override
    public String toString() {
        String fullDescriptionAboutEpic = "Эпик '" + this.getName() + "'" +
                ", \nописание: '" + this.getDescription() + "'" +
                ", \nid=" + this.getId() +
                ", \nстатус прогресса всего эпика: " + this.getStatus() + "\n";

        for (int i = 0; i < subtaskIDs.size(); i++) {
            fullDescriptionAboutEpic = fullDescriptionAboutEpic + "Подзадача № " + (i + 1)
                    + Main.inMemoryTaskManager.findSubtaskByID(subtaskIDs.get(i));
        }
        return fullDescriptionAboutEpic + "\n";
    }
}