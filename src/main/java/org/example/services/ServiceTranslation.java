package org.example.services;

import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ServiceTranslation {

    public static String translateToEnglish(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            // L'API gratuite MyMemory limite la taille du texte par requête.
            String textToTranslate = text.length() > 450 ? text.substring(0, 450) + "..." : text;
            String encodedText = URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=fr%7Cen";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject responseData = jsonResponse.getJSONObject("responseData");
                return responseData.getString("translatedText");
            } else {
                return "Erreur de traduction (" + response.statusCode() + ")";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Traduction indisponible : " + e.getMessage();
        }
    }
}
