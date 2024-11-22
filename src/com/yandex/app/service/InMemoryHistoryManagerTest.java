package com.yandex.app.service;

import static org.junit.jupiter.api.Assertions.*;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;

class InMemoryHistoryManagerTest {
    private static final InMemoryHistoryManager inMemoryHistoryManager = (InMemoryHistoryManager) Managers.getDefaultHistory();
    private static final InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    public static final ArrayList<Task> idsViewedTasksInTest = new ArrayList<>();

    @BeforeAll
    public static void madeBeforeAll() {
        inMemoryTaskManager.saveNewTask(new Task("!!!1", "desc 1", 1));
        inMemoryTaskManager.saveNewTask(new Task("!!!2", "desc 2", 1));
        inMemoryTaskManager.saveNewTask(new Task("!!!3", "desc 3", 1));

        inMemoryTaskManager.saveNewEpic(new Epic("???1", "DESC 1", 1));
        inMemoryTaskManager.saveNewEpic(new Epic("???2", "DESC 2", 1));
        inMemoryTaskManager.saveNewEpic(new Epic("???3", "DESC 3", 1));

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---4", 1, 4));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---4", 1, 4));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---4", 1, 4));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---4", 1, 4));

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---5", 1, 5));

        Task task = inMemoryTaskManager.findTaskByID(1);
        Epic epic = inMemoryTaskManager.findEpicByID(4);
        Subtask subtask = inMemoryTaskManager.findSubtaskByID(11);
        idsViewedTasksInTest.add(task);
        idsViewedTasksInTest.add(epic);
        idsViewedTasksInTest.add(subtask);

        inMemoryHistoryManager.

    }

    @Test
    void shouldAddNewTask() {
        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();


//        assertArrayEquals(inMemoryHistoryManager.getHistory(), idsViewedTasksInTest);
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    @Test
    void getHistory() {
    }

    @Test
    void removeAllTasksInViewedTasks() {
    }

    @Test
    void removeAllEpicsInViewedTasks() {
    }

    @Test
    void removeOneElem() {
    }

    @Test
    void updateOneElem() {
    }
}