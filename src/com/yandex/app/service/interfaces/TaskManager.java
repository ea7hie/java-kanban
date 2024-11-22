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
    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    String deleteOneElementByID(int idForDelete);

//сохранить что-то
    void saveNewTask(Task task);

    void saveNewEpic(Epic epic);

    void saveNewSubtask(Subtask subtask);

//найти что-то
    Task findTaskByID(int idForSearch);

    Epic findEpicByID(int idForSearch);

    Subtask findSubtaskByID(int idSubtask);

//обновить что-то
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);
}