package com.yandex.app.service.httpHandlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;

class BaseHttpHandler {
    protected void sendText(HttpExchange h, String text) throws IOException {
        write(h, 200, text);
    }

    protected void sendTextCreated(HttpExchange h) throws IOException {
        write(h, 201, "Успешно сохранено!\n");
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        write(h, 404, text);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        write(h, 406, "Ошибка сохранения! На это время у вас запланировано кое-что другое!\n");

    }

    protected void sendTextErrorURLLength(HttpExchange h) throws IOException {
        write(h, 414, "Проверьте правильность URL!");
    }

    protected void sendTextErrorMethod(HttpExchange h) throws IOException {
        write(h, 400, "Проверьте правильность выбранного метода!");
    }

    protected void sendTextErrorIsNaN(HttpExchange h) throws IOException {
        write(h, 400, "Проверьте правильность введённого id! (Введено не число!)");
    }

    protected void sendTextErrorInValueOfProgress(HttpExchange h) throws IOException {
        write(h, 400, "Проверьте правильность введённого статуса прогресса!");
    }

    protected void sendTextErrorInvalidValues(HttpExchange h) throws IOException {
        write(h, 400, "Проверьте правильность введённых полей!");
    }

    protected void sendTextErrorServer(HttpExchange h) throws ServerException {
        try {
            write(h, 500, "Приносим извинения! Произошла ошибка в работе сервера!");
        } catch (IOException e) {
            throw new ServerException("Произошла ошибка в работе сервера при попытке отправить sendTextErrorServer!");
        }
    }

    private void write(HttpExchange h, int code, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}