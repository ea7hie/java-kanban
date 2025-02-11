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
import com.yandex.app.service.httpHandlers.ScheduleHandlerGetEpics;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpTaskServerGetEpicsTest {
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
        uri = URI.create("http://localhost:8080/schedule/epics");
        fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());
        inMemoryTaskManager = fm.getInMemoryTaskManager();
        hts = new HttpTaskServer();
        HttpTaskServer.httpServer.createContext("/schedule/epics",
                new ScheduleHandlerGetEpics(inMemoryTaskManager, fm));

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

    //Проверка /epics
    //epics GET - стандартная реализация - вывод всех эпиков: список с эпиками & список пуст
    @Test
    void shouldGetAllEpics() {
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

        StringBuilder messageWithAllEpics = new StringBuilder();
        for (Epic epic : inMemoryTaskManager.getAllEpics()) {
            messageWithAllEpics.append(epic.toString());
            messageWithAllEpics.append(inMemoryTaskManager.getFullDescOfAllSubtasksOfEpicById(epic.getId()));
        }

        String AllEpics = inMemoryTaskManager.getAllEpics().isEmpty() ? "\"Список эпиков пуст.\"" :
                messageWithAllEpics.toString();

        assertEquals(200, response.statusCode());
        assertEquals(AllEpics, response.body());

        inMemoryTaskManager.removeAllEpics();
        AllEpics = inMemoryTaskManager.getAllEpics().isEmpty() ? "\"Список эпиков пуст.\"" :
                messageWithAllEpics.toString();

        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(200, response.statusCode());
        assertEquals(AllEpics, response.body());
    }


    //epics POST - стандартная реализация - добавление
    @Test
    void shouldAddNewEpic() {
        int sizeBeforeAdding = inMemoryTaskManager.getAllEpics().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"5\",\"description\":\"5\",\"id\":-1}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterAdding = inMemoryTaskManager.getAllEpics().size();

        assertEquals(201, response.statusCode());
        assertEquals("Успешно сохранено!\n", response.body());
        assertEquals(sizeBeforeAdding + 1, sizeAfterAdding);
    }


    //epics POST - стандартная реализация - обновление
    @Test
    void shouldUpdateEpic4() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllEpics().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":4}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllEpics().size();

        assertEquals(200, response.statusCode());
        assertEquals("Успешно обновлено!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);

        assertEquals("NEW_NAME", inMemoryTaskManager.findEpicByID(4).getName());
        assertEquals("NEW_DESC", inMemoryTaskManager.findEpicByID(4).getDescription());
    }

    //epics POST - стандартная реализация & неправильное использование - нет эпика с таким id
    @Test
    void shouldNotUpdateEpic11() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllEpics().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":11}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllEpics().size();

        assertEquals(404, response.statusCode());
        assertEquals("Эпика с заданным id (11) не найдено.", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //epics POST - стандартная реализация & неправильное использование - неправильное тело - нет описания
    @Test
    void shouldNotUpdateEpic4WrongBody() {
        int sizeBeforeUpdating = inMemoryTaskManager.getAllEpics().size();

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"id\":4}"))
                .uri(uri)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        int sizeAfterUpdating = inMemoryTaskManager.getAllEpics().size();

        assertEquals(400, response.statusCode());
        assertEquals("Проверьте правильность введённых полей!", response.body());
        assertEquals(sizeAfterUpdating, sizeBeforeUpdating);
    }

    //epics POST - стандартная реализация & неправильное использование - неправильное тело - неправильный id
    @Test
    void shouldNotUpdateEpicWrongID() {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":\"***\"}"))
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

    //epics DELETE - неправильная реализация - ошибка длины URI - нет id в URI
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

    //epics PUT - неправильная реализация - ошибка метода - не предусмотрен данный метод
    @Test
    void shouldGetErrorMethod() {
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":1}"))
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

    //Проверка /epics/{id}
    //epics/4 GET - стандартная реализация
    @Test
    void shouldGetEpic4() {
        uri = URI.create("http://localhost:8080/schedule/epics/4");
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
        assertEquals(inMemoryTaskManager.findEpicByID(4).toString(), response.body());
    }

    //epics/11 GET - стандартная реализация - нет эпика с таким id
    @Test
    void shouldNotGetEpic11() {
        uri = URI.create("http://localhost:8080/schedule/epics/11");
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
        assertEquals("Эпика с заданным id (11) не найдено.", response.body());
    }

    //epics/*** GET - стандартная реализация & неправильное использование - id не число
    @Test
    void shouldNotGetEpicIdIsNaN() {
        uri = URI.create("http://localhost:8080/schedule/epics/***");
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

    //epics/1 POST - неправильная реализация - не предусмотрено
    @Test
    void shouldNotPostEpic() {
        uri = URI.create("http://localhost:8080/schedule/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"NEW_NAME\",\"description\":\"NEW_DESC\"," +
                        "\"id\":-1}"))
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

    //epics/4 DELETE - стандартная реализация
    @Test
    void shouldDeleteEpic4() {
        int sizeBeforeDelete = inMemoryTaskManager.getAllEpics().size();
        uri = URI.create("http://localhost:8080/schedule/epics/4");

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

        int sizeAfterDelete = inMemoryTaskManager.getAllEpics().size();

        assertEquals(200, response.statusCode());
        assertEquals("Успешно удалено!", response.body());
        assertEquals(sizeBeforeDelete - 1, sizeAfterDelete);
    }

    //epics/11 DELETE - стандартная реализация & неправильное использование - нет задачи с таким id
    @Test
    void shouldNotDeleteEpic11() {
        uri = URI.create("http://localhost:8080/schedule/epics/11");

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
        assertEquals("Эпика с заданным id (11) не найдено.", response.body());
    }

    //epics/ABC DELETE - неправильная реализация  - id не число
    @Test
    void shouldNotDeleteEpicABC() {
        uri = URI.create("http://localhost:8080/schedule/epics/ABC");

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

    //epics/1/1 DELETE - неправильная реализация  - неправильная длина URI
    @Test
    void shouldNotDeleteEpic1_1() {
        uri = URI.create("http://localhost:8080/schedule/epics/1/1");

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

    //epics/4/subtasks GET - стандартная реализация
    @Test
    void shouldGetEpicSubtasksOfEpic4() {
        uri = URI.create("http://localhost:8080/schedule/epics/4/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        StringBuilder message = new StringBuilder();
        message.append(inMemoryTaskManager.findEpicByID(4).toString());
        message.append(inMemoryTaskManager.findEpicByID(4).getSubtasksIDs().stream()
                .map(inMemoryTaskManager::findSubtaskByID)
                .map(Subtask::toString)
                .collect(Collectors.joining()));
        String subtasks = message.toString();

        HttpResponse<String> response;
        try {
            response = client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ServersException("Ошибка отправки/получения данных");
        }

        assertEquals(200, response.statusCode());
        assertEquals(subtasks, response.body());
    }

    //epics/11/subtasks GET - стандартная реализация & неправильное использование - нет эпика с таким id
    @Test
    void shouldNotGetEpicSubtasksOfEpic11() {
        uri = URI.create("http://localhost:8080/schedule/epics/11/subtasks");

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
        assertEquals("Эпика с заданным id (11) не найдено.", response.body());
    }
}
