package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.interfaces.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private final InMemoryHistoryManager inMemoryHistoryManager = (InMemoryHistoryManager) Managers.getDefaultHistory();
    private HashMap<Integer, Task> allTasks;
    private HashMap<Integer, Epic> allEpics;
    private HashMap<Integer, Subtask> allSubtasks;
    private final ArrayList<Integer> allIDs = new ArrayList<>();
    private int idOfNewTask = 0;

    public InMemoryTaskManager() {
        this.allTasks = new HashMap<>();
        this.allEpics = new HashMap<>();
        this.allSubtasks = new HashMap<>();
    }

    void countAllIDs() {
        allIDs.clear();
        allIDs.addAll(allTasks.keySet());
        allIDs.addAll(allEpics.keySet());
        allIDs.addAll(allSubtasks.keySet());
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

    private int getIdOfNewTask() {
        countAllIDs();
        do {
            ++idOfNewTask;
        } while (allIDs.contains(idOfNewTask));
        return idOfNewTask;
    }

    private ArrayList<Subtask> getAllSubtasksOfEpicById(int idOfEpic) {
        ArrayList<Subtask> allSubtasksInEpic = new ArrayList<>();
        for (int subtaskID : allEpics.get(idOfEpic).getSubtasksIDs()) {
            allSubtasksInEpic.add(allSubtasks.get(subtaskID));
        }
        return allSubtasksInEpic;
    }

    public String getFullDescOfAllSubtasksOfEpicById(int idOfEpic) {
        ArrayList<Subtask> allSubtasksInEpic = this.getAllSubtasksOfEpicById(idOfEpic);
        String fullDescription = "";
        for (int i = 0; i < allSubtasksInEpic.size(); i++) {
            Subtask currentSubtask = allSubtasksInEpic.get(i);
            fullDescription = fullDescription + "Подзадача №" + (i + 1) + currentSubtask.toString();
        }
        return fullDescription + "\n";
    }

    public String showListViewedTasks() {
        ArrayList<Task> viewedTasks = inMemoryHistoryManager.getHistory();
        if (viewedTasks.isEmpty()) {
            return "Пока что нет недавно просмотренных задач.\n";
        }

        String listViewedTasks = "";
        for (int i = 0; i < viewedTasks.size(); i++) {
            Task viewedTask = viewedTasks.get(i);
            listViewedTasks = listViewedTasks + (i + 1) + "-я просмотренная задача: \n" + viewedTask.toString();
            if (viewedTask instanceof Epic) {
                listViewedTasks = listViewedTasks + this.getFullDescOfAllSubtasksOfEpicById(viewedTask.getId());
            } else if (viewedTask instanceof Subtask) {
                listViewedTasks = listViewedTasks + "\n";
            }
        }
        return listViewedTasks + "\n";
    }

    public ArrayList<Task> getListOfHistory() {
        return inMemoryHistoryManager.getHistory();
    }

    //сеттеры
    public void setAllTasks(HashMap<Integer, Task> allTasks) {
        this.allTasks = allTasks;
    }

    public void setAllEpics(HashMap<Integer, Epic> allEpics) {
        this.allEpics = allEpics;
    }

    public void setAllSubtasks(HashMap<Integer, Subtask> allSubtasks) {
        this.allSubtasks = allSubtasks;
    }


    public void setIdOfNewTask(int idOfNewTask) {
        this.idOfNewTask = idOfNewTask;
    }

    //удалить что-то
    @Override
    public void removeAllTasks() {
        allTasks.clear();
        inMemoryHistoryManager.removeAllTasksInViewedTasks();
    }

    @Override
    public void removeAllEpics() {
        allEpics.clear();
        removeAllSubtasks();
        inMemoryHistoryManager.removeAllEpicsInViewedTasks();
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
    public void deleteOneTaskByID(int idForDelete) {
        inMemoryHistoryManager.remove(idForDelete);
        allTasks.remove(idForDelete);
    }

    @Override
    public void deleteOneEpicByID(int idForDelete) {
        for (int index : allEpics.get(idForDelete).getSubtasksIDs()) {
            inMemoryHistoryManager.remove(index);
            allSubtasks.remove(index);
        }
        inMemoryHistoryManager.remove(idForDelete);
        allEpics.remove(idForDelete);
    }

    @Override
    public void deleteOneSubtaskskByID(int idForDelete) {
        int idOfEpic = allSubtasks.get(idForDelete).getIdOfSubtaskEpic();
        inMemoryHistoryManager.remove(idForDelete);
        allEpics.get(idOfEpic).getSubtasksIDs().remove(Integer.valueOf(idForDelete));
        allSubtasks.remove(idForDelete);
        checkProgressStatusOfEpic(idOfEpic);
    }

    //сохранить что-то
    @Override
    public Task saveNewTask(Task task) {
        task.setId(getIdOfNewTask());
        allTasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic saveNewEpic(Epic epic) {
        epic.setId(getIdOfNewTask());
        if (epic.getSubtasksIDs() != null) {
            for (int subtaskID : epic.getSubtasksIDs()) {
                allSubtasks.get(subtaskID).setIdOfSubtaskEpic(epic.getId());
            }
        }
        allEpics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask saveNewSubtask(Subtask subtask) {
        subtask.setId(getIdOfNewTask());
        allSubtasks.put(subtask.getId(), subtask);
        allEpics.get(subtask.getIdOfSubtaskEpic()).saveNewSubtaskIDs(subtask.getId());
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
        return subtask;
    }

    //найти что-то
    @Override
    public Task findTaskByID(int idForSearch) {
        inMemoryHistoryManager.add(allTasks.get(idForSearch));
        return allTasks.get(idForSearch);
    }

    @Override
    public Epic findEpicByID(int idForSearch) {
        inMemoryHistoryManager.add(allEpics.get(idForSearch));
        return allEpics.get(idForSearch);
    }

    @Override
    public Subtask findSubtaskByID(int idSubtask) {
        inMemoryHistoryManager.add(allSubtasks.get(idSubtask));
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
    public Task updateTask(Task task) {
        inMemoryHistoryManager.add(task);
        allTasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic oldEpic = allEpics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        inMemoryHistoryManager.add(epic);
        return oldEpic;

    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask oldSubtask = allSubtasks.get(subtask.getId());
        oldSubtask.setName(subtask.getName());
        oldSubtask.setDescription(subtask.getDescription());
        oldSubtask.setStatus(subtask.getStatus());
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
        inMemoryHistoryManager.add(subtask);
        return subtask;
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