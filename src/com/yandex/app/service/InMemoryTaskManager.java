package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.comparators.ComparatorByStartTime;
import com.yandex.app.service.comparators.SubtaskStartTimeComparator;
import com.yandex.app.service.interfaces.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.yandex.app.model.Task.FORMAT_DATE;

public class InMemoryTaskManager implements TaskManager {
    private final InMemoryHistoryManager inMemoryHistoryManager = (InMemoryHistoryManager) Managers.getDefaultHistory();
    protected Map<Integer, Task> allTasks;
    protected Map<Integer, Epic> allEpics;
    protected Map<Integer, Subtask> allSubtasks;
    private final ArrayList<Integer> allIDs = new ArrayList<>();
    protected Set<Task> sortedTasks;
    private int idOfNewTask = 0;

    public InMemoryTaskManager() {
        this.allTasks = new HashMap<>();
        this.allEpics = new HashMap<>();
        this.allSubtasks = new HashMap<>();
        this.sortedTasks = new TreeSet<>(new ComparatorByStartTime());
    }

    private void countAllIDs() {
        allIDs.clear();
        allIDs.addAll(allTasks.keySet());
        allIDs.addAll(allEpics.keySet());
        allIDs.addAll(allSubtasks.keySet());
    }

    //сеттеры
    public void setSortedTasks(Set<Task> sortedTasks) {
        this.sortedTasks = sortedTasks;
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

    public String getFullDescOfAllSubtasksOfEpicById(int idOfEpic) {
        List<Subtask> allSubtasksInEpic = allEpics.get(idOfEpic).getSubtasksIDs().stream()
                .map(index -> allSubtasks.get(index))
                .sorted(new SubtaskStartTimeComparator())
                .toList();

        String fullDescription = "";
        for (int i = 0; i < allSubtasksInEpic.size(); i++) {
            Subtask currentSubtask = allSubtasksInEpic.get(i);
            fullDescription = fullDescription
                    + " - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
                    + "Подзадача №" + (i + 1) + currentSubtask.toString();
        }
        return fullDescription + "\n";
    }

    public String getPrioritizedTasks() {
        return sortedTasks.stream().map(Task::toString).collect(Collectors.joining());
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
    public void removeAllTasks() {
        allTasks.clear();
        sortedTasks = sortedTasks.stream().
                filter(task -> task.getClass().toString().endsWith("Subtask"))
                .collect(Collectors.toSet());
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
        sortedTasks = sortedTasks.stream().
                filter(task -> !task.getClass().toString().endsWith("Subtask"))
                .collect(Collectors.toSet());
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
        sortedTasks.add(task);
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
        sortedTasks.add(subtask);
        allEpics.get(subtask.getIdOfSubtaskEpic()).saveNewSubtaskIDs(subtask.getId());
        checkProgressStatusOfEpic(subtask.getIdOfSubtaskEpic());
        countDurationOfEpic(subtask.getIdOfSubtaskEpic());
        countStartTimeOfEpic(subtask.getIdOfSubtaskEpic());
        countEndTimeOfEpic(subtask.getIdOfSubtaskEpic());
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
    protected void checkProgressStatusOfEpic(int idOfEpic) {
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

    //расчёт начала эпика
    public void countStartTimeOfEpic(int idOfEpic) {
        Epic epicForUpdateStartTime = allEpics.get(idOfEpic);
        String minStartTimeOfSubtasks = epicForUpdateStartTime.getSubtasksIDs().stream()
                .map(id -> allSubtasks.get(id))
                .min(new SubtaskStartTimeComparator())
                .get()
                .getStartTime();
        epicForUpdateStartTime.setStartTime(minStartTimeOfSubtasks);
    }

    //расчёт окончания эпика
    public void countEndTimeOfEpic(int idOfEpic) {
        Epic epicForUpdateStartTime = allEpics.get(idOfEpic);
        String maxEndTimeOfSubtasks = epicForUpdateStartTime.getSubtasksIDs().stream()
                .map(id -> allSubtasks.get(id))
                .max(new SubtaskStartTimeComparator())
                .get()
                .getEndTime();
        epicForUpdateStartTime.setEndTime(maxEndTimeOfSubtasks);
    }

    //расчёт продолжительности эпика
    public void countDurationOfEpic(int idOfEpic) {
        Epic epicForUpdateDuration = allEpics.get(idOfEpic);
        int duration = 0;
        for (Integer subtasksID : epicForUpdateDuration.getSubtasksIDs()) {
            duration += allSubtasks.get(subtasksID).getDuration();
        }
        epicForUpdateDuration.setDuration(duration);
    }

    public boolean isTimeOverlap(String startTimeInString, int durationInInt) {
        LocalDateTime startTime = LocalDateTime.parse(startTimeInString, FORMAT_DATE);
        LocalDateTime endTime = startTime.plus(Duration.ofMinutes(durationInInt));
        boolean isNotOverlap;

        for (Task task : allTasks.values()) {
            isNotOverlap = endTime.isBefore(LocalDateTime.parse(task.getStartTime(), FORMAT_DATE))
                    || startTime.isAfter(LocalDateTime.parse(task.getEndTime(), FORMAT_DATE));

            if (!isNotOverlap) {
                return true;
            }
        }

        for (Task task : allEpics.values()) {
            isNotOverlap = endTime.isBefore(LocalDateTime.parse(task.getStartTime(), FORMAT_DATE))
                    || startTime.isAfter(LocalDateTime.parse(task.getEndTime(), FORMAT_DATE));

            if (!isNotOverlap) {
                return true;
            }
        }

        for (Task task : allSubtasks.values()) {
            isNotOverlap = endTime.isBefore(LocalDateTime.parse(task.getStartTime(), FORMAT_DATE))
                    || startTime.isAfter(LocalDateTime.parse(task.getEndTime(), FORMAT_DATE));

            if (!isNotOverlap) {
                return true;
            }
        }

        return false;
    }
}