package commonTests;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CommonTestOfInMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    private static InMemoryTaskManager inMemoryTaskManager;

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

    @Override
    @Test
    void shouldGetAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.add(inMemoryTaskManager.findTaskByID(1));
        allTasks.add(inMemoryTaskManager.findTaskByID(2));
        allTasks.add(inMemoryTaskManager.findTaskByID(3));

        assertArrayEquals(new ArrayList[]{allTasks}, new ArrayList[]{inMemoryTaskManager.getAllTasks()});
    }

    @Override
    @Test
    void shouldGetAllEpics() {
        ArrayList<Task> allEpics = new ArrayList<>();
        allEpics.add(inMemoryTaskManager.findEpicByID(4));
        allEpics.add(inMemoryTaskManager.findEpicByID(5));
        allEpics.add(inMemoryTaskManager.findEpicByID(6));

        assertArrayEquals(new ArrayList[]{allEpics}, new ArrayList[]{inMemoryTaskManager.getAllEpics()});
    }

    @Override
    @Test
    void shouldGetAllSubtasks() {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(7));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(8));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(9));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(10));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(11));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(12));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(13));
        allSubtasks.add(inMemoryTaskManager.findSubtaskByID(14));

        assertArrayEquals(new ArrayList[]{allSubtasks}, new ArrayList[]{inMemoryTaskManager.getAllSubtasks()});
    }

    @Override
    @Test
    void shouldRemoveAllTasks() {
        inMemoryTaskManager.removeAllTasks();
        assertTrue(inMemoryTaskManager.getAllTasks().isEmpty());
    }

    @Override
    @Test
    void shouldRemoveAllEpics() {
        inMemoryTaskManager.removeAllEpics();
        assertTrue(inMemoryTaskManager.getAllEpics().isEmpty()
                && inMemoryTaskManager.getAllSubtasks().isEmpty());
    }

    @Override
    @Test
    void shouldRemoveAllSubtasks() {
        inMemoryTaskManager.removeAllSubtasks();
        assertTrue(inMemoryTaskManager.getAllSubtasks().isEmpty());
    }

    @Override
    @Test
    void shouldDeleteOneTaskByID() {
        int sizeBeforeDelete = inMemoryTaskManager.getAllTasks().size();
        inMemoryTaskManager.deleteOneTaskByID(1);
        int sizeAfterDelete = inMemoryTaskManager.getAllTasks().size();
        assertTrue((sizeBeforeDelete == sizeAfterDelete + 1) && !inMemoryTaskManager.isTaskAddedByID(1));
    }

    @Override
    @Test
    void shouldDeleteOneEpicByID() {
        int sizeBeforeDelete = inMemoryTaskManager.getAllEpics().size();
        int countAllSubtasksBeforeDelete = inMemoryTaskManager.getAllSubtasks().size();
        int countSubtaskInEpic = inMemoryTaskManager.findEpicByID(4).getSubtasksIDs().size();

        inMemoryTaskManager.deleteOneEpicByID(4);
        int sizeAfterDelete = inMemoryTaskManager.getAllEpics().size();
        int countAllSubtasksAfterDelete = inMemoryTaskManager.getAllSubtasks().size();

        assertTrue((sizeBeforeDelete == sizeAfterDelete + 1) && !inMemoryTaskManager.isEpicAddedByID(4)
                && (countAllSubtasksBeforeDelete == countAllSubtasksAfterDelete + countSubtaskInEpic));
    }

    @Override
    @Test
    void shouldDeleteOneSubtaskByID() {
        int sizeBeforeDelete = inMemoryTaskManager.getAllSubtasks().size();
        int idOfEpic = inMemoryTaskManager.findSubtaskByID(7).getIdOfSubtaskEpic();
        int countSubtaskInEpicBeforeDelete = inMemoryTaskManager.findEpicByID(idOfEpic).getSubtasksIDs().size();

        inMemoryTaskManager.deleteOneSubtaskskByID(7);
        int sizeAfterDelete = inMemoryTaskManager.getAllSubtasks().size();
        int countSubtaskInEpiAfterDelete = inMemoryTaskManager.findEpicByID(idOfEpic).getSubtasksIDs().size();

        assertTrue((sizeBeforeDelete == sizeAfterDelete + 1) && !inMemoryTaskManager.isSubtaskAddedByID(7)
                && (countSubtaskInEpicBeforeDelete == countSubtaskInEpiAfterDelete + 1));
    }

    @Override
    @Test
    void shouldSaveNewTask() {
        int sizeBeforeSaving = inMemoryTaskManager.getAllTasks().size();
        inMemoryTaskManager.saveNewTask(
                new Task("!!!15", "desc 15", 15, 35, "27.01.1974; 12:30"));
        int sizeAfterSaving = inMemoryTaskManager.getAllTasks().size();

        assertTrue((sizeBeforeSaving + 1 == sizeAfterSaving) && inMemoryTaskManager.isTaskAddedByID(15));
    }

    @Override
    @Test
    void shouldSaveNewEpic() {
        int sizeBeforeSaving = inMemoryTaskManager.getAllEpics().size();
        inMemoryTaskManager.saveNewEpic(new Epic("???15", "DESC 15", 15));
        int sizeAfterSaving = inMemoryTaskManager.getAllEpics().size();

        assertTrue((sizeBeforeSaving + 1 == sizeAfterSaving) && inMemoryTaskManager.isEpicAddedByID(15));
    }

    @Override
    @Test
    void shouldSaveNewSubtask() {
        int sizeBeforeSaving = inMemoryTaskManager.getAllSubtasks().size();
        inMemoryTaskManager.saveNewSubtask(new Subtask("---15", "Desc---15", 15,
                6, 78, "05.03.2091; 18:48"));
        int sizeAfterSaving = inMemoryTaskManager.getAllSubtasks().size();

        assertTrue((sizeBeforeSaving + 1 == sizeAfterSaving) && inMemoryTaskManager.isSubtaskAddedByID(15));
    }

    @Override
    @Test
    void shouldFindTaskByID() {
        Task task = inMemoryTaskManager.saveNewTask(
                new Task("!!!15", "desc 15", 15, 35, "27.01.1974; 12:30"));
        Task taskByID = inMemoryTaskManager.findTaskByID(15);
        assertEquals(task, taskByID);
    }

    @Override
    @Test
    void shouldFindEpicByID() {
        Epic epic = inMemoryTaskManager.saveNewEpic(new Epic("???15", "DESC 15", 15));
        Epic epicByID = inMemoryTaskManager.findEpicByID(15);
        assertEquals(epic, epicByID);
    }

    @Override
    @Test
    void shouldFindSubtaskByID() {
        Subtask subtask = inMemoryTaskManager.saveNewSubtask(new Subtask("---15", "Desc---15", 15,
                6, 78, "05.03.2091; 18:48"));
        Subtask subtaskByID = inMemoryTaskManager.findSubtaskByID(15);
        assertEquals(subtask, subtaskByID);
    }

    @Override
    @Test
    void shouldUpdateTask() {
        Task newTask = inMemoryTaskManager.findTaskByID(1);
        newTask.setName("NEW NAME1");
        newTask.setDescription("NEW NAME1");
        Task updateTask = inMemoryTaskManager.updateTask(newTask);
        assertEquals(newTask, updateTask);
    }

    @Override
    @Test
    void shouldUpdateEpic() {
        Epic epic = inMemoryTaskManager.findEpicByID(5);
        epic.setName("NEW NAME5");
        epic.setDescription("NEW NAME5");
        Epic updatedEpic = inMemoryTaskManager.updateEpic(epic);
        assertEquals(epic, updatedEpic);
    }

    @Override
    @Test
    void shouldUpdateSubtask() {
        Subtask subtask = inMemoryTaskManager.findSubtaskByID(7);
        subtask.setName("NEW NAME7");
        subtask.setDescription("NEW NAME7");
        Subtask updatedSubtask = inMemoryTaskManager.updateSubtask(subtask);

        assertEquals(subtask, updatedSubtask);
    }
}