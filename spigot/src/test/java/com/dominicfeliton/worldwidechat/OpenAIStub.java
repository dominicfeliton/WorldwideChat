package com.dominicfeliton.worldwidechat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class OpenAIStub implements AutoCloseable {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_TRANSLATION =
            "{\"success\":true,\"translation\":\"Hola, como estas?\",\"reason\":\"none\"}";

    private final List<String> requestBodies = new CopyOnWriteArrayList<>();
    private final HttpServer server;
    private final String guidelinesResponseBody;
    private final String translationResponseBody;
    private final int statusCode;
    private final long delayMillis;
    private final String url;

    private OpenAIStub(String responseBody, int statusCode, long delayMillis) throws IOException {
        this(responseBody, responsesAPIResponse(DEFAULT_TRANSLATION),
                statusCode, delayMillis, "/v1/responses");
    }

    private OpenAIStub(String guidelinesResponseBody, String translationResponseBody,
                       int statusCode, long delayMillis, String endpointPath) throws IOException {
        this.guidelinesResponseBody = guidelinesResponseBody;
        this.translationResponseBody = translationResponseBody;
        this.statusCode = statusCode;
        this.delayMillis = delayMillis;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handleRequest);
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + endpointPath;
    }

    static OpenAIStub success(String content) throws IOException {
        return new OpenAIStub(responsesAPIResponse(content), 200, 0);
    }

    static OpenAIStub error() throws IOException {
        return new OpenAIStub("", 500, 0);
    }

    static OpenAIStub timeout() throws IOException {
        return new OpenAIStub(responsesAPIResponse("{\"translatable\":true}"), 200, 1500);
    }

    static OpenAIStub chatCompletionsSuccess() throws IOException {
        return new OpenAIStub(
                chatCompletionResponse("{\"translatable\":true}"),
                chatCompletionResponse(DEFAULT_TRANSLATION),
                200,
                0,
                "/v1/chat/completions");
    }

    static OpenAIStub translationSuccess(String content) throws IOException {
        return new OpenAIStub(
                responsesAPIResponse("{\"translatable\":true}"),
                responsesAPIResponse(content),
                200,
                0,
                "/v1/responses");
    }

    String url() {
        return url;
    }

    String guidelinesRequestBody() {
        return requestBodies.stream()
                .filter(OpenAIStub::isGuidelinesRequest)
                .findFirst()
                .orElse("");
    }

    String translationRequestBody() {
        return requestBodies.stream()
                .filter(request -> !isGuidelinesRequest(request))
                .findFirst()
                .orElse("");
    }

    String guidelinesRequestInput() {
        return requestInput(guidelinesRequestBody());
    }

    String translationRequestInput() {
        return requestInput(translationRequestBody());
    }

    int requestCount() {
        return requestBodies.size();
    }

    int guidelinesRequestCount() {
        return (int) requestBodies.stream()
                .filter(OpenAIStub::isGuidelinesRequest)
                .count();
    }

    int translationRequestCount() {
        return (int) requestBodies.stream()
                .filter(request -> !isGuidelinesRequest(request))
                .count();
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requestBodies.add(requestBody);

        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (statusCode != 200) {
            sendResponse(exchange, statusCode, "{\"error\":\"boom\"}");
            return;
        }

        sendResponse(exchange, 200, isGuidelinesRequest(requestBody)
                ? guidelinesResponseBody
                : translationResponseBody);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(body);
        } finally {
            exchange.close();
        }
    }

    private static boolean isGuidelinesRequest(String request) {
        return request.contains("translation_guideline_check");
    }

    private static String requestInput(String requestBody) {
        if (requestBody == null || requestBody.isBlank()) {
            return "";
        }

        JsonObject root = JsonParser.parseString(requestBody).getAsJsonObject();
        if (root.has("input")) {
            return root.get("input").getAsString();
        }

        JsonArray messages = root.getAsJsonArray("messages");
        for (JsonElement message : messages) {
            JsonObject messageObject = message.getAsJsonObject();
            if ("user".equals(messageObject.get("role").getAsString())) {
                return messageObject.get("content").getAsString();
            }
        }
        return "";
    }

    private static String responsesAPIResponse(String content) {
        return "{\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":"
                + GSON.toJson(content) + "}]}]}";
    }

    private static String chatCompletionResponse(String content) {
        return "{\"choices\":[{\"message\":{\"content\":" + GSON.toJson(content) + "}}]}";
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
