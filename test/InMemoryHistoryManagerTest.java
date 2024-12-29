import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryHistoryManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    InMemoryTaskManager inMemoryTaskManager;
    ArrayList<Task> idsViewedTasksInTest;
    ArrayList<Task> checkedList;

    @BeforeEach
    public void makeBeforeEach() {
        inMemoryTaskManager = new InMemoryTaskManager();
        idsViewedTasksInTest = new ArrayList<>();

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
        /*inMemoryTaskManager.findEpicByID(4).saveNewSubtaskIDs(7);
        inMemoryTaskManager.findEpicByID(4).saveNewSubtaskIDs(8);
        inMemoryTaskManager.findEpicByID(4).saveNewSubtaskIDs(9);
        inMemoryTaskManager.findEpicByID(4).saveNewSubtaskIDs(10);*/

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---5", 1, 5));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---5", 1, 5));
        /*inMemoryTaskManager.findEpicByID(5).saveNewSubtaskIDs(11);
        inMemoryTaskManager.findEpicByID(5).saveNewSubtaskIDs(12);
        inMemoryTaskManager.findEpicByID(5).saveNewSubtaskIDs(13);
        inMemoryTaskManager.findEpicByID(5).saveNewSubtaskIDs(14);*/

        Task task1 = inMemoryTaskManager.findTaskByID(1);
        Epic epic4 = inMemoryTaskManager.findEpicByID(4);
        Subtask subtask13 = inMemoryTaskManager.findSubtaskByID(13);

        idsViewedTasksInTest.add(task1);
        idsViewedTasksInTest.add(epic4);
        idsViewedTasksInTest.add(subtask13);
    }

    //проверка метода inMemoryHistoryManager.add(Task task) и метода inMemoryHistoryManager.getHistory().
    @Test
    void shouldShowCurrentListOfViewedTasks() {
        checkedList = inMemoryTaskManager.getListOfHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.removeAllTasksInViewedTasks().
    @Test
    void shouldRemoveAllTasksInViewedTasks() {
        Task task2 = inMemoryTaskManager.findTaskByID(2);
        Task task3 = inMemoryTaskManager.findTaskByID(3);
        Epic epic5 = inMemoryTaskManager.findEpicByID(5);
        Subtask subtask9 = inMemoryTaskManager.findSubtaskByID(9);

        idsViewedTasksInTest.removeFirst();//удалили из эталона самый первый task
        idsViewedTasksInTest.add(epic5);
        idsViewedTasksInTest.add(subtask9);

        inMemoryTaskManager.removeAllTasks();
        checkedList = inMemoryTaskManager.getListOfHistory();
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

        inMemoryTaskManager.removeAllEpics();

        idsViewedTasksInTest.remove(1);
        idsViewedTasksInTest.remove(1);
        idsViewedTasksInTest.add(task2);
        idsViewedTasksInTest.add(task3);

        checkedList = inMemoryTaskManager.getListOfHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.updateOneElem().
    @Test
    void shouldUpdateFirstTask() {
        Task updatedTask = new Task("NEWNAME1", "desc 1", 1);
        inMemoryTaskManager.updateTask(updatedTask);

        idsViewedTasksInTest.removeFirst();
        idsViewedTasksInTest.add(updatedTask);

        checkedList = inMemoryTaskManager.getListOfHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.updateOneElem().
    @Test
    void shouldUpdateSubtaskWithId8() {
        Subtask updatedSubtask8Double = inMemoryTaskManager.findSubtaskByID(8);
        Subtask updatedSubtask8 = new Subtask("NEWNAME8", "desc 8", 8, 4);

        inMemoryTaskManager.updateSubtask(updatedSubtask8);
        idsViewedTasksInTest.add(updatedSubtask8);

        checkedList = inMemoryTaskManager.getListOfHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
    }

    //проверка метода inMemoryHistoryManager.updateOneElem().
    @Test
    void shouldUpdateSubtaskWithId8InHistory() {
        Subtask subtask8 = inMemoryTaskManager.findSubtaskByID(8);
        Subtask updatedSubtask8 = new Subtask("NEWNAME8", "desc 8", 8, 4);

        inMemoryTaskManager.updateSubtask(updatedSubtask8);
        Subtask updatedSubtask8Double = (Subtask) inMemoryTaskManager.getListOfHistory().getLast();
        assertEquals(updatedSubtask8, updatedSubtask8Double);
    }

    //проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnWorkingInMemoryHistoryManagerAndEmptyListOfViewedTasks() {
        for (int i = 0; i < 5; i++) {
            InMemoryHistoryManager checkingInMemoryHistoryManager = (InMemoryHistoryManager)
                    Managers.getDefaultHistory();
            checkingInMemoryHistoryManager.clearListOfViewedTasks();
            assertTrue(checkingInMemoryHistoryManager.getHistory().isEmpty());
        }
    }

    //проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnWorkingInMemoryHistoryManagerAndAddInListNewViewedTasks() {
        for (int i = 0; i < 5; i++) {
            InMemoryTaskManager checkingInMemoryTaskManager = (InMemoryTaskManager)
                    Managers.getDefaultTaskManager();
            checkingInMemoryTaskManager.saveNewTask(new Task("0", "0", -1));
            Task task = checkingInMemoryTaskManager.findTaskByID(1);
            idsViewedTasksInTest.clear();
            idsViewedTasksInTest.add(task);

            checkedList = checkingInMemoryTaskManager.getListOfHistory();
            assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedList});
        }
    }

    //проверка, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void checkTaskInHistoryManager() {
        String name = "Первое значение имени";
        String desc = "первое значение описания";
        Task newTask = new Task(name, desc, 1);
        inMemoryTaskManager.saveNewTask(newTask);

        Task newTask2 = inMemoryTaskManager.findTaskByID(15);
        Task savedTask = inMemoryTaskManager.getListOfHistory().getLast();

        assertEquals(newTask, savedTask);
    }

    //проверка на неизменяемость полей при сохранении после просмотра эпика
    @Test
    void checkEpicInHistoryManager() {
        String name = "Первое значение имени";
        String desc = "первое значение описания";
        Epic newEpic = new Epic(name, desc, 1);
        inMemoryTaskManager.saveNewEpic(newEpic);

        Epic newEpic2 = inMemoryTaskManager.findEpicByID(15);
        Epic savedEpic = (Epic) inMemoryTaskManager.getListOfHistory().getLast();

        assertEquals(newEpic, savedEpic);
    }

    //проверка на неизменяемость полей при сохранении после просмотра подзадачи
    @Test
    void checkSubtaskInHistoryManager() {
        String name = "Первое значение имени";
        String desc = "первое значение описания";
        Subtask newSubtask = new Subtask(name, desc, 1, 6);
        inMemoryTaskManager.saveNewSubtask(newSubtask);
        Subtask newSubtask2 = inMemoryTaskManager.findSubtaskByID(15);
        Subtask savedSubtask = (Subtask) inMemoryTaskManager.getListOfHistory().getLast();

        assertTrue(newSubtask == savedSubtask);
    }

    //в истории не должны сохраняться удалённые подзадачи
    @Test
    void shouldDeleteDeletedSubtaskInHistory() {
        Epic epic4 = inMemoryTaskManager.findEpicByID(4);
        Subtask subtask8 = inMemoryTaskManager.findSubtaskByID(8);
        Subtask subtask8Double = inMemoryTaskManager.findSubtaskByID(8);
        Epic epic4Double = inMemoryTaskManager.findEpicByID(4);
        Task task2 = inMemoryTaskManager.findTaskByID(2);

        inMemoryTaskManager.deleteOneSubtaskskByID(8);

        idsViewedTasksInTest.remove(1);
        idsViewedTasksInTest.add(epic4);
        idsViewedTasksInTest.add(task2);

        ArrayList<Task> checkedTasks = inMemoryTaskManager.getListOfHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedTasks});
    }

    //в истории не должны сохраняться удалённые эпики и их подзадачи
    @Test
    void shouldDeleteDeletedEpicAndHisSubtasksInHistory() {
        Epic epic4 = inMemoryTaskManager.findEpicByID(4);
        epic4.saveNewSubtaskIDs(8);
        epic4.saveNewSubtaskIDs(9);
        Subtask subtask8 = inMemoryTaskManager.findSubtaskByID(8);
        Subtask subtask8Double = inMemoryTaskManager.findSubtaskByID(8);
        Epic epic4Double = inMemoryTaskManager.findEpicByID(4);
        Task task2 = inMemoryTaskManager.findTaskByID(2);
        Subtask subtask9 = inMemoryTaskManager.findSubtaskByID(9);

        inMemoryTaskManager.deleteOneEpicByID(4);

        idsViewedTasksInTest.remove(1);
        idsViewedTasksInTest.add(task2);

        ArrayList<Task> checkedTasks = inMemoryTaskManager.getListOfHistory();
        assertArrayEquals(new ArrayList[]{idsViewedTasksInTest}, new ArrayList[]{checkedTasks});
    }
}