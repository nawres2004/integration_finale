package tn.esprit.suivie_nawres.services;

import tn.esprit.suivie_nawres.models.Consultation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;

/**
 * 📄 SERVICE PDF AVEC API PDFSHIFT
 * =================================
 * Ce service utilise l'API PDFShift pour générer des PDF professionnels
 * à partir de templates HTML.
 * 
 * 🌐 API EXTERNE : PDFShift (https://pdfshift.io/)
 * - Convertit du HTML en PDF de haute qualité
 * - Pas besoin de bibliothèque locale comme iText
 * - Génération côté serveur (cloud)
 * 
 * 🔑 AUTHENTIFICATION :
 * - Clé API stockée dans pdfshift.properties
 * - Authentification Basic Auth (API Key + mot de passe vide)
 * 
 * 💡 FONCTIONNEMENT :
 * 1. Créer un template HTML avec les données de la consultation
 * 2. Envoyer le HTML à l'API PDFShift
 * 3. Recevoir le PDF généré
 * 4. Sauvegarder le PDF sur le disque
 */
public class ConsultationPdfShiftService {

    private static final String CONFIG_FILE = "/pdfshift.properties";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");
    
    private String apiKey;
    private String apiUrl;

    /**
     * 🔧 CONSTRUCTEUR : Charge la configuration
     */
    public ConsultationPdfShiftService() {
        chargerConfiguration();
    }

    /**
     * 📋 CHARGE LA CONFIGURATION DEPUIS pdfshift.properties
     */
    private void chargerConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration introuvable : " + CONFIG_FILE);
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            this.apiKey = prop.getProperty("pdfshift.api.key");
            this.apiUrl = prop.getProperty("pdfshift.api.url");
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Clé API PDFShift manquante dans la configuration");
            }
            
            System.out.println("✓ Configuration PDFShift chargée avec succès");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la configuration PDFShift : " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration PDFShift", e);
        }
    }

    /**
     * 🎯 MÉTHODE PRINCIPALE : GÉNÉRER LE PDF VIA API
     * ==============================================
     * 
     * @param consultation L'objet Consultation contenant toutes les données
     * @param cheminFichier Le chemin où sauvegarder le PDF
     * @throws Exception Si une erreur survient
     */
    public void genererPdfConsultation(Consultation consultation, String cheminFichier) throws Exception {
        System.out.println("🚀 Génération du PDF via PDFShift API...");
        
        // ✅ ÉTAPE 1 : Créer le template HTML
        String htmlContent = creerTemplateHtml(consultation);
        
        // ✅ ÉTAPE 2 : Envoyer à l'API PDFShift
        byte[] pdfBytes = envoyerRequeteApi(htmlContent);
        
        // ✅ ÉTAPE 3 : Sauvegarder le PDF
        sauvegarderPdf(pdfBytes, cheminFichier);
        
        System.out.println("✓ PDF généré avec succès : " + cheminFichier);
    }

    /**
     * 🎨 CRÉE LE TEMPLATE HTML POUR LA CONSULTATION
     * =============================================
     * Template professionnel avec CSS moderne
     */
    private String creerTemplateHtml(Consultation consultation) {
        String dateConsultation = consultation.getDateConsultation() != null 
                ? consultation.getDateConsultation().format(FORMAT_DATE) 
                : "Non spécifiée";
        
        String heureConsultation = consultation.getHeureConsultation() != null 
                ? consultation.getHeureConsultation().format(FORMAT_HEURE) 
                : "Non spécifiée";
        
        String examens = consultation.getExamensComplementaires() != null 
                ? consultation.getExamensComplementaires() 
                : "Aucun";
        
        String notes = consultation.getNotesConsultation() != null 
                ? consultation.getNotesConsultation() 
                : "Aucune note";
        
        String cout = consultation.getCoutConsultation() != null 
                ? String.format("%.2f TND", consultation.getCoutConsultation()) 
                : "Non spécifié";

        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Consultation Médicale</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            padding: 40px;\n" +
                "            color: #333;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        .header {\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 40px;\n" +
                "            border-bottom: 3px solid #2980b9;\n" +
                "            padding-bottom: 20px;\n" +
                "        }\n" +
                "        .header h1 {\n" +
                "            color: #2980b9;\n" +
                "            font-size: 32px;\n" +
                "            margin-bottom: 10px;\n" +
                "        }\n" +
                "        .header p {\n" +
                "            color: #7f8c8d;\n" +
                "            font-style: italic;\n" +
                "        }\n" +
                "        .section {\n" +
                "            margin-bottom: 30px;\n" +
                "        }\n" +
                "        .section-title {\n" +
                "            background-color: #3498db;\n" +
                "            color: white;\n" +
                "            padding: 10px 15px;\n" +
                "            font-size: 18px;\n" +
                "            font-weight: bold;\n" +
                "            margin-bottom: 15px;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .info-table {\n" +
                "            width: 100%;\n" +
                "            border-collapse: collapse;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .info-table tr {\n" +
                "            border-bottom: 1px solid #ecf0f1;\n" +
                "        }\n" +
                "        .info-table td {\n" +
                "            padding: 12px;\n" +
                "        }\n" +
                "        .info-table td:first-child {\n" +
                "            font-weight: bold;\n" +
                "            background-color: #ecf0f1;\n" +
                "            width: 35%;\n" +
                "        }\n" +
                "        .cout-box {\n" +
                "            background-color: #27ae60;\n" +
                "            color: white;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            font-size: 24px;\n" +
                "            font-weight: bold;\n" +
                "            border-radius: 5px;\n" +
                "            margin-top: 30px;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            margin-top: 50px;\n" +
                "            padding-top: 20px;\n" +
                "            border-top: 2px solid #ecf0f1;\n" +
                "            color: #7f8c8d;\n" +
                "            font-size: 12px;\n" +
                "            font-style: italic;\n" +
                "        }\n" +
                "        .mode-badge {\n" +
                "            display: inline-block;\n" +
                "            padding: 5px 15px;\n" +
                "            border-radius: 20px;\n" +
                "            font-size: 14px;\n" +
                "            font-weight: bold;\n" +
                "            margin-left: 10px;\n" +
                "        }\n" +
                "        .mode-presentiel { background-color: #3498db; color: white; }\n" +
                "        .mode-distance { background-color: #9b59b6; color: white; }\n" +
                "        .mode-teleconsultation { background-color: #e74c3c; color: white; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"header\">\n" +
                "        <h1>🏥 CONSULTATION MÉDICALE</h1>\n" +
                "        <p>VitaPlus - Système de Gestion Médicale</p>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"section\">\n" +
                "        <div class=\"section-title\">👤 INFORMATIONS DU PATIENT</div>\n" +
                "        <table class=\"info-table\">\n" +
                "            <tr>\n" +
                "                <td>Nom</td>\n" +
                "                <td>" + consultation.getNom() + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Prénom</td>\n" +
                "                <td>" + consultation.getPrenom() + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Date de consultation</td>\n" +
                "                <td>" + dateConsultation + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Heure de consultation</td>\n" +
                "                <td>" + heureConsultation + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Mode de consultation</td>\n" +
                "                <td>" + formatModeHtml(consultation.getModeConsultation()) + "</td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"section\">\n" +
                "        <div class=\"section-title\">🩺 DÉTAILS MÉDICAUX</div>\n" +
                "        <table class=\"info-table\">\n" +
                "            <tr>\n" +
                "                <td>Maladie</td>\n" +
                "                <td>" + consultation.getMaladie() + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Diagnostic</td>\n" +
                "                <td>" + consultation.getDiagnostic() + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Traitement</td>\n" +
                "                <td>" + consultation.getTraitement() + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Examens complémentaires</td>\n" +
                "                <td>" + examens + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td>Notes</td>\n" +
                "                <td>" + notes + "</td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"cout-box\">\n" +
                "        💰 COÛT DE LA CONSULTATION : " + cout + "\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"footer\">\n" +
                "        Document généré automatiquement par VitaPlus via PDFShift API\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 🎨 FORMATE LE MODE DE CONSULTATION AVEC BADGE HTML
     */
    private String formatModeHtml(String mode) {
        String classe = "";
        String texte = mode;
        
        switch (mode) {
            case "PRESENTIEL":
                classe = "mode-presentiel";
                texte = "🏥 Présentiel";
                break;
            case "A_DISTANCE":
                classe = "mode-distance";
                texte = "💻 À distance";
                break;
            case "TELECONSULTATION":
                classe = "mode-teleconsultation";
                texte = "📹 Téléconsultation";
                break;
        }
        
        return "<span class=\"mode-badge " + classe + "\">" + texte + "</span>";
    }

    /**
     * 🌐 ENVOIE LA REQUÊTE À L'API PDFSHIFT
     * =====================================
     * 
     * @param htmlContent Le contenu HTML à convertir en PDF
     * @return Les bytes du PDF généré
     * @throws Exception Si l'API retourne une erreur
     */
    private byte[] envoyerRequeteApi(String htmlContent) throws Exception {
        System.out.println("📡 Envoi de la requête à PDFShift API...");
        System.out.println("🔑 Utilisation de la clé API : " + apiKey.substring(0, 15) + "...");
        
        // Créer la connexion HTTP
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Configuration de la requête
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            
            // Authentification Basic Auth selon la doc PDFShift
            // Format: "api_key:" (clé API suivie de deux-points, pas de mot de passe)
            String auth = apiKey + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            System.out.println("🔐 En-tête Authorization configuré");
            
            // Corps de la requête JSON
            String jsonBody = "{\n" +
                    "  \"source\": " + escapeJson(htmlContent) + ",\n" +
                    "  \"landscape\": false,\n" +
                    "  \"use_print\": false\n" +
                    "}";
            
            // Envoyer la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Vérifier le code de réponse
            int responseCode = connection.getResponseCode();
            System.out.println("📥 Code de réponse API : " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lire la réponse (le PDF en bytes)
                try (InputStream is = connection.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    
                    System.out.println("✓ PDF reçu de l'API (" + baos.size() + " bytes)");
                    return baos.toByteArray();
                }
            } else {
                // Lire le message d'erreur
                try (InputStream es = connection.getErrorStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    
                    throw new IOException("Erreur API PDFShift (code " + responseCode + ") : " + errorMessage.toString());
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 💾 SAUVEGARDE LE PDF SUR LE DISQUE
     * ==================================
     * 
     * @param pdfBytes Les bytes du PDF
     * @param cheminFichier Le chemin où sauvegarder
     * @throws IOException Si l'écriture échoue
     */
    private void sauvegarderPdf(byte[] pdfBytes, String cheminFichier) throws IOException {
        File fichier = new File(cheminFichier);
        Files.write(fichier.toPath(), pdfBytes);
        System.out.println("💾 PDF sauvegardé : " + cheminFichier);
    }

    /**
     * 🔧 ÉCHAPPE LE HTML POUR LE JSON
     * ===============================
     * Remplace les caractères spéciaux pour éviter les erreurs JSON
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
}
