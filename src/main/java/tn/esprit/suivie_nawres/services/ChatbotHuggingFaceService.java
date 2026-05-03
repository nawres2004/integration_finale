package tn.esprit.suivie_nawres.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 🤖 SERVICE CHATBOT MÉDICAL AVEC HUGGING FACE
 * =============================================
 * 100% GRATUIT et ILLIMITÉ !
 * 
 * 🌐 API : Hugging Face Inference API
 * - Modèle : Mistral-7B-Instruct (excellent pour le français)
 * - Totalement gratuit
 * - Pas de limite de quota
 * 
 * 🔑 OBTENIR UNE CLÉ API (2 minutes) :
 * 1. Aller sur : https://huggingface.co/join
 * 2. Créer un compte gratuit
 * 3. Aller sur : https://huggingface.co/settings/tokens
 * 4. Cliquer sur "New token" → "Read"
 * 5. Copier le token dans huggingface.properties
 */
public class ChatbotHuggingFaceService {

    private static final String CONFIG_FILE = "/huggingface.properties";
    private String apiKey;
    private String apiUrl;

    public ChatbotHuggingFaceService() {
        chargerConfiguration();
    }

    private void chargerConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration introuvable : " + CONFIG_FILE);
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            this.apiKey = prop.getProperty("huggingface.api.key");
            this.apiUrl = prop.getProperty("huggingface.api.url");
            
            if (apiKey == null || apiKey.isEmpty() || "VOTRE_CLE_HUGGINGFACE_ICI".equals(apiKey)) {
                throw new IllegalStateException(
                    "⚠️ Clé API Hugging Face manquante !\n\n" +
                    "Pour utiliser le chatbot GRATUIT :\n" +
                    "1. Allez sur : https://huggingface.co/join\n" +
                    "2. Créez un compte gratuit\n" +
                    "3. Allez sur : https://huggingface.co/settings/tokens\n" +
                    "4. Créez un token (Read)\n" +
                    "5. Copiez-le dans src/main/resources/huggingface.properties"
                );
            }
            
            System.out.println("✓ Configuration Hugging Face chargée avec succès");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur configuration Hugging Face : " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration Hugging Face", e);
        }
    }

    public String envoyerMessage(String messageUtilisateur) throws Exception {
        System.out.println("🤖 Envoi du message au chatbot Hugging Face...");
        
        String promptComplet = creerPromptMedical(messageUtilisateur);
        String reponseJson = envoyerRequeteApi(promptComplet);
        String reponse = extraireReponse(reponseJson);
        
        System.out.println("✅ Réponse reçue");
        return reponse;
    }

    private String creerPromptMedical(String messageUtilisateur) {
        // Format Mistral Instruct
        return "[INST] Tu es un assistant médical virtuel pour l'application VitaPlus. " +
               "Tu aides les patients avec des informations médicales générales en français. " +
               "Tu es professionnel, empathique et précis. " +
               "Tu rappelles toujours que tes conseils ne remplacent pas une consultation médicale réelle. " +
               "Réponds en français de manière claire et concise.\n\n" +
               "Question : " + messageUtilisateur + " [/INST]";
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
            } else if (responseCode == 503) {
                // Modèle en cours de chargement
                return "[{\"generated_text\":\"Le modèle est en cours de chargement. Veuillez réessayer dans 20 secondes.\"}]";
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    
                    throw new IOException("Erreur API Hugging Face (code " + responseCode + ") : " + errorMessage.toString());
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }

    private String creerCorpsRequete(String prompt) {
        // Format simple pour DialoGPT
        return "{\n" +
               "  \"inputs\": " + escapeJson(prompt) + "\n" +
               "}";
    }

    private String extraireReponse(String jsonResponse) {
        try {
            // Format Hugging Face: [{"generated_text":"..."}]
            
            // Vérifier si c'est un message de chargement
            if (jsonResponse.contains("en cours de chargement")) {
                return "⏳ Le modèle IA est en cours de chargement...\n\n" +
                       "Veuillez réessayer dans 20 secondes.\n\n" +
                       "(C'est normal la première fois, ensuite c'est instantané !)";
            }
            
            int textStart = jsonResponse.indexOf("\"generated_text\"");
            if (textStart == -1) {
                return "Désolé, je n'ai pas pu générer une réponse.";
            }
            
            int colonIndex = jsonResponse.indexOf(":", textStart);
            int quoteStart = jsonResponse.indexOf("\"", colonIndex) + 1;
            int quoteEnd = jsonResponse.lastIndexOf("\"");
            
            if (quoteEnd <= quoteStart) {
                return "Désolé, je n'ai pas pu générer une réponse.";
            }
            
            String texte = jsonResponse.substring(quoteStart, quoteEnd);
            
            // Nettoyer le texte
            texte = texte.replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("[/INST]", "")
                        .replace("[INST]", "")
                        .trim();
            
            // Enlever le prompt s'il est répété
            if (texte.startsWith("Tu es un assistant")) {
                int questionIndex = texte.indexOf("Question :");
                if (questionIndex != -1) {
                    int answerStart = texte.indexOf("\n", questionIndex);
                    if (answerStart != -1) {
                        texte = texte.substring(answerStart).trim();
                    }
                }
            }
            
            return texte;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur extraction réponse : " + e.getMessage());
            e.printStackTrace();
            return "Désolé, une erreur s'est produite lors du traitement de la réponse.";
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
        return apiKey != null && !apiKey.isEmpty() && !"VOTRE_CLE_HUGGINGFACE_ICI".equals(apiKey);
    }
}
