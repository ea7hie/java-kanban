package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.model.Progress;
import com.yandex.app.service.interfaces.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> allTasks;
    private final HashMap<Integer, Epic> allEpics;
    private final HashMap<Integer, Subtask> allSubtasks;
    private int idOfNewTask = 0;

    public InMemoryTaskManager() {
        this.allTasks = new HashMap<>();
        this.allEpics = new HashMap<>();
        this.allSubtasks = new HashMap<>();
    }
//получить что-то (геттеры)
    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(allEpics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(allSubtasks.values());
    }

    public ArrayList<Integer> getIdsOfAllSubtasks() {
        ArrayList<Integer> idsOfAllSubtasks = new ArrayList<>();
        for (Subtask subtask : getAllSubtasks()) {
            idsOfAllSubtasks.add(subtask.getId());
        }
        return idsOfAllSubtasks;
    }

    private int getIdOfNewTask() {
        return ++idOfNewTask;
    }

    public ArrayList<Subtask> getAllSubtasksOfEpicById(int idOfEpic) {
        ArrayList<Subtask> allSubtasksInEpic = new ArrayList<>();
        for (int subtaskID : allEpics.get(idOfEpic).getSubtasksIDs()) {
            allSubtasksInEpic.add(allSubtasks.get(subtaskID));
        }
        InMemoryHistoryManager.add(allEpics.get(idOfEpic));
        return allSubtasksInEpic;
    }
//удалить что-то
    @Override
    public void removeAllTasks() {
        allTasks.clear();
        InMemoryHistoryManager.removeAllTasksInViewedTasks();
        System.out.println("Список задач очищен.");
    }

    @Override
    public void removeAllEpics() {
        allEpics.clear();
        removeAllSubtasks();
        InMemoryHistoryManager.removeAllEpicsInViewedTasks();
        System.out.println("Список эпиков очищен.");
    }

    @Override
    public void removeAllSubtasks() {
        for (Epic epic : allEpics.values()) {
            epic.getSubtasksIDs().clear();
            epic.setStatus(Progress.NEW);
        }
        allSubtasks.clear();
    }

    @Override
    public String deleteOneTaskByID(int idForDelete) {
        allTasks.remove(idForDelete);
        InMemoryHistoryManager.removeOneElem(idForDelete);
        return "Выполнено успешно";
    }

    @Override
    public String deleteOneEpicByID(int idForDelete) {
        for (int index  : allEpics.get(idForDelete).getSubtasksIDs()) {
            allSubtasks.remove(index);
        }
        allEpics.remove(idForDelete);
        return "Выполнено успешно";
    }

    @Override
    public String deleteOneSubtaskaskByID(int idForDelete){
        int idOfEpic = allSubtasks.get(idForDelete).getIdOfSubtaskEpic();
        allEpics.get(idOfEpic).getSubtasksIDs().remove(Integer.valueOf(idForDelete));
        allSubtasks.remove(idForDelete);
        checkProgressStatusOfEpic(idOfEpic);
        return "Выполнено успешно";
    }
//сохранить что-то
    @Override
    public void saveNewTask(Task task) {
        task.setId(getIdOfNewTask());
        allTasks.put(task.getId(), task);
    }

    @Override
    public void saveNewEpic(Epic epic) {
        epic.setId(getIdOfNewTask());
        if (epic.getSubtasksIDs() != null) {
            for (int subtaskID : epic.getSubtasksIDs()) {
                allSubtasks.get(subtaskID).setIdOfSubtaskEpic(epic.getId());
            }
        }
        allEpics.put(epic.getId(), epic);
    }

    @Override
    public void saveNewSubtask(Subtask subtask) {
        subtask.setId(getIdOfNewTask());
        allSubtasks.put(subtask.getId(), subtask);
        allEpics.get(subtask.getIdOfSubtaskEpic()).saveNewSubtaskIDs(subtask.getId());
        allEpics.get(subtask.getIdOfSubtaskEpic()).addNewSubtaskInallSubtasksOfEpic(subtask);
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
    }
//найти что-то
    @Override
    public Task findTaskByID(int idForSearch) {
        InMemoryHistoryManager.add(allTasks.get(idForSearch));
        return allTasks.get(idForSearch);
    }

    @Override
    public Epic findEpicByID(int idForSearch) {
        InMemoryHistoryManager.add(allEpics.get(idForSearch));
        return allEpics.get(idForSearch);
    }

    @Override
    public Subtask findSubtaskByID(int idSubtask) {
        InMemoryHistoryManager.add(allSubtasks.get(idSubtask));
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
    @Override
    public void updateTask(Task task) {
        allTasks.put(task.getId(), task);
        System.out.println("Успешно обновлено!\n");
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = allEpics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        System.out.println("Успешно обновлено!\n");
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = allSubtasks.get(subtask.getId());
        oldSubtask.setName(subtask.getName());
        oldSubtask.setDescription(subtask.getDescription());
        oldSubtask.setStatus(subtask.getStatus());
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
        System.out.println("Успешно обновлено!\n");
    }
//проверка статуса эпика
    private void checkProgressStatusOfEpic(int idOfEpic) {
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
            checkedEpic.setStatus(Progress.NEW);
        } else if (counterOfDONE == checkedEpic.getSubtasksIDs().size()) {
            checkedEpic.setStatus(Progress.DONE);
        } else {
            checkedEpic.setStatus(Progress.IN_PROGRESS);
        }
    }
}