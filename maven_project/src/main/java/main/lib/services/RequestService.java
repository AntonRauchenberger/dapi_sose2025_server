package main.lib.services;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles http get requests
 */
public class RequestService implements Runnable {

    private final static int PORT = 8080;

    static class GetHandler implements HttpHandler {

        // Helper method for parsing query parameters
        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null || query.isEmpty())
                return params;

            for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                params.put(key, value);
            }
            return params;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                return;
            }

            URI requestURI = exchange.getRequestURI();
            Map<String, String> queryParams = parseQueryParams(requestURI.getRawQuery());

            String type = queryParams.getOrDefault("type", "all");
            String responseJson;

            // TODO add needed functions and responses
            switch (type.toLowerCase()) {
                case "status" -> responseJson = """
                        { "status": "success" }
                        """;
                case "time" -> responseJson = """
                        { "timestamp": "%s" }
                        """.formatted(Instant.now());
                case "all" -> responseJson = """
                        {
                          "status": "success",
                          "message": "GET request empfangen!",
                          "timestamp": "%s"
                        }
                        """.formatted(Instant.now());
                case "image" -> {
                    try {
                        File imageFile = new File("maven_project\\src\\main\\resources\\dog_example.png");

                        if (!imageFile.exists()) {
                            exchange.sendResponseHeaders(404, 0);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write("Bild nicht gefunden.".getBytes(StandardCharsets.UTF_8));
                            }
                            return;
                        }

                        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

                        exchange.getResponseHeaders().add("Content-Type", "image/png");
                        exchange.sendResponseHeaders(200, imageBytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(imageBytes);
                        }

                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        exchange.sendResponseHeaders(500, 0);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write("Serverfehler beim Laden des Bildes.".getBytes(StandardCharsets.UTF_8));
                        }
                        return;
                    }
                }

                default -> {
                    exchange.sendResponseHeaders(400, 0); // Bad Request
                    responseJson = """
                            { "error": "Ungültiger 'type'-Parameter." }
                            """;
                }
            }

            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            byte[] responseBytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    // Is started as thread, so queries can be handled in the background
    @Override
    public void run() {
        final HttpServer[] serverHolder = new HttpServer[1];

        try {
            serverHolder[0] = HttpServer.create(new InetSocketAddress(PORT), 0);
            serverHolder[0].createContext("/api/data", new GetHandler());
            serverHolder[0].setExecutor(null); // Default executor
        } catch (IOException e) {
            System.err.println("[Fehler] HTTP-Server konnte nicht erstellt werden:");
            e.printStackTrace();
            return;
        }

        String localIp;
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("[Warnung] Lokale IP-Adresse konnte nicht ermittelt werden, verwende 'localhost'.");
            localIp = "localhost";
        }

        // Shutdown-Hook mit Zugriff auf serverHolder[0]
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("HTTP Server wird gestoppt...");
            serverHolder[0].stop(0);
        }));

        System.out.println("=========================================");
        System.out.println("HTTP Server läuft unter:");
        System.out.println("GET -> http://" + localIp + ":" + PORT + "/api/data?type=all");
        System.out.println("=========================================");

        try {
            serverHolder[0].start();
        } catch (Exception e) {
            System.err.println("[Fehler] Beim Starten des HTTP-Servers ist ein Fehler aufgetreten:");
            e.printStackTrace();
        }
    }

}
