package HttpServerTests;

import com.yandex.app.HttpTaskServer;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.model.Task;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.httpHandlers.ScheduleHandlerGetHistory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerGetHistoryTest {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final URI uri = URI.create("http://localhost:8080/schedule/history");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static InMemoryTaskManager inMemoryTaskManager;

    @BeforeAll
    static void beforeAll() {
        inMemoryTaskManager = new InMemoryTaskManager();

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

        Task task1 = inMemoryTaskManager.findTaskByID(1);
        Epic epic4 = inMemoryTaskManager.findEpicByID(4);
        Subtask subtask13 = inMemoryTaskManager.findSubtaskByID(13);

        HttpTaskServer hts = new HttpTaskServer();
        HttpTaskServer.httpServer.createContext("/schedule/history",
                new ScheduleHandlerGetHistory(inMemoryTaskManager));

        HttpTaskServer.start();
    }

    @AfterAll
    static void stop() {
        HttpTaskServer.stop();
    }

    @Test
    void shouldGetHistory() {
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
        assertEquals(inMemoryTaskManager.showListViewedTasks(), response.body());
    }
}