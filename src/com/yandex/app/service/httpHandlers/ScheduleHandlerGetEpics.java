package com.yandex.app.service.httpHandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Epic;
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

public class ScheduleHandlerGetEpics extends BaseHttpHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final InMemoryTaskManager inMemoryTaskManager;
    private final FileBackedTaskManager fm;
    private final Gson gson;

    public ScheduleHandlerGetEpics(InMemoryTaskManager inMemoryTaskManager, FileBackedTaskManager fm) {
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

        if (lengthOfPath == 5 && path.get(4).equals("subtasks")) {
            int index;
            try {
                index = Integer.parseInt(path.get(3));
            } catch (NumberFormatException e) {
                sendTextErrorIsNaN(httpExchange);
                return;
            }

            if (inMemoryTaskManager.isEpicAddedByID(index)) {
                sendText(httpExchange, printOneEpicWithAllItsSubtasks(index));
            } else {
                sendNotFound(httpExchange, String.format("Эпика с заданным id (%d) не найдено.", index));
            }

            return;
        }

        if (lengthOfPath > 4) {
            sendTextErrorURLLength(httpExchange);
            return;
        }

        switch (method) {
            case "GET":
                if (lengthOfPath == 3) {
                    sendText(httpExchange, printAllEpics());
                } else {
                    int index;
                    try {
                        index = Integer.parseInt(path.get(3));
                    } catch (NumberFormatException e) {
                        sendTextErrorIsNaN(httpExchange);
                        return;
                    }

                    if (inMemoryTaskManager.isEpicAddedByID(index)) {
                        sendText(httpExchange, inMemoryTaskManager.findEpicByID(index).toString());
                    } else {
                        sendNotFound(httpExchange, String.format("Эпика с заданным id (%d) не найдено.", index));
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

                try {
                    name = Objects.requireNonNull(jsonObject.get("name").getAsString());
                    description = Objects.requireNonNull(jsonObject.get("description").getAsString());
                    id = jsonObject.get("id").getAsInt();
                } catch (NullPointerException | IllegalStateException | ClassCastException e) {
                    sendTextErrorInvalidValues(httpExchange);
                    return;
                }

                if (id == temporaryID) {
                    fm.saveNewEpic(inMemoryTaskManager.saveNewEpic(new Epic(name, description, temporaryID)));
                    sendTextCreated(httpExchange);
                } else if (inMemoryTaskManager.isEpicAddedByID(id)) {
                    Epic epic = inMemoryTaskManager.findEpicByID(id);
                    epic.setName(name);
                    epic.setDescription(description);
                    fm.updateEpic(inMemoryTaskManager.updateEpic(epic));
                    sendText(httpExchange, "Успешно обновлено!");
                } else {
                    sendNotFound(httpExchange, String.format("Эпика с заданным id (%d) не найдено.", id));
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

                if (inMemoryTaskManager.isEpicAddedByID(idForDelete)) {
                    inMemoryTaskManager.deleteOneEpicByID(idForDelete);
                    sendText(httpExchange, "Успешно удалено!");
                } else {
                    sendNotFound(httpExchange, String.format("Эпика с заданным id (%d) не найдено.", idForDelete));
                }
                break;
            default:
                sendTextErrorMethod(httpExchange);
        }
    }

    private String printAllEpics() {
        if (inMemoryTaskManager.getAllEpics().isEmpty()) {
            return gson.toJson("Список эпиков пуст.");
        }

        StringBuilder messageWithAllEpics = new StringBuilder();
        for (Epic epic : inMemoryTaskManager.getAllEpics()) {
            messageWithAllEpics.append(epic.toString());
            messageWithAllEpics.append(inMemoryTaskManager.getFullDescOfAllSubtasksOfEpicById(epic.getId()));
        }
        return messageWithAllEpics.toString();
    }

    private String printOneEpicWithAllItsSubtasks(int id) {
        StringBuilder message = new StringBuilder();
        message.append(inMemoryTaskManager.findEpicByID(id).toString());
        String subtasks = inMemoryTaskManager.findEpicByID(id).getSubtasksIDs().stream()
                .map(inMemoryTaskManager::findSubtaskByID)
                .map(Subtask::toString)
                .collect(Collectors.joining());

        return message.append(subtasks).toString();
    }
}