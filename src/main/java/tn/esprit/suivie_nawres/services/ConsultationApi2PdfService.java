package tn.esprit.suivie_nawres.services;

import tn.esprit.suivie_nawres.models.Consultation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * 📄 SERVICE PDF AVEC API2PDF
 * ============================
 * Ce service utilise l'API API2PDF pour générer des PDF professionnels
 * à partir de templates HTML.
 * 
 * 🌐 API EXTERNE : API2PDF (https://www.api2pdf.com/)
 * - Convertit du HTML en PDF de haute qualité
 * - Plus fiable que PDFShift
 * - 100 conversions gratuites par mois
 * 
 * 🔑 AUTHENTIFICATION :
 * - Clé API stockée dans api2pdf.properties
 * - Authentification via header Authorization
 */
public class ConsultationApi2PdfService {

    private static final String CONFIG_FILE = "/api2pdf.properties";
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");
    
    private String apiKey;
    private String apiUrl;

    /**
     * 🔧 CONSTRUCTEUR : Charge la configuration
     */
    public ConsultationApi2PdfService() {
        chargerConfiguration();
    }

    /**
     * 📋 CHARGE LA CONFIGURATION DEPUIS api2pdf.properties
     */
    private void chargerConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration introuvable : " + CONFIG_FILE);
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            this.apiKey = prop.getProperty("api2pdf.api.key");
            this.apiUrl = prop.getProperty("api2pdf.api.url");
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Clé API API2PDF manquante dans la configuration");
            }
            
            System.out.println("✓ Configuration API2PDF chargée avec succès");
            System.out.println("🔑 Clé API : " + apiKey.substring(0, 8) + "...");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la configuration API2PDF : " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration API2PDF", e);
        }
    }

    /**
     * 🎯 MÉTHODE PRINCIPALE : GÉNÉRER LE PDF VIA API
     * ==============================================
     */
    public void genererPdfConsultation(Consultation consultation, String cheminFichier) throws Exception {
        System.out.println("🚀 Génération du PDF via API2PDF...");
        
        // ✅ ÉTAPE 1 : Créer le template HTML
        String htmlContent = creerTemplateHtml(consultation);
        
        // ✅ ÉTAPE 2 : Envoyer à l'API API2PDF
        String pdfUrl = envoyerRequeteApi(htmlContent);
        
        // ✅ ÉTAPE 3 : Télécharger le PDF
        telechargerPdf(pdfUrl, cheminFichier);
        
        System.out.println("✓ PDF généré avec succès : " + cheminFichier);
    }

    /**
     * 🎨 CRÉE LE TEMPLATE HTML POUR LA CONSULTATION
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
                "        Document généré automatiquement par VitaPlus via API2PDF\n" +
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
     * 🌐 ENVOIE LA REQUÊTE À L'API API2PDF
     */
    private String envoyerRequeteApi(String htmlContent) throws Exception {
        System.out.println("📡 Envoi de la requête à API2PDF...");
        
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", apiKey);
            
            System.out.println("🔐 En-tête Authorization configuré");
            
            // Corps de la requête JSON pour API2PDF
            String jsonBody = "{\n" +
                    "  \"html\": " + escapeJson(htmlContent) + ",\n" +
                    "  \"inlinePdf\": true,\n" +
                    "  \"fileName\": \"consultation.pdf\"\n" +
                    "}";
            
            // Envoyer la requête
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
                    
                    // Parser la réponse JSON pour extraire l'URL du PDF
                    String jsonResponse = response.toString();
                    String pdfUrl = extrairePdfUrl(jsonResponse);
                    
                    System.out.println("✓ URL du PDF reçue : " + pdfUrl);
                    return pdfUrl;
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder errorMessage = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorMessage.append(line);
                    }
                    
                    throw new IOException("Erreur API API2PDF (code " + responseCode + ") : " + errorMessage.toString());
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 📥 TÉLÉCHARGE LE PDF DEPUIS L'URL
     */
    private void telechargerPdf(String pdfUrl, String cheminFichier) throws Exception {
        System.out.println("📥 Téléchargement du PDF...");
        
        URL url = new URL(pdfUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            
            try (InputStream is = connection.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                
                byte[] pdfBytes = baos.toByteArray();
                System.out.println("✓ PDF téléchargé (" + pdfBytes.length + " bytes)");
                
                // Sauvegarder le PDF
                File fichier = new File(cheminFichier);
                Files.write(fichier.toPath(), pdfBytes);
                System.out.println("💾 PDF sauvegardé : " + cheminFichier);
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 🔍 EXTRAIT L'URL DU PDF DEPUIS LA RÉPONSE JSON
     */
    private String extrairePdfUrl(String jsonResponse) {
        // Simple extraction de l'URL du PDF depuis la réponse JSON
        // Format: {"pdf":"https://..."}
        int start = jsonResponse.indexOf("\"pdf\":\"") + 7;
        int end = jsonResponse.indexOf("\"", start);
        return jsonResponse.substring(start, end).replace("\\", "");
    }

    /**
     * 🔧 ÉCHAPPE LE HTML POUR LE JSON
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
