package main.lib.helpers;

import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ImageHelper {

    public static void handleImageRequest(HttpExchange exchange, String imagePath) throws IOException {
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            String responseJson = "{ \"error\": \"Bild nicht gefunden.\" }";
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(404, responseJson.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseJson.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, imageBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(imageBytes);
            }
        }
    }
}