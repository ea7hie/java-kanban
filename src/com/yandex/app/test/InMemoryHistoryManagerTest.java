package com.yandex.app.test;

import static org.junit.jupiter.api.Assertions.*;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;

class InMemoryHistoryManagerTest {
    private static final InMemoryHistoryManager inMemoryHistoryManager = (InMemoryHistoryManager)
            Managers.getDefaultHistory();
    private static final InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    public static final ArrayList<Task> idsViewedTasksInTest = new ArrayList<>();

    @BeforeEach
    public void makeBeforeEach() {
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
        Subtask subtask = inMemoryTaskManager.findSubtaskByID(13);

        inMemoryHistoryManager.add(task);
        inMemoryHistoryManager.add(epic);
        inMemoryHistoryManager.add(subtask);

        idsViewedTasksInTest.add(task);
        idsViewedTasksInTest.add(epic);
        idsViewedTasksInTest.add(subtask);
    }

    @AfterEach
    public void makeAfterEach() {
        idsViewedTasksInTest.clear();
        inMemoryHistoryManager.clearListOfViewedTasks();
    }

    //проверка метода inMemoryHistoryManager.add(Task task) и метода inMemoryHistoryManager.getHistory().
    @Test
    void shouldAddNewTask() {
        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.removeAllTasksInViewedTasks().
    @Test
    void shouldRemoveAllTasksInViewedTasks() {
        Task task2 = inMemoryTaskManager.findTaskByID(2);
        Task task3 = inMemoryTaskManager.findTaskByID(3);
        Epic epic5 = inMemoryTaskManager.findEpicByID(5);
        Subtask subtask9 = inMemoryTaskManager.findSubtaskByID(9);

        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(epic5);
        inMemoryHistoryManager.add(subtask9);

        idsViewedTasksInTest.removeFirst();//удалили из эталона самый первый task
        idsViewedTasksInTest.add(epic5);
        idsViewedTasksInTest.add(subtask9);

        inMemoryHistoryManager.removeAllTasksInViewedTasks();
        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.removeAllEpicsInViewedTasks().
    @Test
    void shouldRemoveAllEpicsInViewedTasks() {
        Task task2 = inMemoryTaskManager.findTaskByID(2);
        Task task3 = inMemoryTaskManager.findTaskByID(3);
        Epic epic4 = inMemoryTaskManager.findEpicByID(4);
        Epic epic6 = inMemoryTaskManager.findEpicByID(6);
        Subtask subtask9 = inMemoryTaskManager.findSubtaskByID(9);
        Subtask subtask11 = inMemoryTaskManager.findSubtaskByID(11);
        Subtask subtask12 = inMemoryTaskManager.findSubtaskByID(12);

        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(subtask9);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(epic4);
        inMemoryHistoryManager.add(subtask11);
        inMemoryHistoryManager.add(epic6);
        inMemoryHistoryManager.add(subtask12);

        inMemoryHistoryManager.removeAllEpicsInViewedTasks();

        idsViewedTasksInTest.remove(1);
        idsViewedTasksInTest.add(task2);
        idsViewedTasksInTest.add(task3);
        idsViewedTasksInTest.add(subtask11);
        idsViewedTasksInTest.add(subtask12);

        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.removeOneElem(Task).
    @Test
    void shouldDeleteFirstTask() {
        inMemoryHistoryManager.removeOneElem(1);
        idsViewedTasksInTest.removeFirst();

        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.updateOneElem().
    @Test
    void shouldUpdateFirstTask() {
        Task updatedTask = new Task("NEWNAME1", "desc 1", 1);
        inMemoryTaskManager.updateTask(updatedTask);
        inMemoryHistoryManager.updateOneElem(updatedTask);

        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка на переполнение
    @Test
    void sizeOfListMustbe10() {
        Task task2 = inMemoryTaskManager.findTaskByID(2);
        Task task3 = inMemoryTaskManager.findTaskByID(3);
        Epic epic4 = inMemoryTaskManager.findEpicByID(4);
        Epic epic6 = inMemoryTaskManager.findEpicByID(6);
        Subtask subtask9 = inMemoryTaskManager.findSubtaskByID(9);
        Subtask subtask11 = inMemoryTaskManager.findSubtaskByID(11);
        Subtask subtask12 = inMemoryTaskManager.findSubtaskByID(12);

        inMemoryHistoryManager.add(task2);
        inMemoryHistoryManager.add(subtask9);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(task3);
        inMemoryHistoryManager.add(epic4);
        inMemoryHistoryManager.add(subtask11);
        inMemoryHistoryManager.add(epic6);
        inMemoryHistoryManager.add(subtask12);
        inMemoryHistoryManager.add(subtask9);
        inMemoryHistoryManager.add(task2);

        idsViewedTasksInTest.clear();
        idsViewedTasksInTest.add(task2);
        idsViewedTasksInTest.add(subtask9);
        idsViewedTasksInTest.add(task3);
        idsViewedTasksInTest.add(task3);
        idsViewedTasksInTest.add(epic4);
        idsViewedTasksInTest.add(subtask11);
        idsViewedTasksInTest.add(epic6);
        idsViewedTasksInTest.add(subtask12);
        idsViewedTasksInTest.add(subtask9);
        idsViewedTasksInTest.add(task2);

        ArrayList<Task> checkedList = inMemoryHistoryManager.getHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

//проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnWorkingInMemoryHistoryManagerAndEmptyListOfViewedTasks() {
        for (int i = 0; i < 5; i++) {
            InMemoryHistoryManager checkingInMemoryHistoryManager = (InMemoryHistoryManager)
                    Managers.getDefaultHistory();
            assertTrue(checkingInMemoryHistoryManager.getHistory().isEmpty());
        }
    }

//проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnWorkingInMemoryHistoryManagerAndAddInListNewViewedTasks() {
        for (int i = 0; i < 5; i++) {
            InMemoryHistoryManager checkingInMemoryHistoryManager = (InMemoryHistoryManager)
                    Managers.getDefaultHistory();
            Task task2 = inMemoryTaskManager.findTaskByID(2);
            checkingInMemoryHistoryManager.add(task2);
            idsViewedTasksInTest.clear();
            idsViewedTasksInTest.add(task2);

            ArrayList<Task> checkedList = checkingInMemoryHistoryManager.getHistory();
            assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
        }
    }

//проверка, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void checkTaskInHistoryManager() {
        String name = "Первое значение имени";
        String desc = "первое значение описания";
        Task newTask = new Task(name, desc, 1);
        inMemoryHistoryManager.add(newTask);
        Task savedTask = inMemoryHistoryManager.getHistory().getLast();

        assertEquals(newTask, savedTask);
    }

    @Test
    void checkEpicInHistoryManager() {
        String name = "Первое значение имени";
        String desc = "первое значение описания";
        Epic newEpic = new Epic(name, desc, 1);
        inMemoryHistoryManager.add(newEpic);
        Task savedEpic= inMemoryHistoryManager.getHistory().getLast();

        assertEquals(newEpic, savedEpic);
    }

    @Test
    void checkSubtaskInHistoryManager() {
        String name = "Первое значение имени";
        String desc = "первое значение описания";
        Subtask newSubtask = new Subtask(name, desc, 1, 6);
        inMemoryHistoryManager.add(newSubtask);
        Task savedSubtask = inMemoryHistoryManager.getHistory().getLast();

        assertTrue(newSubtask == savedSubtask);
    }
}