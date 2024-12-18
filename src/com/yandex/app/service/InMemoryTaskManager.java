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

    private int getIdOfNewTask() {
        return ++idOfNewTask;
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

    //удалить что-то
    @Override
    public String removeAllTasks() {
        allTasks.clear();
        inMemoryHistoryManager.removeAllTasksInViewedTasks();
        return "Список задач очищен.\n";
    }

    @Override
    public String removeAllEpics() {
        allEpics.clear();
        removeAllSubtasks();
        inMemoryHistoryManager.removeAllEpicsInViewedTasks();
        return "Список эпиков очищен.\n";
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
        inMemoryHistoryManager.remove(idForDelete);
        allTasks.remove(idForDelete);
        return printMessageAboutSuccessfulFinishingOperation();
    }

    @Override
    public String deleteOneEpicByID(int idForDelete) {
        for (int index : allEpics.get(idForDelete).getSubtasksIDs()) {
            inMemoryHistoryManager.remove(index);
            allSubtasks.remove(index);
        }
        inMemoryHistoryManager.remove(idForDelete);
        allEpics.remove(idForDelete);
        return printMessageAboutSuccessfulFinishingOperation();
    }

    @Override
    public String deleteOneSubtaskskByID(int idForDelete) {
        int idOfEpic = allSubtasks.get(idForDelete).getIdOfSubtaskEpic();
        inMemoryHistoryManager.remove(idForDelete);
        allEpics.get(idOfEpic).getSubtasksIDs().remove(Integer.valueOf(idForDelete));
        allSubtasks.remove(idForDelete);
        checkProgressStatusOfEpic(idOfEpic);
        return printMessageAboutSuccessfulFinishingOperation();
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
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
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
    public String updateTask(Task task) {
        inMemoryHistoryManager.add(task);
        allTasks.put(task.getId(), task);
        return printMessageAboutSuccessfulFinishingOperation();
    }

    @Override
    public String updateEpic(Epic epic) {
        Epic oldEpic = allEpics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        inMemoryHistoryManager.add(epic);
        return printMessageAboutSuccessfulFinishingOperation();

    }

    @Override
    public String updateSubtask(Subtask subtask) {
        Subtask oldSubtask = allSubtasks.get(subtask.getId());
        oldSubtask.setName(subtask.getName());
        oldSubtask.setDescription(subtask.getDescription());
        oldSubtask.setStatus(subtask.getStatus());
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
        inMemoryHistoryManager.add(subtask);
        return printMessageAboutSuccessfulFinishingOperation();
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

    //вывод сообщений о статусе выполненного процесса
    public String printMessageAboutSuccessfulFinishingOperation() {
        return "Выполнено успешно" + "\n";
    }
}