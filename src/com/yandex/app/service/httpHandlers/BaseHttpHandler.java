package com.yandex.app.service.httpHandlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;

class BaseHttpHandler {
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextCreated(HttpExchange h) throws IOException {
        byte[] resp = "Успешно сохранено!\n".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        byte[] resp = "Ошибка сохранения! На это время у вас запланировано кое-что другое!\n"
                .getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextErrorURLLength(HttpExchange h) throws IOException {
        byte[] resp = "Проверьте правильность URL!".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(414, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextErrorMethod(HttpExchange h) throws IOException {
        byte[] resp = "Проверьте правильность выбранного метода!".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextErrorIsNaN(HttpExchange h) throws IOException {
        byte[] resp = "Проверьте правильность введённого id! (Введено не число!)".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextErrorInValueOfProgress(HttpExchange h) throws IOException {
        byte[] resp = "Проверьте правильность введённого статуса прогресса!".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextErrorInvalidValues(HttpExchange h) throws IOException {
        byte[] resp = "Проверьте правильность введённых полей!".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendTextErrorServer(HttpExchange h) throws ServerException {
        try {
            byte[] resp = "Приносим извинения! Произошла ошибка в работе сервера!".getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(500, resp.length);
            h.getResponseBody().write(resp);
            h.close();
        } catch (IOException e) {
            throw new ServerException("Произошла ошибка в работе сервера при попытке отправить sendTextErrorServer!");
        }
    }
}