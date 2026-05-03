package org.example.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ServiceGemini {

    private static final String API_KEY = "AIzaSyDTjXjw_CRo2XvQik7Q4t1NI2j8F-9R4lg";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    // Modèles de secours en ordre de priorité
    private static final String[] MODELS = {
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-2.0-flash"
    };

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 secondes entre chaque tentative

    public static String generateArticle(String sujet) {
        if (API_KEY.equals("VOTRE_CLE_API_ICI") || API_KEY.isEmpty()) {
            return "ERREUR : Clé API manquante.\n\nVeuillez insérer votre clé API Gemini dans le fichier ServiceGemini.java.\nPour obtenir une clé gratuite :\n1. Allez sur https://aistudio.google.com/app/apikey\n2. Connectez-vous avec Google\n3. Cliquez sur 'Create API key'\n4. Copiez la clé et collez-la à la place de 'VOTRE_CLE_API_ICI'.";
        }

        String prompt = "Tu es un docteur professionnel. Rédige un article de blog médical très bien structuré, clair et rassurant sur le sujet suivant : " + sujet + ". "
                      + "Utilise des sauts de ligne pour aérer le texte. Ne mets pas de titre général car je l'ai déjà, commence directement par l'introduction.";

        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject contentsObj = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject partsObj = new JSONObject();

        partsObj.put("text", prompt);
        partsArray.put(partsObj);
        contentsObj.put("parts", partsArray);
        contentsArray.put(contentsObj);
        requestBody.put("contents", contentsArray);

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        // Essayer chaque modèle dans l'ordre
        for (String model : MODELS) {
            String apiUrl = BASE_URL + model + ":generateContent?key=" + API_KEY;
            String result = tryGenerateWithRetry(client, apiUrl, requestBody.toString(), model);
            if (result != null) {
                return result;
            }
        }

        return "Impossible de générer l'article. Tous les modèles IA sont temporairement indisponibles. Veuillez réessayer dans quelques instants.";
    }

    /**
     * Tente d'appeler un modèle donné avec plusieurs tentatives en cas d'erreur 503.
     * @return le texte généré si succès, null si échec après toutes les tentatives
     */
    private static String tryGenerateWithRetry(HttpClient client, String apiUrl, String body, String modelName) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if (status == 200) {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            return parts.getJSONObject(0).getString("text");
                        }
                    }
                } else if (status == 503) {
                    // Modèle surchargé — attendre et réessayer
                    System.err.println("[ServiceGemini] Modèle " + modelName + " surchargé (503). Tentative " + attempt + "/" + MAX_RETRIES);
                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // backoff exponentiel
                    }
                } else if (status == 404) {
                    // Modèle introuvable — passer au suivant immédiatement
                    System.err.println("[ServiceGemini] Modèle " + modelName + " introuvable (404). Passage au modèle suivant.");
                    return null;
                } else {
                    // Autre erreur — arrêter les tentatives pour ce modèle
                    System.err.println("[ServiceGemini] Erreur " + status + " pour " + modelName + " : " + response.body());
                    return null;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("[ServiceGemini] Exception pour " + modelName + " : " + e.getMessage());
                if (attempt >= MAX_RETRIES) return null;
            }
        }
        return null; // Toutes les tentatives échouées pour ce modèle
    }
}
