package com.yandex.taskTrackerApp.model;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Subtask> subtasks;

    public Epic(String name, String description, int id, Progress status, ArrayList<Subtask> subtasks) {
        super(name, description, id, status);
        this.subtasks = subtasks;
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public String toString() {
        String fullDescriptionAboutEpic = "Эпик '" + this.getName() + "'" +
                ", \nописание: '" + this.getDescription() + "'" +
                ", \nid=" + this.getId() +
                ", \nстатус прогресса всего эпика: " + this.getStatus() + "\n";

        for (int i = 0; i < subtasks.size(); i++) {
            fullDescriptionAboutEpic = fullDescriptionAboutEpic + "Подзадача № " + (i + 1) + " '"
                    + subtasks.get(i).toString();
        }

        return fullDescriptionAboutEpic;
    }

    public String showAllSubtasks() {
        String listSubtasks = "";
        for (int i = 0; i < subtasks.size(); i++) {
            listSubtasks = listSubtasks + "Подзадача № " + (i + 1) + " '"
                    + subtasks.get(i).toString();
        }

        return listSubtasks;
    }
}

