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
        //если это не эпик и не подзадача, то это задача - надо удалить
        viewedTasks.removeIf(viewedTask -> !(viewedTask instanceof Subtask || viewedTask instanceof Epic));
    }

    public void removeAllEpicsInViewedTasks() {
        //Нужно перед удалением запомнить id всех эпиков, чтобы потом удалить из истории все их подзадачи
        //*мысли вслух* если объединить обе проверки, то мб такое, что сначала проверится и пройдет дальше подзадача,
        //а её эпик удалиться позже при следующих итерациях. Потому дважды надо пройтись по списку

        ArrayList<Integer> idsAllEpicsInHistory = new ArrayList<>();
        for (Task viewedTask : viewedTasks) {
            if (viewedTask instanceof Epic) {
                idsAllEpicsInHistory.add(viewedTask.getId());
            }
        }

        viewedTasks.removeIf(viewedTask -> viewedTask instanceof Epic);
        viewedTasks.removeIf(viewedTask -> (viewedTask instanceof Subtask)
                && (idsAllEpicsInHistory.contains(((Subtask) viewedTask).getIdOfSubtaskEpic())));
    }

    public void removeOneElem(int idForDelete) {
        for (Task viewedTask : viewedTasks) {
            if (viewedTask.getId() == idForDelete) {
                viewedTasks.remove(viewedTask);
                return;
            }
        }
    }

    public <T extends Task> void updateOneElem(T task) {
        for (Task viewedTask : viewedTasks) {
            if (viewedTask.getId() == task.getId()) {
                viewedTask.setName(task.getName());
                viewedTask.setDescription(task.getDescription());
                viewedTask.setStatus(task.getStatus());
                return;
            }
        }
    }

    public void clearListOfViewedTasks() {
        viewedTasks.clear();
    }
}
