package com.yandex.app.service.httpHandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Progress;
import com.yandex.app.model.Task;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.interfaces.TaskManager;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ScheduleHandlerGetTasks extends BaseHttpHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final Gson gson;
    private final TaskManager tm;

    public ScheduleHandlerGetTasks(TaskManager taskManager) {
        this.gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();
        this.tm = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws ServerException {
        try {
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

                        if (tm.findTaskByID(index) != null) {
                            sendText(httpExchange, tm.findTaskByID(index).toString());
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
                        if (tm.isTimeOverlap(startTime, duration)) {
                            sendHasInteractions(httpExchange);
                            return;
                        }

                       tm.saveNewTask(new Task(name, description, temporaryID, duration, startTime));
                        sendTextCreated(httpExchange);
                    } else if (tm.findTaskByID(id) != null) {
                        Task t = new Task(name, description, temporaryID, duration, startTime);
                        t.setId(id);
                        t.setStatus(progress);
                        tm.updateTask(t);
                        sendText(httpExchange, "Успешно обновлено!");
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

                    if (tm.findTaskByID(idForDelete) != null) {
                        tm.deleteOneTaskByID(idForDelete);
                        sendText(httpExchange, "Успешно удалено!");
                    } else {
                        sendNotFound(httpExchange, String.format("Задачи с заданным id (%d) не найдено.", idForDelete));
                    }
                    break;
                default:
                    sendTextErrorMethod(httpExchange);
            }
        } catch (Exception e) {
            sendTextErrorServer(httpExchange);
            throw new ServersException("Ошибка в работе сервера при работе с эпиками");
        }
    }

    private String printAllTasks() {
        if (tm.getAllTasks().isEmpty()) {
            return gson.toJson("Список задач пуст.");
        }
        return tm.getAllTasks().stream()
                .map(Task::toString)
                .collect(Collectors.joining());
    }
}