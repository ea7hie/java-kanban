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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerGetSubtasksTest {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static final Path dirForSave = Paths.get("test/HttpServerTests/storage");
    private static final Path fileForSave = dirForSave.resolve("allTasks.csv");

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
        uri = URI.create("http://localhost:8080/schedule/subtasks");
        fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());
        inMemoryTaskManager = fm.getInMemoryTaskManager();
        hts = new HttpTaskServer(inMemoryTaskManager);

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
        hts.stop();
    }

    //Проверка /subtasks
    //subtasks GET - стандартная реализация - вывод всех подзадач: список с подзадачами & список пуст
    @Test
    void shouldGetAllSubtasks() {
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

        String allSubtasks = inMemoryTaskManager.getAllSubtasks().isEmpty() ? "\"Список подзадач пуст.\"" :
                inMemoryTaskManager.getAllSubtasks().stream()
                        .map(Subtask::toString)
                        .collect(Collectors.joining());

        assertEquals(200, response.statusCode());
        assertEquals(allSubtasks, response.body());

        inMemoryTaskManager.removeAllEpics();
        allSubtasks = inMemoryTaskManager.getAllSubtasks().isEmpty() ? "\"Список подзадач пуст.\"" :
                inMemoryTaskManager.getAllSubtasks().stream()
                        .map(Subtask::toString)
                        .collect(Collectors.joining());

        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(200, response.statusCode());
        assertEquals(allSubtasks, response.body());
    }

    //subtasks POST - стандартная реализация - добавление
    @Test
    void shouldAddNewSubtask() {
        int sizeBeforeAdding = inMemoryTaskManager.getAllSubtasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"5\",\"description\":\"5\",\"id\":-1," +
                        "\"idOfSubtaskEpic\": 4,\"status\": \"NEW\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterAdding = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(201, response.statusCode());
        assertEquals("Успешно сохранено!\n", response.body());
        assertEquals(sizeBeforeAdding + 1, sizeAfterAdding);
    }

    //subtasks POST - стандартная реализация - обновление
    @Test
    void shouldUpdateSubtask11() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllSubtasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":11,\"idOfSubtaskEpic\": 5,\"status\": \"DONE\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(200, response.statusCode());
        assertEquals("Успешно обновлено!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);

        assertEquals("NEW_NAME", inMemoryTaskManager.findSubtaskByID(11).getName());
        assertEquals("NEW_DESC", inMemoryTaskManager.findSubtaskByID(11).getDescription());
        assertEquals("DONE", inMemoryTaskManager.findSubtaskByID(11).getStatus().toString());
        assertEquals("IN_PROGRESS", inMemoryTaskManager.findEpicByID(5).getStatus().toString());
    }

    //subtasks POST - стандартная реализация & неправильное использование - пересечение
    @Test
    void shouldNotAddSubtask() {
        int sizeBeforeAdding = inMemoryTaskManager.getAllSubtasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":-1,\"idOfSubtaskEpic\": 5,\"status\":\"DONE\",\"duration\": 5," +
                        "\"startTime\":\"27.01.2004; 12:30\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterAdding = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(406, response.statusCode());
        assertEquals("Ошибка сохранения! На это время у вас запланировано кое-что другое!\n", response.body());
        assertEquals(sizeAfterAdding, sizeBeforeAdding);
    }

    //subtasks POST - стандартная реализация & неправильное использование - нет подзадачи с таким id
    @Test
    void shouldNotUpdateSubtask111() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllSubtasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":111,\"idOfSubtaskEpic\": 5,\"status\": \"DONE\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(404, response.statusCode());
        assertEquals("Подзадачи с заданным id (111) не найдено.", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //subtasks POST - стандартная реализация & неправильное использование - нет эпика с таким id
    @Test
    void shouldNotUpdateSubtask11Epic7() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllSubtasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":11,\"idOfSubtaskEpic\": 7,\"status\": \"DONE\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(404, response.statusCode());
        assertEquals("Нет эпика с таким id (7)", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //subtasks POST - стандартная реализация & неправильное использование - неправильное тело
    @Test
    void shouldNotUpdateSubtask11WrongBody() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllSubtasks().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"id\":11}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность введённых полей!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //subtasks POST - стандартная реализация & неправильное использование - неправильное тело - неправильный id
    @Test
    void shouldNotUpdateSubtaskWrongID() {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":\"***\",\"idOfSubtaskEpic\": 5,\"status\": \"DONE\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
                .uri(uri)
                .build();

        assertDoesNotThrow(() -> {
            try {
                HttpResponse<String> response = client.send(request, handler);
            } catch (IOException | InterruptedException e) {
                throw new ServersException("Ошибка отправки/получения данных");
            }
        });
    }

    //subtasks DELETE - неправильная реализация - ошибка длины URI - нет id в URI
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

    //subtasks PUT - неправильная реализация - ошибка метода - не предусмотрен данный метод
    @Test
    void shouldGetErrorMethod() {
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":11,\"idOfSubtaskEpic\": 5,\"status\": \"DONE\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
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

    //Проверка /subtasks/{id}
    //subtasks/11 GET - стандартная реализация
    @Test
    void shouldGetSubtask11() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/11");
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
        assertEquals(inMemoryTaskManager.findSubtaskByID(11).toString(), response.body());
    }

    //subtasks/111 GET - стандартная реализация - нет эпика с таким id
    @Test
    void shouldNotGetSubtask111() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/111");
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
        assertEquals("Подзадачи с заданным id (111) не найдено.", response.body());
    }

    //subtasks/*** GET - стандартная реализация & неправильное использование - id не число
    @Test
    void shouldNotGetEpicIdIsNaN() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/***");
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

    //subtasks/1 POST - неправильная реализация - не предусмотрено
    @Test
    void shouldNotPostEpic() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":11,\"idOfSubtaskEpic\": 5,\"status\": \"DONE\",\"duration\": 5," +
                        "\"startTime\": \"11.10.2023; 12:23\"}"))
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

    //subtasks/11 DELETE - стандартная реализация
    @Test
    void shouldDeleteSubtask11() {
        int sizeBeforeDelete = inMemoryTaskManager.getAllSubtasks().size();
        uri = URI.create("http://localhost:8080/schedule/subtasks/11");

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

        int sizeAfterDelete = inMemoryTaskManager.getAllSubtasks().size();

        assertEquals(200, response.statusCode());
        assertEquals("Успешно удалено!", response.body());
        assertEquals(sizeBeforeDelete - 1, sizeAfterDelete);
    }

    //subtasks/111 DELETE - стандартная реализация & неправильное использование - нет подзадачи с таким id
    @Test
    void shouldNotDeleteSubtask111() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/111");

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
        assertEquals("Подзадачи с заданным id (111) не найдено.", response.body());
    }

    //subtasks/ABC DELETE - неправильная реализация  - id не число
    @Test
    void shouldNotDeleteSubtaskABC() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/ABC");

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

    //subtasks/1/1 DELETE - неправильная реализация  - неправильная длина URI
    @Test
    void shouldNotDeleteSubtask1_1() {
        uri = URI.create("http://localhost:8080/schedule/subtasks/1/1");

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