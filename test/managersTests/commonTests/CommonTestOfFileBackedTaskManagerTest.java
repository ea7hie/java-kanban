package managersTests.commonTests;

import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.service.exceptions.ManagerSaveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CommonTestOfFileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static final Path dirForSave = Paths.get("test/managersTests/commonTests/storage");
    private static final Path fileForSave = dirForSave.resolve("allTasks.txt");

    private static FileBackedTaskManager fm;
    private static InMemoryTaskManager inMemoryTaskManager;

    @BeforeAll
    public static void beforeAll() {
        if (!Files.exists(dirForSave)) {
            try {
                Files.createDirectory(dirForSave);
                Files.createFile(fileForSave);
                try (Writer writeNewStringInStorage = new FileWriter(fileForSave.toString(),
                        StandardCharsets.UTF_8, true)) {
                    writeNewStringInStorage.write("id,type,name,status,description,epic\n");
                } catch (IOException e) {
                    throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                            "Произошла ошибка сохранения...");
                }
            } catch (IOException e) {
                throw new ManagerSaveException("Приносим свои извинения, вы не должны были видеть это!" +
                        "Произошла ошибка создания хранилища...");
            }
            fm = new FileBackedTaskManager(fileForSave);
            inMemoryTaskManager = (InMemoryTaskManager) Managers.getDefaultTaskManager();
        }
    }

    @BeforeEach
    public void makeBeforeEach() {
        fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());
        inMemoryTaskManager = fm.getInMemoryTaskManager();

        fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                new Task("!!!1", "desc 1", 1, 35, "27.01.2004; 12:30")));
        fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                new Task("!!!2", "desc 2", 1, 15, "27.01.2024; 22:30")));
        fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                new Task("!!!3", "desc 3", 1, 3, "27.02.2025; 02:30")));

        fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("???1", "DESC 1", 1)));
        fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("???2", "DESC 2", 1)));
        fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("???3", "DESC 3", 1)));

        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---4", 1,
                4, 78, "05.03.2001; 18:48")));
        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---4", 1,
                4, 52, "17.02.2006; 17:02")));
        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---4", 1,
                4, 8, "03.12.2025; 02:03")));
        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---4", 1,
                4, 1458, "01.01.2025; 15:47")));

        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---1", "Desc---5", 1,
                5, 245, "28.01.2025; 19:35")));
        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---2", "Desc---5", 1,
                5, 17, "29.02.2024; 18:52")));
        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---3", "Desc---5", 1,
                5, 1, "15.06.2024; 23:03")));
        fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---4", "Desc---5", 1,
                5, 47, "19.01.2025; 12:36")));
    }

    @AfterEach
    public void afterEach() {
        fm.clearFile();
    }

    @Override
    @Test
    void shouldGetAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.add(inMemoryTaskManager.findTaskByID(1));
        allTasks.add(inMemoryTaskManager.findTaskByID(2));
        allTasks.add(inMemoryTaskManager.findTaskByID(3));

        assertArrayEquals(new ArrayList[]{allTasks}, new ArrayList[]{fm.getAllTasks()});
    }

    @Override
    @Test
    void shouldGetAllEpics() {
        ArrayList<Epic> allEpics = new ArrayList<>();
        allEpics.add(inMemoryTaskManager.findEpicByID(4));
        allEpics.add(inMemoryTaskManager.findEpicByID(5));
        allEpics.add(inMemoryTaskManager.findEpicByID(6));

        assertArrayEquals(new ArrayList[]{allEpics}, new ArrayList[]{fm.getAllEpics()});
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

        assertArrayEquals(new ArrayList[]{allSubtasks}, new ArrayList[]{fm.getAllSubtasks()});
    }

    @Override
    @Test
    void shouldRemoveAllTasks() {
        fm.removeAllTasks();
        assertTrue(fm.getAllTasks().isEmpty());
    }

    @Override
    @Test
    void shouldRemoveAllEpics() {
        fm.removeAllEpics();
        assertTrue(fm.getAllEpics().isEmpty()
                && fm.getAllSubtasks().isEmpty());
    }

    @Override
    @Test
    void shouldRemoveAllSubtasks() {
        fm.removeAllSubtasks();
        assertTrue(fm.getAllSubtasks().isEmpty());
    }

    @Override
    @Test
    void shouldDeleteOneTaskByID() {
        int sizeBeforeDelete = fm.getAllTasks().size();
        fm.deleteOneTaskByID(1);
        int sizeAfterDelete = fm.getAllTasks().size();
        assertTrue((sizeBeforeDelete == sizeAfterDelete + 1) && !fm.isTaskAddedByID(1));
    }

    @Override
    @Test
    void shouldDeleteOneEpicByID() {
        int sizeBeforeDelete = fm.getAllEpics().size();
        int countAllSubtasksBeforeDelete = fm.getAllSubtasks().size();
        int countSubtaskInEpic = fm.findEpicByID(4).getSubtasksIDs().size();

        fm.deleteOneEpicByID(4);
        int sizeAfterDelete = fm.getAllEpics().size();
        int countAllSubtasksAfterDelete = fm.getAllSubtasks().size();

        assertTrue((sizeBeforeDelete == sizeAfterDelete + 1) && !fm.isEpicAddedByID(4)
                && (countAllSubtasksBeforeDelete == countAllSubtasksAfterDelete + countSubtaskInEpic));
    }

    @Override
    @Test
    void shouldDeleteOneSubtaskByID() {
        Epic epic = fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("name", "desc", -1)));
        Subtask newSubtask = fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(new Subtask("---1",
                "Desc---4", 1, epic.getId(), 78, "05.03.2001; 18:48")));

        int sizeBeforeDelete = fm.getAllSubtasks().size();
        int countSubtaskInEpicBeforeDelete = epic.getSubtasksIDs().size();

        fm.deleteOneSubtaskskByID(newSubtask.getId());
        int sizeAfterDelete = fm.getAllSubtasks().size();
        int countSubtaskInEpiAfterDelete = epic.getSubtasksIDs().size();

        assertTrue((sizeBeforeDelete == sizeAfterDelete + 1) && !fm.isSubtaskAddedByID(newSubtask.getId())
                && (countSubtaskInEpicBeforeDelete == countSubtaskInEpiAfterDelete + 1));
    }

    @Override
    @Test
    void shouldSaveNewTask() {
        int sizeBeforeSaving = fm.getAllTasks().size();
        fm.saveNewTask(
                new Task("!!!15", "desc 15", 15, 35, "27.01.1974; 12:30"));
        int sizeAfterSaving = fm.getAllTasks().size();

        assertTrue((sizeBeforeSaving + 1 == sizeAfterSaving));
    }

    @Override
    @Test
    void shouldSaveNewEpic() {
        int sizeBeforeSaving = fm.getAllEpics().size();
        fm.saveNewEpic(new Epic("???15", "DESC 15", 15));
        int sizeAfterSaving = fm.getAllEpics().size();

        assertTrue((sizeBeforeSaving + 1 == sizeAfterSaving) && fm.isEpicAddedByID(15));
    }

    @Override
    @Test
    void shouldSaveNewSubtask() {
        int sizeBeforeSaving = fm.getAllSubtasks().size();
        fm.saveNewSubtask(new Subtask("---15", "Desc---15", 15,
                6, 78, "05.03.2091; 18:48"));
        int sizeAfterSaving = fm.getAllSubtasks().size();

        assertTrue((sizeBeforeSaving + 1 == sizeAfterSaving) && fm.isSubtaskAddedByID(15));
    }

    @Override
    @Test
    void shouldFindTaskByID() {
        Task task = fm.saveNewTask(
                new Task("!!!15", "desc 15", 15, 35, "27.01.1974; 12:30"));
        Task taskByID = fm.findTaskByID(task.getId());
        assertEquals(task, taskByID);
    }

    @Override
    @Test
    void shouldFindEpicByID() {
        Epic epic = fm.saveNewEpic(new Epic("???15", "DESC 15", 15));
        Epic epicByID = fm.findEpicByID(15);
        assertEquals(epic, epicByID);
    }

    @Override
    @Test
    void shouldFindSubtaskByID() {
        Subtask subtask = fm.saveNewSubtask(new Subtask("---15", "Desc---15", 15,
                6, 78, "05.03.2091; 18:48"));
        Subtask subtaskByID = fm.findSubtaskByID(15);
        assertEquals(subtask, subtaskByID);
    }

    @Override
    @Test
    void shouldUpdateTask() {
        Task newTask = fm.findTaskByID(1);
        newTask.setName("NEW NAME1");
        newTask.setDescription("NEW NAME1");
        Task updateTask = fm.updateTask(newTask);
        assertEquals(newTask, updateTask);
    }

    @Override
    @Test
    void shouldUpdateEpic() {
        Epic epic = fm.findEpicByID(5);
        epic.setName("NEW NAME5");
        epic.setDescription("NEW NAME5");
        Epic updatedEpic = fm.updateEpic(epic);
        assertEquals(epic, updatedEpic);
    }

    @Override
    @Test
    void shouldUpdateSubtask() {
        Epic epic = fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("name", "desc", -1)));
        Subtask newSubtask = fm.saveNewSubtask(new Subtask("---1", "Desc---4", 1,
                epic.getId(), 78, "05.03.2001; 18:48"));
        newSubtask.setName("/////");
        newSubtask.setDescription("/////");
        Subtask updatedSubtask = fm.updateSubtask(newSubtask);

        assertEquals(newSubtask, updatedSubtask);
    }
}
