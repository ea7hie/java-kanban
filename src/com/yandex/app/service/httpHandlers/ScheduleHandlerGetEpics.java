package com.yandex.app.service.httpHandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.model.Epic;
import com.yandex.app.model.Subtask;
import com.yandex.app.service.comparators.SubtaskStartTimeComparator;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.interfaces.TaskManager;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.yandex.app.model.Task.NEW_LINE;

public class ScheduleHandlerGetEpics extends BaseHttpHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final Gson gson;
    private final TaskManager tm;

    public ScheduleHandlerGetEpics(TaskManager taskManager) {
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

            if (lengthOfPath == 5 && path.get(4).equals("subtasks")) {
                int index;
                try {
                    index = Integer.parseInt(path.get(3));
                } catch (NumberFormatException e) {
                    sendTextErrorIsNaN(httpExchange);
                    return;
                }

                if (tm.findEpicByID(index) != null) {
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

                        if (tm.findEpicByID(index) != null) {
                            sendText(httpExchange, tm.findEpicByID(index).toString());
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
                        tm.saveNewEpic(new Epic(name, description, temporaryID));
                        sendTextCreated(httpExchange);
                    } else if (tm.findEpicByID(id) != null) {
                        Epic epic = tm.findEpicByID(id);
                        epic.setName(name);
                        epic.setDescription(description);
                        tm.updateEpic(epic);
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

                    if (tm.findEpicByID(idForDelete) != null) {
                        tm.deleteOneEpicByID(idForDelete);
                        sendText(httpExchange, "Успешно удалено!");
                    } else {
                        sendNotFound(httpExchange, String.format("Эпика с заданным id (%d) не найдено.", idForDelete));
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

    private String printAllEpics() {
        if (tm.getAllEpics().isEmpty()) {
            return gson.toJson("Список эпиков пуст.");
        }

        StringBuilder messageWithAllEpics = new StringBuilder();
        for (Epic epic : tm.getAllEpics()) {
            messageWithAllEpics.append(epic.toString());
            messageWithAllEpics.append(getFullDescOfAllSubtasksOfEpicById(epic.getId()));
        }
        return messageWithAllEpics.toString();
    }

    private String printOneEpicWithAllItsSubtasks(int id) {
        StringBuilder message = new StringBuilder();
        message.append(tm.findEpicByID(id).toString());
        String subtasks = tm.findEpicByID(id).getSubtasksIDs().stream()
                .map(tm::findSubtaskByID)
                .map(Subtask::toString)
                .collect(Collectors.joining());

        return message.append(subtasks).toString();
    }

    private String getFullDescOfAllSubtasksOfEpicById(int id) {
        Epic epic = tm.findEpicByID(id);

        Map<Integer, Subtask> allSubtasks = new HashMap<>();
        for (Subtask subtask : tm.getAllSubtasks()) {
            allSubtasks.put(subtask.getId(), subtask);
        }

        List<Subtask> allSubtasksInEpic = epic.getSubtasksIDs().stream()
                .map(allSubtasks::get)
                .sorted(new SubtaskStartTimeComparator())
                .toList();

        String fullDescription = "";
        for (int i = 0; i < allSubtasksInEpic.size(); i++) {
            Subtask currentSubtask = allSubtasksInEpic.get(i);
            fullDescription = fullDescription + "Подзадача №" + (i + 1) + currentSubtask.toString();
        }
        return fullDescription + NEW_LINE;
    }
}