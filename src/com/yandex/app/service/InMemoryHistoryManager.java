package com.yandex.app.service;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.interfaces.HistoryManager;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> viewedTasks = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (viewedTasks.size() >= 10) {
            viewedTasks.removeFirst();
        }
        viewedTasks.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return viewedTasks;
    }

    public void removeAllTasksInViewedTasks() {
        viewedTasks.removeIf(viewedTask -> !(viewedTask instanceof Subtask || viewedTask instanceof Epic));
    }

    public void removeAllEpicsInViewedTasks() {
        viewedTasks.removeIf(viewedTask -> viewedTask instanceof Epic);
        viewedTasks.removeIf(viewedTask -> viewedTask instanceof Subtask);
    }

    public void removeOneElem(int idForDelete) {
        for (Task viewedTask : viewedTasks) {
            if (viewedTask.getId() == idForDelete) {
                viewedTasks.remove(viewedTask);
            }
        }
    }

    public <T extends Task> void updateOneElem(T task) {
        for (Task viewedTask : viewedTasks) {
            if (viewedTask.getId() == task.getId()) {
                viewedTask.setName(task.getName());
                viewedTask.setDescription(task.getDescription());
                viewedTask.setStatus(task.getStatus());
            }
        }
    }

    public void clearListOfViewedTasks() {
        viewedTasks.clear();
    }
}
