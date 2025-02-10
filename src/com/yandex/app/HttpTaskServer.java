package com.yandex.app;

import com.sun.net.httpserver.HttpServer;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.Managers;
import com.yandex.app.service.exceptions.ManagerSaveException;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.httpHandlers.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpTaskServer {
    private static HttpServer httpServer;
    private static InMemoryTaskManager inMemoryTaskManager;

    public static void main(String[] args) {
        Path dirForSave = Paths.get("src/com/yandex/app/service/storage");
        Path fileForSave = dirForSave.resolve("allTasks.txt");

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

        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new ServersException("Приносим свои извинения, вы не должны были видеть это!\n" +
                    "Произошла ошибка создания сервера...");
        }

        httpServer.createContext("/schedule/prioritized", new ScheduleHandlerGetPrioritized(inMemoryTaskManager));
        httpServer.createContext("/schedule/history", new ScheduleHandlerGetHistory(inMemoryTaskManager));
        httpServer.createContext("/schedule/tasks", new ScheduleHandlerGetTasks(inMemoryTaskManager, fm));
        httpServer.createContext("/schedule/subtasks", new ScheduleHandlerGetSubtasks(inMemoryTaskManager, fm));
        httpServer.createContext("/schedule/epics", new ScheduleHandlerGetEpics(inMemoryTaskManager, fm));

        httpServer.start();
    }
}