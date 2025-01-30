package com.yandex.app.service;

import com.yandex.app.service.interfaces.HistoryManager;
import com.yandex.app.service.interfaces.TaskManager;

public final class Managers {
    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}