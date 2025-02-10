package com.yandex.app.service.httpHandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Task;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScheduleHandlerGetTasks extends BaseHttpHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final InMemoryTaskManager inMemoryTaskManager;
    private final FileBackedTaskManager fm;
    private final Gson gson;

    public ScheduleHandlerGetTasks(InMemoryTaskManager inMemoryTaskManager, FileBackedTaskManager fm) {
        this.inMemoryTaskManager = inMemoryTaskManager;
        this.fm = fm;
        this.gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        List<String> path = Arrays.asList(httpExchange.getRequestURI().getPath().split("/"));
        int lengthOfPath = path.size();

        if (lengthOfPath > 4) {
            sendTextErrorURLLength(httpExchange);
            return;
        }

        switch (method) {
            case "GET":
                if (lengthOfPath == 3) {
                    sendText(httpExchange, printAllTasks());
                } else {
                    int index;
                    try {
                        index = Integer.parseInt(path.get(3));
                    } catch (NumberFormatException e) {
                        sendTextErrorIsNaN(httpExchange);
                        return;
                    }

                    if (inMemoryTaskManager.isTaskAddedByID(index)) {
                        sendText(httpExchange, inMemoryTaskManager.findTaskByID(index).toString());
                    } else {
                        sendNotFound(httpExchange, String.format("Задачи с заданным id (%d) не найдено.", index));
                    }
                }
                break;
            case "POST":
                if (lengthOfPath != 3) {
                    sendTextErrorURLLength(httpExchange);
                    return;
                }

                String body = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                JsonElement jsonElement = JsonParser.parseString(body);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                int temporaryID = -1;

                String name;
                String description;
                int id;
                int duration;
                String startTime;
                String status;

                try {
                    name = Objects.requireNonNull(jsonObject.get("name").getAsString());
                    description = Objects.requireNonNull(jsonObject.get("description").getAsString());
                    id = jsonObject.get("id").getAsInt();
                    duration = jsonObject.get("duration").getAsInt();
                    startTime = Objects.requireNonNull(jsonObject.get("startTime").getAsString());
                    status = Objects.requireNonNull(jsonObject.get("status").getAsString());
                } catch (NullPointerException | IllegalStateException | ClassCastException e) {
                    sendTextErrorInvalidValues(httpExchange);
                    return;
                }

                Progress progress;
                try {
                    progress = Progress.valueOf(status);
                } catch (IllegalArgumentException e) {
                    sendTextErrorInValueOfProgress(httpExchange);
                    return;
                }

                if (id == temporaryID) {
                    if (inMemoryTaskManager.isTimeOverlap(startTime, duration)) {
                        sendHasInteractions(httpExchange);
                        return;
                    }

                    fm.saveNewTask(inMemoryTaskManager.saveNewTask(
                            new Task(name, description, temporaryID, duration, startTime))
                    );
                    sendTextCreated(httpExchange);
                } else if (inMemoryTaskManager.isTaskAddedByID(id)) {
                    Task t = new Task(name, description, temporaryID, duration, startTime);
                    t.setId(id);
                    t.setStatus(progress);
                    fm.updateTask(inMemoryTaskManager.updateTask(t));
                    sendText(httpExchange, "Успешно обновлено");
                } else {
                    sendNotFound(httpExchange, String.format("Задачи с заданным id (%d) не найдено.", id));
                }
                break;
            case "DELETE":
                if (lengthOfPath != 4) {
                    sendTextErrorURLLength(httpExchange);
                    return;
                }

                int idForDelete;
                try {
                    idForDelete = Integer.parseInt(path.get(3));
                } catch (NumberFormatException e) {
                    sendTextErrorIsNaN(httpExchange);
                    return;
                }

                if (inMemoryTaskManager.isTaskAddedByID(idForDelete)) {
                    inMemoryTaskManager.deleteOneTaskByID(idForDelete);
                    fm.deleteOneTaskByID(idForDelete);
                    sendText(httpExchange, "Успешно удалено!");
                } else {
                    sendNotFound(httpExchange, String.format("Задачи с заданным id (%d) не найдено.", idForDelete));
                }
                break;
            default:
                sendTextErrorMethod(httpExchange);
        }
    }

    private String printAllTasks() {
        if (inMemoryTaskManager.getAllTasks().isEmpty()) {
            return gson.toJson("Список задач пуст.");
        }
        return inMemoryTaskManager.getAllTasks().stream()
                .map(Task::toString)
                .collect(Collectors.joining());
    }
}