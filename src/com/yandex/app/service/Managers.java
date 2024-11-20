package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.interfaces.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class Managers {
    public TaskManager getDefault() {
        return new TaskManager() {
            @Override
            public ArrayList<Task> getAllTasks() {
                return null;
            }

            @Override
            public ArrayList<Epic> getAllEpics() {
                return null;
            }

            @Override
            public ArrayList<Subtask> getAllSubtasks() {
                return null;
            }

            @Override
            public List<Task> getHistory() {
                return List.of();
            }

            @Override
            public void removeAllTasks() {

            }

            @Override
            public void removeAllEpics() {

            }

            @Override
            public void removeAllSubtasks() {

            }

            @Override
            public String deleteOneElementByID(int idForDelete) {
                return "";
            }

            @Override
            public void saveNewTask(Task task) {

            }

            @Override
            public void saveNewEpic(Epic epic) {

            }

            @Override
            public void saveNewSubtask(Subtask subtask) {

            }

            @Override
            public Task findTaskByID(int idForSearch) {
                return null;
            }

            @Override
            public Epic findEpicByID(int idForSearch) {
                return null;
            }

            @Override
            public Subtask findSubtaskByID(int idSubtask) {
                return null;
            }

            @Override
            public void updateTask(Task task) {

            }

            @Override
            public void updateEpic(Epic epic) {

            }

            @Override
            public void updateSubtask(Subtask subtask) {

            }
        };
    }

    //public static
}
