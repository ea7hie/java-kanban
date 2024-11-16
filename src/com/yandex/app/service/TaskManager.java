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
    private final HashMap<Integer, Subtask> allSubtasks;
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

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(allSubtasks.values());
    }

    public int getIdOfNewTask() {
        return idOfNewTask++;
    }

    public Epic getOneEpicByID(int idForViewEpic) {
        return allEpics.get(idForViewEpic);
    }
//удалить что-то
    public void removeAllTasks() {
        allTasks.clear();
        System.out.println("Список задач очищен.");
    }

    public void removeAllEpics() {
        allEpics.clear();
        removeAllSubtasks();
        System.out.println("Список эпиков очищен.");
    }

    public void removeAllSubtasks() {
        allSubtasks.clear();
    }

    public String deleteOneElementByID(int idForDelete) {
        if (allTasks.containsKey(idForDelete)) {
            allTasks.remove(idForDelete);
        } else if (allEpics.containsKey(idForDelete)) {
            allEpics.remove(idForDelete);
            for (Subtask subtask : allSubtasks.values()) {
                if (subtask.getIdOfSubtaskEpic() == idForDelete) {
                    allSubtasks.remove(subtask.getId());
                }
            }
        } else {
            int idOfEpic = allSubtasks.get(idForDelete).getIdOfSubtaskEpic();
            allEpics.get(idOfEpic).getSubtasksIDs().remove(idForDelete);
            allSubtasks.remove(idForDelete);
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

    public void saveNewSubtask(Subtask subtask) {
        allSubtasks.put(subtask.getId(), subtask);
    }
//найти что-то
    public Task findTaskByID(int idForSearch) {
        return allTasks.get(idForSearch);
    }

    public Epic findEpicByID(int idForSearch) {
        return allEpics.get(idForSearch);
    }

    public Subtask findSubtaskByID(int idSubtask) {
       return allSubtasks.get(idSubtask);
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

    public boolean isSubtaskAddedByID(int id) {
        if (!allSubtasks.isEmpty()) {
            return allSubtasks.containsKey(id);
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

    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = allSubtasks.get(subtask.getId());
        oldSubtask.setName(subtask.getName());
        oldSubtask.setStatus(Progress.NEW);
        System.out.println("Успешно обновлено!");
    }
//проверка статуса эпика
    public Progress checkProgressStatusOfEpic(int idOfEpic) {
        int counterOfNEW = 0;
        int counterOfDONE = 0;

        Epic checkedEpic = allEpics.get(idOfEpic);
        for (Integer id : checkedEpic.getSubtasksIDs()) {
            Subtask currentCheckingSubtask = allSubtasks.get(id);
            if (currentCheckingSubtask.getStatus().equals(Progress.NEW)) {
                counterOfNEW++;
            } else if (currentCheckingSubtask.getStatus().equals(Progress.DONE)) {
                counterOfDONE++;
            }
        }

        if (counterOfNEW == checkedEpic.getSubtasksIDs().size()) {
            return Progress.NEW;
        } else if (counterOfDONE == checkedEpic.getSubtasksIDs().size()) {
            return Progress.DONE;
        }
        return Progress.IN_PROGRESS;
    }
}