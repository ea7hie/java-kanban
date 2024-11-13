package com.yandex.taskTrackerApp.service;

import com.yandex.taskTrackerApp.model.Epic;
import com.yandex.taskTrackerApp.model.Subtask;
import com.yandex.taskTrackerApp.model.Task;
import com.yandex.taskTrackerApp.model.Progress;

import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> allTasks;
    private final HashMap<Integer, Epic> allEpics;

    public TaskManager() {
        this.allTasks = new HashMap<>();
        this.allEpics = new HashMap<>();
    }

    public HashMap<Integer, Task> getAllTasks() {
        return allTasks;
    }

    public HashMap<Integer, Epic> getAllEpics() {
        return allEpics;
    }

    public void saveNewTask(Task task) {
        allTasks.put(task.getId(), task);
    }

    public void saveNewEpic(Epic epic) {
        allEpics.put(epic.getId(), epic);
    }

    public String printAllTasks() {
        if (allTasks.isEmpty()) {
           return "Список задач пуст.";
        } else {
            String messageWithAllTasks = "";
            for (Task task : allTasks.values()) {
                messageWithAllTasks = messageWithAllTasks + task.toString();
            }
            return messageWithAllTasks;
        }
    }

    public String printAllEpics() {
        if (allEpics.isEmpty()) {
            return "Список задач пуст.";
        } else {
            String messageWithAllEpics = "";
            for (Epic epic : allEpics.values()) {
                messageWithAllEpics = messageWithAllEpics + epic.toString();
            }
            return messageWithAllEpics;
        }
    }

    public void removeAllTasks() {
        allTasks.clear();
        System.out.println("Список задач очищен.");
    }

    public void removeAllEpics() {
        allEpics.clear();
        System.out.println("Список эпиков очищен.");
    }

    //отличие от isTaskAddedByID: возращает более точную инфу, почему элемент не найден
    public String findTaskByID(int id) {
        if (!allTasks.isEmpty()) {
            if (allTasks.containsKey(id)) {
                return allTasks.get(id).toString();
            }
            return "Задачи с таким id нет. Проверьте ввод или попробуйте запустить поиск по этому id среди эпиков.";
        }
        return "Список задач пуст.";
    }

    public String findEpicByID(int id) {
        if (!allEpics.isEmpty()) {
            if (allEpics.containsKey(id)) {
                return allEpics.get(id).toString();
            }
            return "Эпика с таким id нет. Проверьте ввод или попробуйте запустить поиск по этому id среди задач.";
        }
        return "Список эпиков пуст.";
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

    public void updateTask(Task task) {
        allTasks.put(task.getId(), task);
        System.out.println("Успешно обновлено!");
    }

    public void updateEpic(Epic epic) {
        allEpics.put(epic.getId(), epic);
        System.out.println("Успешно обновлено!");
    }

    public String deleteOneElementByID(int idForDelete, boolean isTask) {
        if (isTask) {
            allTasks.remove(idForDelete);
        } else {
            allEpics.remove(idForDelete);
        }
        return "Выполнено успешно";
    }

    public String showOneEpicByID(int idFotViewEpic) {
        return allEpics.get(idFotViewEpic).showAllSubtasks();
    }

    public Progress checkProgressStatusOfEpic(Epic epic) {
        int counter = 0;
        for (Subtask subtask : epic.getSubtasks()) {
            if (subtask.getStatus().equals(Progress.NEW)) {
                counter++;
            }
        }
        if (counter == epic.getSubtasks().size()) {
            return Progress.NEW;
        }

        counter = 0;
        for (Subtask subtask : epic.getSubtasks()) {
            if (subtask.getStatus().equals(Progress.DONE)) {
                counter++;
            }
        }
        if (counter == epic.getSubtasks().size()) {
            return Progress.DONE;
        }

        return Progress.IN_PROGRESS;
    }
}