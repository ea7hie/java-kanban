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

    //удалить что-то
    String removeAllTasks();

    String removeAllEpics();

    void removeAllSubtasks();

    String deleteOneTaskByID(int idForDelete);

    String deleteOneEpicByID(int idForDelete);

    String deleteOneSubtaskskByID(int idForDelete);

    //сохранить что-то
    void saveNewTask(Task task);

    void saveNewEpic(Epic epic);

    void saveNewSubtask(Subtask subtask);

    //найти что-то
    Task findTaskByID(int idForSearch);

    Epic findEpicByID(int idForSearch);

    Subtask findSubtaskByID(int idSubtask);

    //обновить что-то
    String updateTask(Task task);

    String updateEpic(Epic epic);

    String updateSubtask(Subtask subtask);
}