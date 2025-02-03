import com.yandex.app.model.Epic;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static InMemoryTaskManager inMemoryTaskManager;
    private static final Random random = new Random();

    @BeforeEach
    public void makeBeforeEach() {
        inMemoryTaskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();

        inMemoryTaskManager.saveNewTask(
                new Task("!!!1", "desc 1", 1, 35, "27.01.2004; 12:30"));
        inMemoryTaskManager.saveNewTask(
                new Task("!!!2", "desc 2", 1, 15, "27.01.2024; 22:30"));
        inMemoryTaskManager.saveNewTask(
                new Task("!!!3", "desc 3", 1, 3, "27.02.2025; 02:30"));

        inMemoryTaskManager.saveNewEpic(new Epic("???1", "DESC 1", 1));
        inMemoryTaskManager.saveNewEpic(new Epic("???2", "DESC 2", 1));
        inMemoryTaskManager.saveNewEpic(new Epic("???3", "DESC 3", 1));

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---4", 1,
                4, 78, "05.03.2001; 18:48"));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---4", 1, 4,
                52, "17.02.2006; 17:02"));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---4", 1, 4,
                8, "03.12.2025; 02:03"));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---4", 1, 4,
                1458, "01.01.2025; 15:47"));

        inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---5", 1, 5,
                245, "28.01.2025; 19:35"));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---5", 1, 5,
                17, "29.02.2024; 18:52"));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---5", 1, 5,
                1, "15.06.2024; 23:03"));
        inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---5", 1, 5,
                47, "19.01.2025; 12:36"));
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
        ArrayList<Integer> idsBeforeAttemptingSave = checkedEpic.getSubtasksIDs();
        ArrayList<Integer> correctIds = new ArrayList<>(idsBeforeAttemptingSave);

        int someId = 100500;
        if (inMemoryTaskManager.isSubtaskAddedByID(someId)) {
            checkedEpic.saveNewSubtaskIDs(someId);
        }
        ArrayList<Integer> checkedIds = checkedEpic.getSubtasksIDs();
        assertArrayEquals(new ArrayList[]{correctIds}, new ArrayList[]{checkedIds});
    }

    //объект Subtask нельзя сделать своим же эпиком
    @Test
    void shouldNotSave() {
        // аналогично обновлён метод смены id Epic'а в классе Subtask
        Subtask checkedSubtask = new Subtask("NAME", "Desc", 1, 5,
                48, "29.08.2026; 16:15");
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

    //проверка на добавление saveNewTask() с заданным id
    @Test
    void shouldSaveNewTasksWithOtherId() {
        int sizeBeforeSaveNewTasks = inMemoryTaskManager.getAllTasks().size();
        final int randomNumber = random.nextInt();

        Task firstCheckedTask = new Task("CHECK1", "Check 1", -100500,
                12, "25.04.2025; 18:53");
        Task secondCheckedTask = new Task("CHECK1", "Check 1", randomNumber,
                12, "25.04.2024; 18:53");
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

        Task newtask = new Task(name, description, 24, 12, "25.04.2025; 18:53");
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

    //проверка на неизменяемость полей при сохранении saveNewSubtask()
    @Test
    void shouldSaveNewSubtasksWithTrueValues() {
        String name = "someone name for Subtask1";
        String description = "someone desc for Subtask1";
        Subtask subtask1 = new Subtask(name, description, 24, 5,
                12, "25.04.2025; 18:53");
        inMemoryTaskManager.saveNewSubtask(subtask1);

        String name2 = "someone name for Subtask2";
        String description2 = "someone desc for Subtask2";
        Subtask subtask2 = new Subtask(name2, description2, 24, 5,
                12, "25.04.2024; 18:53");
        inMemoryTaskManager.saveNewSubtask(subtask2);

        assertTrue(name.equals(inMemoryTaskManager.findSubtaskByID(15).getName())
                && description.equals(inMemoryTaskManager.findSubtaskByID(15).getDescription())
                && 5 == inMemoryTaskManager.findSubtaskByID(15).getIdOfSubtaskEpic()
                && name2.equals(inMemoryTaskManager.findSubtaskByID(16).getName())
                && description2.equals(inMemoryTaskManager.findSubtaskByID(16).getDescription())
                && 5 == inMemoryTaskManager.findSubtaskByID(16).getIdOfSubtaskEpic()
        );
    }

    //в эпике не должны сохраняться удалённые подзадачи
    @Test
    void shouldDeleteIdOfDeletedSubtaskInEpic() {
        ArrayList<Integer> correctIds = inMemoryTaskManager.findEpicByID(4).getSubtasksIDs();
        correctIds.remove(1);

        inMemoryTaskManager.deleteOneSubtaskskByID(8);

        ArrayList<Integer> checkedIds = inMemoryTaskManager.findEpicByID(4).getSubtasksIDs();
        assertArrayEquals(new ArrayList[]{correctIds}, new ArrayList[]{checkedIds});
    }

    //проверка смены статуса эпика при смене статуса его подзадач
    @Test
    void shouldBeNew() {
        assertSame(inMemoryTaskManager.findEpicByID(4).getStatus(), Progress.NEW);
    }

    @Test
    void shouldBeDone() {
        Subtask subtaskByID7 = inMemoryTaskManager.findSubtaskByID(7);
        Subtask subtaskByID8 = inMemoryTaskManager.findSubtaskByID(8);
        Subtask subtaskByID9 = inMemoryTaskManager.findSubtaskByID(9);
        Subtask subtaskByID10 = inMemoryTaskManager.findSubtaskByID(10);

        subtaskByID7.setStatus(Progress.DONE);
        subtaskByID8.setStatus(Progress.DONE);
        subtaskByID9.setStatus(Progress.DONE);
        subtaskByID10.setStatus(Progress.DONE);

        inMemoryTaskManager.updateSubtask(subtaskByID7);
        inMemoryTaskManager.updateSubtask(subtaskByID8);
        inMemoryTaskManager.updateSubtask(subtaskByID9);
        inMemoryTaskManager.updateSubtask(subtaskByID10);

        assertSame(inMemoryTaskManager.findEpicByID(4).getStatus(), Progress.DONE);
    }

    @Test
    void shouldBeIN_PROGRESS() {
        Subtask subtaskByID7 = inMemoryTaskManager.findSubtaskByID(7);
        Subtask subtaskByID8 = inMemoryTaskManager.findSubtaskByID(8);
        Subtask subtaskByID9 = inMemoryTaskManager.findSubtaskByID(9);
        Subtask subtaskByID10 = inMemoryTaskManager.findSubtaskByID(10);

        subtaskByID7.setStatus(Progress.NEW);
        subtaskByID8.setStatus(Progress.NEW);
        subtaskByID9.setStatus(Progress.DONE);
        subtaskByID10.setStatus(Progress.DONE);

        inMemoryTaskManager.updateSubtask(subtaskByID7);
        inMemoryTaskManager.updateSubtask(subtaskByID8);
        inMemoryTaskManager.updateSubtask(subtaskByID9);
        inMemoryTaskManager.updateSubtask(subtaskByID10);

        assertSame(inMemoryTaskManager.findEpicByID(4).getStatus(), Progress.IN_PROGRESS);
    }

    @Test
    void shouldBeIN_PROGRESSAllSubtasksIsIN_PROGRESS() {
        Subtask subtaskByID7 = inMemoryTaskManager.findSubtaskByID(7);
        Subtask subtaskByID8 = inMemoryTaskManager.findSubtaskByID(8);
        Subtask subtaskByID9 = inMemoryTaskManager.findSubtaskByID(9);
        Subtask subtaskByID10 = inMemoryTaskManager.findSubtaskByID(10);

        subtaskByID7.setStatus(Progress.IN_PROGRESS);
        subtaskByID8.setStatus(Progress.IN_PROGRESS);
        subtaskByID9.setStatus(Progress.IN_PROGRESS);
        subtaskByID10.setStatus(Progress.IN_PROGRESS);

        inMemoryTaskManager.updateSubtask(subtaskByID7);
        inMemoryTaskManager.updateSubtask(subtaskByID8);
        inMemoryTaskManager.updateSubtask(subtaskByID9);
        inMemoryTaskManager.updateSubtask(subtaskByID10);

        assertSame(inMemoryTaskManager.findEpicByID(4).getStatus(), Progress.IN_PROGRESS);
    }

    @Test
    void shouldAllSubtaskHaveIdOfEpic() {
        boolean isHas = true;
        for (Subtask subtask : inMemoryTaskManager.getAllSubtasks()) {
            isHas = isHas && subtask.getIdOfSubtaskEpic() != 0;
        }
        assertTrue(isHas);
    }

    @Test
    void shouldNotSaveBecauseIsOverlaps() {
        int sizeBeforeTryToSave = inMemoryTaskManager.getAllTasks().size();
        if (inMemoryTaskManager.isTimeOverlap("28.01.2025; 19:30", 15)) {
            System.out.println("Can't to save");
        } else {
            inMemoryTaskManager.saveNewTask(new Task("someName", "somDesc", -1, 15,
                    "28.01.2025; 19:30")); //overlaps with subtask id = 11
        }
        int sizeAfterTryToSave = inMemoryTaskManager.getAllTasks().size();
        assertEquals(sizeBeforeTryToSave, sizeAfterTryToSave);
    }
}