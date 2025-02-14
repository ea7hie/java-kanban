package com.yandex.app.service.interfaces;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;

import java.util.ArrayList;

public interface TaskManager {
    //получить что-то (геттеры)
    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getAllSubtasks();

    ArrayList<Task> getListOfHistory();

    String showListViewedTasks();

    String getPrioritizedTasks();

    //удалить что-то
    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    void deleteOneTaskByID(int idForDelete);

    void deleteOneEpicByID(int idForDelete);

    void deleteOneSubtaskskByID(int idForDelete);

    //сохранить что-то
    Task saveNewTask(Task task);

    Epic saveNewEpic(Epic epic);

    Subtask saveNewSubtask(Subtask subtask);

    //найти что-то
    Task findTaskByID(int idForSearch);

    Epic findEpicByID(int idForSearch);

    Subtask findSubtaskByID(int idSubtask);

    //обновить что-то
    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    boolean isTimeOverlap(String startTime, int duration);
}