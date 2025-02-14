package com.yandex.app.service.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.service.exceptions.ServersException;
import com.yandex.app.service.interfaces.TaskManager;

import java.rmi.ServerException;
import java.util.Arrays;
import java.util.List;

public class ScheduleHandlerGetHistory extends BaseHttpHandler implements HttpHandler {
    private final TaskManager tm;

    public ScheduleHandlerGetHistory(TaskManager taskManager) {
        this.tm = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws ServerException {
        try {
            String method = httpExchange.getRequestMethod();
            List<String> path = Arrays.asList(httpExchange.getRequestURI().getPath().split("/"));

            if (path.size() == 3 && method.equals("GET")) {
                sendText(httpExchange, tm.showListViewedTasks());
            } else if (path.size() != 3) {
                sendTextErrorURLLength(httpExchange);
            } else {
                sendTextErrorMethod(httpExchange);
            }
        } catch (Exception e) {
            sendTextErrorServer(httpExchange);
            throw new ServersException("Ошибка в работе сервера при работе с эпиками");
        }
    }
}