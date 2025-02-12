package com.yandex.app.service.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.service.FileBackedTaskManager;
import com.yandex.app.service.InMemoryTaskManager;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.interfaces.TaskManager;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.List;

public class ScheduleHandlerGetPrioritized extends BaseHttpHandler implements HttpHandler {
    private final TaskManager inMemoryTaskManager;

    public ScheduleHandlerGetPrioritized(TaskManager taskManager) {
        this.inMemoryTaskManager = ((FileBackedTaskManager) taskManager).getInMemoryTaskManager();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws ServerException {
        try {
            String method = httpExchange.getRequestMethod();
            List<String> path = Arrays.asList(httpExchange.getRequestURI().getPath().split("/"));

            if (path.size() == 3 && method.equals("GET")) {
                sendText(httpExchange, ((InMemoryTaskManager) inMemoryTaskManager).getPrioritizedTasks());
            } else if (path.size() != 3) {
                sendTextErrorURLLength(httpExchange);
            } else {
                sendTextErrorMethod(httpExchange);
            }
        }  catch (IOException e) {
            sendTextErrorServer(httpExchange);
            throw new ServersException("Ошибка в работе сервера при работе с эпиками");
        }
    }
}