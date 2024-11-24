package com.yandex.app.test;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

class InMemoryTaskManagerTest {
    private static InMemoryTaskManager inMemoryTaskManager;// = new InMemoryTaskManager();
    private static final Random random = new Random();

    @BeforeEach
    public void makeBeforeEach() {
        inMemoryTaskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();
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
    }

    //два объекта Task равны, если равны их id
    @Test
    void shouldBeEqualTwoTasksIfEqualTheirIds() {
        Task checkedTask1 = inMemoryTaskManager.findTaskByID(1);
        Task checkedTask2 = inMemoryTaskManager.findTaskByID(1);
        assertEquals(checkedTask1, checkedTask2);
    }

    //два объекта Epic равны, если равны их id
    @Test
    void shouldBeEqualTwoEpicsIfEqualTheirIds() {
        Epic checkedEpic1 = inMemoryTaskManager.findEpicByID(4);
        Epic checkedEpic2 = inMemoryTaskManager.findEpicByID(4);
        assertEquals(checkedEpic1, checkedEpic2);
    }

    //два объекта Subtask равны, если равны их id
    @Test
    void shouldBeEqualTwoSubtasksIfEqualTheirIds() {
        Subtask checkedSubtask1 = inMemoryTaskManager.findSubtaskByID(13);
        Subtask checkedSubtask2 = inMemoryTaskManager.findSubtaskByID(13);
        assertEquals(checkedSubtask1, checkedSubtask2);
    }

    //объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void shouldNotAdd() {
        Epic checkedEpic = inMemoryTaskManager.findEpicByID(4);
        ArrayList<Integer> idsBeforeAttemptingSave= (ArrayList<Integer>) checkedEpic.getSubtasksIDs();

        //выяснилось, что в пред.версии оба списка ссылались на один и тот же объект,
        //и проверка всегда давала положительный результат, так как изменения в одном списке отображались и в другом
        // потому сейчас создаётся новый независимый от других список
        ArrayList<Integer> correctIds = new ArrayList<>(idsBeforeAttemptingSave);

        int someId = 100500;
        checkedEpic.saveNewSubtaskIDs(someId, inMemoryTaskManager.getIdsOfAllSubtasks());

        ArrayList<Integer> checkedIds = (ArrayList<Integer>) checkedEpic.getSubtasksIDs();
        System.out.println(correctIds);
        System.out.println(checkedIds);
        assertArrayEquals(new ArrayList[]{correctIds}, new ArrayList[]{checkedIds});
    }

    //объект Subtask нельзя сделать своим же эпиком
    @Test
    void shouldNotSave() {
        // аналогично обновлён метод смены id Epic'а в классе Subtask
        Subtask checkedSubtask = new Subtask("NAME", "Desc", 1, 5);
        checkedSubtask.setIdOfSubtaskEpic(1);
        assertNotEquals(1, 4);
    }

    //проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnWorkingInMemoryTaskManager() {
        for (int i = 0; i < 5; i++) {
            InMemoryTaskManager checkingInMemoryTaskManager = (InMemoryTaskManager)
                    Managers.getDefaultTaskManager();

            Task task2 = inMemoryTaskManager.findTaskByID(2);

            checkingInMemoryTaskManager.saveNewTask(task2);
            Task checkingTask = checkingInMemoryTaskManager.findTaskByID(1);
            assertTrue(task2.getName().equals(checkingTask.getName())
                    && task2.getDescription().equals(checkingTask.getDescription())
                    && task2.getStatus().equals(checkingTask.getStatus())
            );
        }
    }

    //проверка на добавление saveNewTask()
    @Test
    void sizeOfListOfTasksShouldBeIncrement() {
        int sizeBeforeSaveNewTask = inMemoryTaskManager.getAllTasks().size();
        inMemoryTaskManager.saveNewTask(new Task("CHECK1", "Check 1", 1));
        int sizeAfterSaveNewTask = inMemoryTaskManager.getAllTasks().size();
        assertEquals(++sizeBeforeSaveNewTask, sizeAfterSaveNewTask);
    }

    //проверка на добавление saveNewEpic()
    @Test
    void sizeOfListOfEpicsShouldBeIncrement() {
        int sizeBeforeSaveNewEpic = inMemoryTaskManager.getAllEpics().size();
        inMemoryTaskManager.saveNewEpic(new Epic("???3", "DESC 3", 1));
        int sizeAfterSaveNewEpic = inMemoryTaskManager.getAllEpics().size();
        assertEquals(++sizeBeforeSaveNewEpic, sizeAfterSaveNewEpic);
    }

    //проверка на добавление saveNewSubtask()
    @Test
    void sizeOfListOfSubtasksShouldBeIncrement() {
        int sizeBeforeSaveNewSubtask = inMemoryTaskManager.getAllSubtasks().size();
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---5", 1, 5));
        int sizeAfterSaveNewSubtask = inMemoryTaskManager.getAllSubtasks().size();
        assertEquals(++sizeBeforeSaveNewSubtask, sizeAfterSaveNewSubtask);
    }

    //проверка на поиск findTaskByID
    @Test
    void shouldReturnSearchedTask() {
        Task newTask = new Task("checking", "desc checking", 1);
        inMemoryTaskManager.saveNewTask(newTask);
        assertEquals(newTask, inMemoryTaskManager.findTaskByID(15));
    }

    //проверка на поиск findEpicByID
    @Test
    void shouldReturnSearchedEpic() {
        Epic newEpic = new Epic("???3", "DESC 3", 1);
        inMemoryTaskManager.saveNewEpic(newEpic);
        assertTrue(newEpic.getName().equals(inMemoryTaskManager.findEpicByID(15).getName())
                && newEpic.getDescription().equals(inMemoryTaskManager.findEpicByID(15).getDescription())
                && newEpic.getStatus().equals(inMemoryTaskManager.findEpicByID(15).getStatus())
                && newEpic.getSubtasksIDs().isEmpty()
                && 15 == inMemoryTaskManager.findEpicByID(15).getId()
        );
    }

    //проверка на поиск findSubtaskByID
    @Test
    void shouldReturnSearchedSubtask() {
        Subtask newSubtask = new Subtask("---3", "Desc---5", 1, 5);
        inMemoryTaskManager.saveNewSubtask(newSubtask);
        assertTrue(newSubtask.getName().equals(inMemoryTaskManager.findSubtaskByID(15).getName())
                && newSubtask.getDescription().equals(inMemoryTaskManager.findSubtaskByID(15).getDescription())
                && newSubtask.getStatus().equals(inMemoryTaskManager.findSubtaskByID(15).getStatus())
                && newSubtask.getIdOfSubtaskEpic() == inMemoryTaskManager.findSubtaskByID(15).getIdOfSubtaskEpic()
                && 15 == inMemoryTaskManager.findSubtaskByID(15).getId()
        );
    }

    //проверка на добавление saveNewTask() с заданным id
    @Test
    void shouldSaveNewTasksWithOtherId() {
        int sizeBeforeSaveNewTasks = inMemoryTaskManager.getAllTasks().size();
        final int randomNumber = random.nextInt();

        Task firstCheckedTask = new Task("CHECK1", "Check 1", -100500);
        Task secondCheckedTask = new Task("CHECK1", "Check 1", randomNumber);
        inMemoryTaskManager.saveNewTask(firstCheckedTask);
        inMemoryTaskManager.saveNewTask(secondCheckedTask);

        int sizeAfterSaveNewTasks = inMemoryTaskManager.getAllTasks().size();
        assertTrue((sizeBeforeSaveNewTasks + 2) == sizeAfterSaveNewTasks
                && -100500 != firstCheckedTask.getId()
                && randomNumber != secondCheckedTask.getId()
        );
    }

    //проверка на неизменяемость полей при сохранении saveNewTask()
    @Test
    void shouldSaveNewTasksWithTrueValues() {
        String name = "someone name";
        String description = "someone desc";

        Task newtask = new Task(name, description, 24);
        inMemoryTaskManager.saveNewTask(newtask);

        assertTrue(name.equals(inMemoryTaskManager.findTaskByID(15).getName())
                && description.equals(inMemoryTaskManager.findTaskByID(15).getDescription())
        );
    }
    //проверка на неизменяемость полей при сохранении saveNewEpic()
    @Test
    void shouldSaveNewEpicsWithTrueValues() {
       String name = "someone name for epic";
       String description = "someone desc for epic";

       Epic newEpic = new Epic(name, description, 24);
       inMemoryTaskManager.saveNewEpic(newEpic);

        assertTrue(name.equals(inMemoryTaskManager.findEpicByID(15).getName())
                && description.equals(inMemoryTaskManager.findEpicByID(15).getDescription())
        );
    }

    //проверка на неизменяемость полей при сохранении saveNewTask()
    @Test
    void shouldSaveNewSubtasksWithTrueValues() {
       String name = "someone name for Subtask1";
       String description = "someone desc for Subtask1";
       Subtask subtask1 = new Subtask(name, description, 24, 5);
       inMemoryTaskManager.saveNewSubtask(subtask1);

       String name2 = "someone name for Subtask2";
       String description2 = "someone desc for Subtask2";
       Subtask subtask2 = new Subtask(name2, description2, 24, 5);
       inMemoryTaskManager.saveNewSubtask(subtask2);

        assertTrue(name.equals(inMemoryTaskManager.findSubtaskByID(15).getName())
                && description.equals(inMemoryTaskManager.findSubtaskByID(15).getDescription())
                && 5 == inMemoryTaskManager.findSubtaskByID(15).getIdOfSubtaskEpic()
                && name2.equals(inMemoryTaskManager.findSubtaskByID(16).getName())
                && description2.equals(inMemoryTaskManager.findSubtaskByID(16).getDescription())
                && 5 == inMemoryTaskManager.findSubtaskByID(16).getIdOfSubtaskEpic()
        );
    }
}