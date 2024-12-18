package com.yandex.app.service.interfaces;

import com.yandex.app.model.Task;

import java.util.ArrayList;

public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    ArrayList<Task> getHistory();
}
