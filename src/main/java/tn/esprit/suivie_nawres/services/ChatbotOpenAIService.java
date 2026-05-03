package tn.esprit.suivie_nawres.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 🤖 SERVICE CHATBOT MÉDICAL AVEC OPENAI (ChatGPT)
 * =================================================
 * Alternative plus stable à Gemini
 * 
 * 🌐 API : OpenAI ChatGPT
 * - Modèle : gpt-3.5-turbo (rapide et gratuit avec crédit initial)
 * - Plus stable que Gemini
 * 
 * 🔑 OBTENIR UNE CLÉ API :
 * 1. Aller sur : https://platform.openai.com/api-keys
 * 2. Créer un compte (5$ de crédit gratuit)
 * 3. Créer une clé API
 * 4. Copier la clé dans openai.properties
 */
public class ChatbotOpenAIService {

    private static final String CONFIG_FILE = "/openai.properties";
    private String apiKey;
    private String apiUrl;
    private String model;

    public ChatbotOpenAIService() {
        chargerConfiguration();
    }

    private void chargerConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration introuvable : " + CONFIG_FILE);
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            this.apiKey = prop.getProperty("openai.api.key");
            this.apiUrl = prop.getProperty("openai.api.url");
            this.model = prop.getProperty("openai.model");
            
            if (apiKey == null || apiKey.isEmpty() || "VOTRE_CLE_OPENAI_ICI".equals(apiKey)) {
                throw new IllegalStateException(
                    "⚠️ Clé API OpenAI manquante !\n\n" +
                    "Pour utiliser le chatbot :\n" +
                    "1. Allez sur : https://platform.openai.com/api-keys\n" +
                    "2. Créez une clé API (5$ gratuit)\n" +
                    "3. Copiez-la dans src/main/resources/openai.properties"
                );
            }
            
            System.out.println("✓ Configuration OpenAI chargée avec succès");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur configuration OpenAI : " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration OpenAI", e);
        }
    }

    public String envoyerMessage(String messageUtilisateur) throws Exception {
        System.out.println("🤖 Envoi du message au chatbot OpenAI...");
        
        String promptComplet = creerPromptMedical(messageUtilisateur);
        String reponseJson = envoyerRequeteApi(promptComplet);
        String reponse = extraireReponse(reponseJson);
        
        System.out.println("✅ Réponse reçue");
        return reponse;
    }

    private String creerPromptMedical(String messageUtilisateur) {
        return "Tu es un assistant médical virtuel pour l'application VitaPlus. " +
               "Tu aides les patients et les médecins avec des informations médicales générales. " +
               "Tu es professionnel, empathique et précis. " +
               "Tu rappelles toujours que tes conseils ne remplacent pas une consultation médicale réelle. " +
               "Réponds en français de manière claire et concise.\n\n" +
               "Question du patient : " + messageUtilisateur;
    }

    private String envoyerRequeteApi(String prompt) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            
            String jsonBody = creerCorpsRequete(prompt);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("📥 Code de réponse API : " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    
                    throw new IOException("Erreur API OpenAI (code " + responseCode + ") : " + errorMessage.toString());
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }

    private String creerCorpsRequete(String prompt) {
        return "{\n" +
               "  \"model\": \"" + model + "\",\n" +
               "  \"messages\": [\n" +
               "    {\n" +
               "      \"role\": \"user\",\n" +
               "      \"content\": " + escapeJson(prompt) + "\n" +
               "    }\n" +
               "  ],\n" +
               "  \"temperature\": 0.7,\n" +
               "  \"max_tokens\": 1000\n" +
               "}";
    }

    private String extraireReponse(String jsonResponse) {
        try {
            // Format OpenAI: {"choices":[{"message":{"content":"..."}}]}
            int contentStart = jsonResponse.indexOf("\"content\"");
            if (contentStart == -1) {
                return "Désolé, je n'ai pas pu générer une réponse.";
            }
            
            int colonIndex = jsonResponse.indexOf(":", contentStart);
            int quoteStart = jsonResponse.indexOf("\"", colonIndex) + 1;
            int quoteEnd = jsonResponse.indexOf("\"", quoteStart);
            
            while (jsonResponse.charAt(quoteEnd - 1) == '\\') {
                quoteEnd = jsonResponse.indexOf("\"", quoteEnd + 1);
            }
            
            String texte = jsonResponse.substring(quoteStart, quoteEnd);
            
            texte = texte.replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            
            return texte;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur extraction réponse : " + e.getMessage());
            return "Désolé, une erreur s'est produite.";
        }
    }

    private String escapeJson(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    public boolean estConfigure() {
        return apiKey != null && !apiKey.isEmpty() && !"VOTRE_CLE_OPENAI_ICI".equals(apiKey);
    }
}
