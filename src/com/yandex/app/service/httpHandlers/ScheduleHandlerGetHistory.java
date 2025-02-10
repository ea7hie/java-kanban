package com.yandex.app.service.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.app.service.InMemoryTaskManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ScheduleHandlerGetHistory extends BaseHttpHandler implements HttpHandler {
    private final InMemoryTaskManager inMemoryTaskManager;

    public ScheduleHandlerGetHistory(InMemoryTaskManager inMemoryTaskManager) {
        this.inMemoryTaskManager = inMemoryTaskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        List<String> path = Arrays.asList(httpExchange.getRequestURI().getPath().split("/"));

        if (path.size() == 3 && method.equals("GET")) {
            sendText(httpExchange, inMemoryTaskManager.showListViewedTasks());
        } else if (path.size() != 3) {
            sendTextErrorURLLength(httpExchange);
        } else {
            sendTextErrorMethod(httpExchange);
        }
    }
}