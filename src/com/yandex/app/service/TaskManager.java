package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.Progress;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> allTasks;
    private final HashMap<Integer, Epic> allEpics;
    private final HashMap<Integer, ArrayList<Subtask>> allSubtasks;
    private int idOfNewTask = 0;

    public TaskManager() {
        this.allTasks = new HashMap<>();
        this.allEpics = new HashMap<>();
        this.allSubtasks = new HashMap<>();
    }
//получить что-то (геттеры)
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(allEpics.values());
    }
//удалить что-то
    public void removeAllTasks() {
        allTasks.clear();
        System.out.println("Список задач очищен.");
    }

    public void removeAllEpics() {
        allEpics.clear();
        System.out.println("Список эпиков очищен.");
    }

    public String deleteOneElementByID(int idForDelete) {
        if (allTasks.containsKey(idForDelete)) {
            allTasks.remove(idForDelete);
        } else {
            allEpics.remove(idForDelete);
        }
        return "Выполнено успешно";
    }
//сохранить что-то
    public void saveNewTask(Task task) {
        allTasks.put(task.getId(), task);
    }

    public void saveNewEpic(Epic epic) {
        allEpics.put(epic.getId(), epic);
    }

    public void saveNewSubtask(int idOfNewSubtask, ArrayList<Subtask> subtasks) {
        allSubtasks.put(idOfNewSubtask, subtasks);
    }
//найти что-то
    public Task findTaskByID(int idForSearch) {
        return allTasks.get(idForSearch);
    }

    public Epic findEpicByID(int idForSearch) {
        return allEpics.get(idForSearch);
    }

    public Subtask findSubtaskByID(int idSubtask, int idOfEpic) {
       for (Subtask subtask : allSubtasks.get(idOfEpic)) {
            if (subtask.getId() == idSubtask) {
                return subtask;
            }
       }
       return null;
    }

    public boolean isTaskAddedByID(int id) {
        if (!allTasks.isEmpty()) {
            return allTasks.containsKey(id);
        }
        return false;
    }

    public boolean isEpicAddedByID(int id) {
        if (!allEpics.isEmpty()) {
            return allEpics.containsKey(id);
        }
        return false;
    }
//обновить что-то
    public void updateTask(Task task) {
        allTasks.put(task.getId(), task);
        System.out.println("Успешно обновлено!");
    }

    public void updateEpic(Epic epic) {
        Epic oldEpic = allEpics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        System.out.println("Успешно обновлено!");
    }

    public ArrayList<Subtask> showOneEpicByID(int idForViewEpic) {
        return allSubtasks.get(idForViewEpic);
    }

    public Progress checkProgressStatusOfEpic(int idOfEpic) {
        int counterOfNEW = 0;
        int counterOfDONE = 0;

        for (Subtask subtask : allSubtasks.get(idOfEpic)) {
            if (subtask.getStatus().equals(Progress.NEW)) {
                counterOfNEW++;
            } else if (subtask.getStatus().equals(Progress.DONE)) {
                counterOfDONE++;
            }
        }

        if (counterOfNEW == allSubtasks.get(idOfEpic).size()) {
            return Progress.NEW;
        } else if (counterOfDONE == allSubtasks.get(idOfEpic).size()) {
            return Progress.DONE;
        } else {
            return Progress.IN_PROGRESS;
        }
    }

    public int generateNewID() {
        this.idOfNewTask++;
        return (this.idOfNewTask * 11 - 7);
    }
}