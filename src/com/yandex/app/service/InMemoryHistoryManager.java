package com.yandex.app.service;

import com.yandex.app.model.Task;
import com.yandex.app.service.interfaces.HistoryManager;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    @Override
    public void add(Task task) {

    }

    @Override
    public ArrayList<Task> getHistory() {
        return null;
    }
}
