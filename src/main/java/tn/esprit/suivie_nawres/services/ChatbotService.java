package tn.esprit.suivie_nawres.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 🤖 SERVICE CHATBOT MÉDICAL AVEC GEMINI AI
 * ==========================================
 * Ce service utilise l'API Gemini de Google pour créer un assistant médical intelligent.
 * 
 * 🌐 API EXTERNE : Google Gemini AI
 * - Gratuit avec quota généreux
 * - Modèle : gemini-pro
 * - Spécialisé pour les conversations médicales
 * 
 * 🔑 OBTENIR UNE CLÉ API :
 * 1. Aller sur : https://makersuite.google.com/app/apikey
 * 2. Se connecter avec un compte Google
 * 3. Cliquer sur "Create API Key"
 * 4. Copier la clé dans gemini.properties
 */
public class ChatbotService {

    private static final String CONFIG_FILE = "/gemini.properties";
    private String apiKey;
    private String apiUrl;

    /**
     * 🔧 CONSTRUCTEUR : Charge la configuration
     */
    public ChatbotService() {
        chargerConfiguration();
    }

    /**
     * 📋 CHARGE LA CONFIGURATION DEPUIS gemini.properties
     */
    private void chargerConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration introuvable : " + CONFIG_FILE);
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            this.apiKey = prop.getProperty("gemini.api.key");
            this.apiUrl = prop.getProperty("gemini.api.url");
            
            if (apiKey == null || apiKey.isEmpty() || "VOTRE_CLE_API_ICI".equals(apiKey)) {
                throw new IllegalStateException(
                    "⚠️ Clé API Gemini manquante !\n\n" +
                    "Pour utiliser le chatbot :\n" +
                    "1. Allez sur : https://makersuite.google.com/app/apikey\n" +
                    "2. Créez une clé API gratuite\n" +
                    "3. Copiez-la dans src/main/resources/gemini.properties"
                );
            }
            
            System.out.println("✓ Configuration Gemini AI chargée avec succès");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la configuration Gemini : " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration Gemini", e);
        }
    }

    /**
     * 💬 ENVOIE UN MESSAGE AU CHATBOT ET REÇOIT LA RÉPONSE
     * ====================================================
     * 
     * @param messageUtilisateur Le message de l'utilisateur
     * @return La réponse du chatbot
     * @throws Exception Si une erreur survient
     */
    public String envoyerMessage(String messageUtilisateur) throws Exception {
        System.out.println("🤖 Envoi du message au chatbot Gemini...");
        System.out.println("📝 Message : " + messageUtilisateur);
        
        // Créer le contexte médical pour le chatbot
        String promptComplet = creerPromptMedical(messageUtilisateur);
        
        // Envoyer la requête à l'API
        String reponseJson = envoyerRequeteApi(promptComplet);
        
        // Extraire la réponse du JSON
        String reponse = extraireReponse(reponseJson);
        
        System.out.println("✅ Réponse reçue : " + reponse.substring(0, Math.min(100, reponse.length())) + "...");
        
        return reponse;
    }

    /**
     * 🏥 CRÉE UN PROMPT MÉDICAL CONTEXTUALISÉ
     * =======================================
     * Ajoute un contexte médical pour que le chatbot réponde de manière appropriée
     */
    private String creerPromptMedical(String messageUtilisateur) {
        return "Tu es un assistant médical virtuel pour l'application VitaPlus. " +
               "Tu aides les patients et les médecins avec des informations médicales générales. " +
               "Tu es professionnel, empathique et précis. " +
               "Tu rappelles toujours que tes conseils ne remplacent pas une consultation médicale réelle. " +
               "Réponds en français de manière claire et concise.\n\n" +
               "Question du patient : " + messageUtilisateur;
    }

    /**
     * 🌐 ENVOIE LA REQUÊTE À L'API GEMINI
     * ===================================
     */
    private String envoyerRequeteApi(String prompt) throws Exception {
        // Utiliser l'URL directement (sans la clé dans l'URL)
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            // ✅ CORRECTION : Utiliser X-goog-api-key dans le header
            connection.setRequestProperty("X-goog-api-key", apiKey);
            
            // Corps de la requête JSON pour Gemini
            String jsonBody = creerCorpsRequete(prompt);
            
            // Envoyer la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("📥 Code de réponse API : " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lire la réponse
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
                // Lire le message d'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    
                    throw new IOException("Erreur API Gemini (code " + responseCode + ") : " + errorMessage.toString());
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 📦 CRÉE LE CORPS DE LA REQUÊTE JSON
     */
    private String creerCorpsRequete(String prompt) {
        return "{\n" +
               "  \"contents\": [{\n" +
               "    \"parts\": [{\n" +
               "      \"text\": " + escapeJson(prompt) + "\n" +
               "    }]\n" +
               "  }],\n" +
               "  \"generationConfig\": {\n" +
               "    \"temperature\": 0.7,\n" +
               "    \"maxOutputTokens\": 1000\n" +
               "  }\n" +
               "}";
    }

    /**
     * 🔍 EXTRAIT LA RÉPONSE DU JSON
     */
    private String extraireReponse(String jsonResponse) {
        try {
            // Recherche simple du texte dans la réponse JSON
            // Format: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}
            
            int textStart = jsonResponse.indexOf("\"text\"");
            if (textStart == -1) {
                return "Désolé, je n'ai pas pu générer une réponse.";
            }
            
            int colonIndex = jsonResponse.indexOf(":", textStart);
            int quoteStart = jsonResponse.indexOf("\"", colonIndex) + 1;
            int quoteEnd = jsonResponse.indexOf("\"", quoteStart);
            
            // Gérer les guillemets échappés
            while (jsonResponse.charAt(quoteEnd - 1) == '\\') {
                quoteEnd = jsonResponse.indexOf("\"", quoteEnd + 1);
            }
            
            String texte = jsonResponse.substring(quoteStart, quoteEnd);
            
            // Décoder les caractères échappés
            texte = texte.replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            
            return texte;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'extraction de la réponse : " + e.getMessage());
            return "Désolé, une erreur s'est produite lors du traitement de la réponse.";
        }
    }

    /**
     * 🔧 ÉCHAPPE LE TEXTE POUR LE JSON
     */
    private String escapeJson(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    /**
     * ✅ VÉRIFIE SI L'API EST CONFIGURÉE
     */
    public boolean estConfigure() {
        return apiKey != null && !apiKey.isEmpty() && !"VOTRE_CLE_API_ICI".equals(apiKey);
    }
}
