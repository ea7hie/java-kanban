package HttpServerTests;

import com.yandex.app.HttpTaskServer;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.service.exceptions.ManagerSaveException;
import com.yandex.app.service.exceptions.ServersException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerGetPrioritizedTest {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpTaskServer hts;
    private static final URI uri = URI.create("http://localhost:8080/schedule/prioritized");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static InMemoryTaskManager inMemoryTaskManager;

    @BeforeAll
    static void beforeAll() {
        Path dirForSave = Paths.get("test/HttpServerTests/storage");
        Path fileForSave = dirForSave.resolve("allTasks.csv");

        FileBackedTaskManager fm;
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

        hts = new HttpTaskServer(fm);
    }

    @AfterEach
    void stop() {
        hts.stop();
    }

    @Test
    void shouldGetPrioritizedTask() {
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
        assertEquals(inMemoryTaskManager.getPrioritizedTasks(), response.body());
    }
}