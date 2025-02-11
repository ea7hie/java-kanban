package HttpServerTests;

import com.yandex.app.HttpTaskServer;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.service.exceptions.ManagerSaveException;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.httpHandlers.ScheduleHandlerGetTasks;
import org.junit.jupiter.api.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpTaskServerGetTasksTest {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static final Path dirForSave = Paths.get("test/HttpServerTests/storage");
    private static final Path fileForSave = dirForSave.resolve("allTasks.txt");

    private static InMemoryTaskManager inMemoryTaskManager;
    private static FileBackedTaskManager fm;
    private static URI uri;
    private static HttpTaskServer hts;

    @BeforeAll
    static void beforeAll() {
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
        } else {
            fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());
            inMemoryTaskManager = fm.getInMemoryTaskManager();
        }
    }

    @BeforeEach
    void write() {
        uri = URI.create("http://localhost:8080/schedule/tasks");
        fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());
        inMemoryTaskManager = fm.getInMemoryTaskManager();
        hts = new HttpTaskServer();
        HttpTaskServer.httpServer.createContext("/schedule/tasks",
                new ScheduleHandlerGetTasks(inMemoryTaskManager, fm));

        HttpTaskServer.start();

        fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                new Task("!!!1", "desc 1", 1, 35, "27.01.2004; 12:30")));
        fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                new Task("!!!2", "desc 2", 1, 15, "27.01.2024; 22:30")));
        fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                new Task("!!!3", "desc 3", 1, 3, "27.02.2025; 02:30")));

        fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("???1", "DESC 1", 1)));
        fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic("???2", "DESC 2", 1)));

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
    void clear() {
        fm.clearFile();
        HttpTaskServer.stop();
    }

    //Проверка /tasks
    //tasks GET - стандартная реализация - вывод всех задач: список с задачами & список пуст
    @Test
    void shouldGetAllTasks() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        String allTasks = inMemoryTaskManager.getAllTasks().isEmpty() ? "\"Список задач пуст.\"" :
                inMemoryTaskManager.getAllTasks().stream()
                        .map(Task::toString)
                        .collect(Collectors.joining());

        assertEquals(200, response.statusCode());
        assertEquals(allTasks, response.body());

        inMemoryTaskManager.removeAllTasks();
        allTasks = inMemoryTaskManager.getAllTasks().isEmpty() ? "\"Список задач пуст.\"" :
                inMemoryTaskManager.getAllTasks().stream()
                        .map(Task::toString)
                        .collect(Collectors.joining());

        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(200, response.statusCode());
        assertEquals(allTasks, response.body());
    }

    //tasks POST - стандартная реализация - добавление
    @Test
    void shouldAddNewTask() {
        int sizeBeforeAdding = inMemoryTaskManager.getAllTasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"5\",\"description\":\"5\",\"id\":-1," +
                        "\"status\":\"NEW\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterAdding = inMemoryTaskManager.getAllTasks().size();

        assertEquals(201, response.statusCode());
        assertEquals("Успешно сохранено!\n", response.body());
        assertEquals(sizeBeforeAdding + 1, sizeAfterAdding);
    }

    //tasks POST - стандартная реализация & неправильное использование - пересечение
    @Test
    void shouldNotAddTask() {
        int sizeBeforeAdding = inMemoryTaskManager.getAllTasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":-1,\"status\":\"NEW\",\"duration\":15,\"startTime\":\"27.01.2004; 12:30\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterAdding = inMemoryTaskManager.getAllTasks().size();

        assertEquals(406, response.statusCode());
        assertEquals("Ошибка сохранения! На это время у вас запланировано кое-что другое!\n", response.body());
        assertEquals(sizeAfterAdding, sizeBeforeAdding);
    }

    //tasks POST - стандартная реализация - обновление
    @Test
    void shouldUpdateTask1() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllTasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":1,\"status\":\"DONE\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllTasks().size();

        assertEquals(200, response.statusCode());
        assertEquals("Успешно обновлено!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);

        assertEquals("DONE", inMemoryTaskManager.findTaskByID(1).getStatus().toString());
        assertEquals("NEW_NAME", inMemoryTaskManager.findTaskByID(1).getName());
        assertEquals("NEW_DESC", inMemoryTaskManager.findTaskByID(1).getDescription());
    }

    //tasks POST - стандартная реализация & неправильное использование - нет задачи с таким id
    @Test
    void shouldNotUpdateTask11() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllTasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":11,\"status\":\"DONE\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllTasks().size();

        assertEquals(404, response.statusCode());
        assertEquals("Задачи с заданным id (11) не найдено.", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //tasks POST - стандартная реализация & неправильное использование - неправильное тело - нет статуса прогресса
    @Test
    void shouldNotUpdateTask1WrongBody() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllTasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":1,\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllTasks().size();

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность введённых полей!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //tasks POST - стандартная реализация & неправильное использование - неправильное тело - неправильный статуса прогресса
    @Test
    void shouldNotUpdateTask1WrongProgress() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllTasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":1,\"status\":\"ERROR\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllTasks().size();

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность введённого статуса прогресса!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //tasks POST - стандартная реализация & неправильное использование - неправильное тело - неправильный id
    @Test
    void shouldNotUpdateTask1WrongID() {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":\"***\",\"status\":\"NEW\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        assertThrows(ServersException.class, () -> {
            try {
                HttpResponse<String> response = client.send(request, handler);
            } catch (IOException | InterruptedException e) {
                throw new ServersException("Ошибка отправки/получения данных");
            }
        });
    }

    //tasks DELETE - неправильная реализация - ошибка длины URI - нет id в URI
    @Test
    void shouldGetErrorUrlLength() {
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(414, response.statusCode());
        assertEquals("Проверьте правильность URL!", response.body());
    }

    //tasks PUT - неправильная реализация - ошибка метода - не предусмотрен данный метод
    @Test
    void shouldGetErrorMethod() {
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":1,\"status\":\"DONE\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность выбранного метода!", response.body());
    }

    //Проверка /tasks/{id}
    //tasks/1 GET - стандартная реализация
    @Test
    void shouldGetTask1() {
        uri = URI.create("http://localhost:8080/schedule/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(200, response.statusCode());
        assertEquals(inMemoryTaskManager.findTaskByID(1).toString(), response.body());
    }

    //tasks/11 GET - стандартная реализация - нет задачи с таким id
    @Test
    void shouldNotGetTask11() {
        uri = URI.create("http://localhost:8080/schedule/tasks/11");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(404, response.statusCode());
        assertEquals("Задачи с заданным id (11) не найдено.", response.body());
    }

    //tasks/*** GET - стандартная реализация & неправильное использование - id не число
    @Test
    void shouldNotGetTaskIdIsNaN() {
        uri = URI.create("http://localhost:8080/schedule/tasks/***");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность введённого id! (Введено не число!)", response.body());
    }

    //tasks/1 POST - неправильная реализация - не предусмотрено
    @Test
    void shouldNotPostTask() {
        uri = URI.create("http://localhost:8080/schedule/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":-1,\"status\":\"DONE\",\"duration\":5,\"startTime\":\"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(414, response.statusCode());
        assertEquals("Проверьте правильность URL!", response.body());
    }

    //tasks/1 DELETE - стандартная реализация
    @Test
    void shouldDeleteTask1() {
        int sizeBeforeDelete = inMemoryTaskManager.getAllTasks().size();
        uri = URI.create("http://localhost:8080/schedule/tasks/1");

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterDelete = inMemoryTaskManager.getAllTasks().size();

        assertEquals(200, response.statusCode());
        assertEquals("Успешно удалено!", response.body());
        assertEquals(sizeBeforeDelete - 1, sizeAfterDelete);
    }

    //tasks/11 DELETE - стандартная реализация & неправильное использование - нет задачи с таким id
    @Test
    void shouldNotDeleteTask11() {
        uri = URI.create("http://localhost:8080/schedule/tasks/11");

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(404, response.statusCode());
        assertEquals("Задачи с заданным id (11) не найдено.", response.body());
    }

    //tasks/ABC DELETE - неправильная реализация  - id не число
    @Test
    void shouldNotDeleteTaskABC() {
        uri = URI.create("http://localhost:8080/schedule/tasks/ABC");

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность введённого id! (Введено не число!)", response.body());
    }

    //tasks/1/1 DELETE - неправильная реализация  - неправильная длина URI
    @Test
    void shouldNotDeleteTask1_1() {
        uri = URI.create("http://localhost:8080/schedule/tasks/1/1");

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(414, response.statusCode());
        assertEquals("Проверьте правильность URL!", response.body());
    }
}