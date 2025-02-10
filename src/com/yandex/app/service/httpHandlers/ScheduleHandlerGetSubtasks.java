package com.yandex.app.service.httpHandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Subtask;
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

public class ScheduleHandlerGetSubtasks extends BaseHttpHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final InMemoryTaskManager inMemoryTaskManager;
    private final FileBackedTaskManager fm;
    private final Gson gson;

    public ScheduleHandlerGetSubtasks(InMemoryTaskManager inMemoryTaskManager, FileBackedTaskManager fm) {
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
                    sendText(httpExchange, printAllSubtasks());
                } else {
                    int index;
                    try {
                        index = Integer.parseInt(path.get(3));
                    } catch (NumberFormatException e) {
                        sendTextErrorIsNaN(httpExchange);
                        return;
                    }

                    if (inMemoryTaskManager.isSubtaskAddedByID(index)) {
                        sendText(httpExchange, inMemoryTaskManager.findSubtaskByID(index).toString());
                    } else {
                        sendNotFound(httpExchange, String.format("Подзадачи с заданным id (%d) не найдено.", index));
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
                int idOfSubtaskEpic;
                int duration;
                String startTime;
                String status;

                try {
                    name = Objects.requireNonNull(jsonObject.get("name").getAsString());
                    description = Objects.requireNonNull(jsonObject.get("description").getAsString());
                    id = jsonObject.get("id").getAsInt();
                    idOfSubtaskEpic = jsonObject.get("idOfSubtaskEpic").getAsInt();
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

                if (!inMemoryTaskManager.isEpicAddedByID(idOfSubtaskEpic)) {
                    sendNotFound(httpExchange, String.format("Нет эпика с таким id (%d)", idOfSubtaskEpic));
                    return;
                }

                if (id == temporaryID) {
                    if (inMemoryTaskManager.isTimeOverlap(startTime, duration)) {
                        sendHasInteractions(httpExchange);
                        break;
                    }

                    Subtask newSubtask = new Subtask(
                            name, description, temporaryID, idOfSubtaskEpic, duration, startTime);
                    fm.saveNewSubtask(inMemoryTaskManager.saveNewSubtask(newSubtask));
                    sendTextCreated(httpExchange);
                } else if (inMemoryTaskManager.isSubtaskAddedByID(id)) {
                    Subtask t = new Subtask(name, description, temporaryID, idOfSubtaskEpic, duration, startTime);
                    t.setId(id);
                    t.setStatus(progress);
                    fm.updateSubtask(inMemoryTaskManager.updateSubtask(t));
                    sendText(httpExchange, "Успешно обновлено");
                } else {
                    sendNotFound(httpExchange, String.format("Подзадачи с заданным id (%d) не найдено.", id));
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

                if (inMemoryTaskManager.isSubtaskAddedByID(idForDelete)) {
                    fm.deleteOneSubtaskskByID(idForDelete);
                    sendText(httpExchange, "Успешно удалено!");
                } else {
                    sendNotFound(httpExchange, String.format("Подзадачи с заданным id (%d) не найдено.", idForDelete));
                }
                break;
            default:
                sendTextErrorMethod(httpExchange);
        }
    }

    private String printAllSubtasks() {
        if (inMemoryTaskManager.getAllSubtasks().isEmpty()) {
            return gson.toJson("Список подзадач пуст.");
        }
        return inMemoryTaskManager.getAllSubtasks().stream()
                .map(Subtask::toString)
                .collect(Collectors.joining());
    }
}