package managersTests.commonTests;

import com.yandex.app.service.interfaces.TaskManager;
import org.junit.jupiter.api.Test;

abstract class TaskManagerTest<T extends TaskManager> {
    @Test
    abstract void shouldGetAllTasks();

    @Test
    abstract void shouldGetAllEpics();

    @Test
    abstract void shouldGetAllSubtasks();

    @Test
    abstract void shouldRemoveAllTasks();

    @Test
    abstract void shouldRemoveAllEpics();

    @Test
    abstract void shouldRemoveAllSubtasks();

    @Test
    abstract void shouldDeleteOneTaskByID();

    @Test
    abstract void shouldDeleteOneEpicByID();

    @Test
    abstract void shouldDeleteOneSubtaskByID();

    @Test
    abstract void shouldSaveNewTask();

    @Test
    abstract void shouldSaveNewEpic();

    @Test
    abstract void shouldSaveNewSubtask();

    @Test
    abstract void shouldFindTaskByID();

    @Test
    abstract void shouldFindEpicByID();

    @Test
    abstract void shouldFindSubtaskByID();

    @Test
    abstract void shouldUpdateTask();

    @Test
    abstract void shouldUpdateEpic();

    @Test
    abstract void shouldUpdateSubtask();
}