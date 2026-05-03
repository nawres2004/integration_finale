package tn.esprit.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIService {

    // IMPORTANT: Remplacez par votre clé API Gemini
    private static final String API_KEY = "AIzaSyBqS3Oyi42epk24fjX9s7WqyvjQOf8GcOA";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key="
            + API_KEY;

    private final HttpClient httpClient;

    public AIService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getContreIndications(String nomMedicament, String dosage) throws IOException, InterruptedException {
        if (API_KEY.equals("VOTRE_CLE_API_GEMINI")) {
            return "Veuillez configurer votre clé API Gemini dans AIService.java.";
        }

        String prompt = "Donne-moi les contre-indications médicales principales pour le médicament suivant : "
                + nomMedicament + " " + (dosage != null ? dosage : "")
                + ". Réponds de manière concise, en français, sous forme de liste séparée par des virgules.";

        // Construction manuelle du JSON pour éviter la dépendance org.json
        String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + escapeJson(prompt) + "\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return extractTextFromResponse(response.body());
        } else {
            System.err.println("DEBUG Gemini Error Body: " + response.body());
            throw new IOException("Erreur API Gemini (Status " + response.statusCode() + "): " + response.body());
        }
    }

    /**
     * Extrait le texte du JSON de réponse Gemini sans utiliser de bibliothèque
     * externe.
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            // Recherche simple du champ "text" dans la structure JSON de Gemini
            String pattern = "\"text\": \"";
            int start = responseBody.indexOf(pattern);
            if (start == -1)
                return "Aucune réponse trouvée.";

            start += pattern.length();
            int end = responseBody.indexOf("\"", start);

            if (end == -1)
                return "Erreur de formatage de la réponse.";

            String text = responseBody.substring(start, end);
            // Dé-échappement basique des sauts de ligne
            return text.replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception e) {
            return "Impossible d'extraire les informations de l'IA.";
        }
    }

    private String escapeJson(String input) {
        if (input == null)
            return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
