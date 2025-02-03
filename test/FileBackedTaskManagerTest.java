import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.exceptions.ManagerSaveException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBackedTaskManagerTest {
    private static final Path dirForSave = Paths.get("java-kanban\\src\\com\\yandex\\app\\service\\storage");
    private static final Path
            fileForSave = dirForSave.resolve("allTasks.txt");
    private static FileBackedTaskManager fm;

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
        } else {
            fm = FileBackedTaskManager.loadFromFile(fileForSave.toFile());
        }
    }

    @Test
    public void testException() {
        Assertions.assertDoesNotThrow(() -> {
            fm.clearFile();
        });
    }
}