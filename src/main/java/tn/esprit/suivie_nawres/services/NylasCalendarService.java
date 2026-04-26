package tn.esprit.suivie_nawres.services;

import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.models.RendezVous;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;

/**
 * 📅 SERVICE NYLAS CALENDAR API
 * =============================
 * Ce service permet de synchroniser les rendez-vous et consultations
 * avec Nylas Calendar (qui peut se connecter à Google, Outlook, etc.)
 * 
 * 🌐 API EXTERNE : Nylas Calendar API
 * - Simple à utiliser (juste une clé API)
 * - Synchronise avec Google Calendar, Outlook, etc.
 * - 100 requêtes/mois gratuit
 * 
 * 🔑 AUTHENTIFICATION :
 * - Access Token Nylas stocké dans nylas-calendar.properties
 * - Authentification via header Bearer
 */
public class NylasCalendarService {

    private static final String CONFIG_FILE = "/nylas-calendar.properties";
    
    private String apiKey;
    private String apiUrl;

    /**
     * 🔧 CONSTRUCTEUR : Charge la configuration
     */
    public NylasCalendarService() {
        chargerConfiguration();
    }

    /**
     * 📋 CHARGE LA CONFIGURATION
     */
    private void chargerConfiguration() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("Fichier de configuration introuvable : " + CONFIG_FILE);
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            this.apiKey = prop.getProperty("nylas.api.key");
            this.apiUrl = prop.getProperty("nylas.api.url");
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("Clé API Nylas manquante");
            }
            
            System.out.println("✓ Configuration Nylas Calendar chargée");
            System.out.println("🔑 Clé API : " + apiKey.substring(0, 15) + "...");
            
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement config Nylas : " + e.getMessage());
            throw new RuntimeException("Impossible de charger la configuration", e);
        }
    }

    /**
     * 📅 CRÉER UN ÉVÉNEMENT POUR UN RENDEZ-VOUS
     * =========================================
     */
    public String creerEvenementRendezVous(RendezVous rdv) throws Exception {
        System.out.println("📅 Création événement Nylas Calendar pour RDV...");
        
        // Construire le titre
        String titre = String.format("RDV - %s %s", 
                rdv.getPrenom() != null ? rdv.getPrenom() : "",
                rdv.getNom() != null ? rdv.getNom() : "");
        
        // Construire la description
        String description = String.format(
                "Rendez-vous médical\\n" +
                "Patient: %s %s\\n" +
                "Mode: %s\\n" +
                "Statut: %s\\n" +
                "Notes: %s",
                rdv.getPrenom(), rdv.getNom(),
                rdv.getModeConsultation(),
                rdv.getStatutRendezVous(),
                rdv.getNotesRendezVous() != null ? rdv.getNotesRendezVous() : "Aucune"
        );
        
        // Dates et heures
        LocalDate date = rdv.getDateRendezVous();
        LocalTime heure = rdv.getHeureRendezVous();
        
        return creerEvenement(titre, description, date, heure, 60); // 60 min par défaut
    }

    /**
     * 🩺 CRÉER UN ÉVÉNEMENT POUR UNE CONSULTATION
     * ===========================================
     */
    public String creerEvenementConsultation(Consultation consultation) throws Exception {
        System.out.println("🩺 Création événement Nylas Calendar pour consultation...");
        
        // Construire le titre
        String titre = String.format("Consultation - %s %s", 
                consultation.getPrenom() != null ? consultation.getPrenom() : "",
                consultation.getNom() != null ? consultation.getNom() : "");
        
        // Construire la description
        String description = String.format(
                "Consultation médicale\\n" +
                "Patient: %s %s\\n" +
                "Maladie: %s\\n" +
                "Diagnostic: %s\\n" +
                "Traitement: %s\\n" +
                "Mode: %s\\n" +
                "Coût: %.2f TND",
                consultation.getPrenom(), consultation.getNom(),
                consultation.getMaladie(),
                consultation.getDiagnostic(),
                consultation.getTraitement(),
                consultation.getModeConsultation(),
                consultation.getCoutConsultation()
        );
        
        // Dates et heures
        LocalDate date = consultation.getDateConsultation();
        LocalTime heure = consultation.getHeureConsultation();
        
        return creerEvenement(titre, description, date, heure, 45); // 45 min par défaut
    }

    /**
     * 📝 CRÉER UN ÉVÉNEMENT GÉNÉRIQUE
     * ===============================
     */
    private String creerEvenement(String titre, String description, 
                                   LocalDate date, LocalTime heure, int dureeMinutes) throws Exception {
        
        // Convertir en timestamp Unix (secondes depuis 1970)
        ZonedDateTime debut = ZonedDateTime.of(date, heure, ZoneId.of("Africa/Tunis"));
        ZonedDateTime fin = debut.plusMinutes(dureeMinutes);
        
        long timestampDebut = debut.toEpochSecond();
        long timestampFin = fin.toEpochSecond();
        
        // URL de l'API Nylas
        String urlStr = apiUrl + "/v3/grants/me/events";
        
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            
            System.out.println("🔐 En-tête Authorization configuré");
            
            // Corps de la requête JSON pour Nylas
            String jsonBody = String.format(
                    "{\n" +
                    "  \"title\": \"%s\",\n" +
                    "  \"description\": \"%s\",\n" +
                    "  \"when\": {\n" +
                    "    \"start_time\": %d,\n" +
                    "    \"end_time\": %d\n" +
                    "  },\n" +
                    "  \"participants\": [],\n" +
                    "  \"busy\": true,\n" +
                    "  \"reminders\": {\n" +
                    "    \"use_default\": false,\n" +
                    "    \"overrides\": [\n" +
                    "      {\"reminder_minutes\": 1440, \"reminder_method\": \"email\"},\n" +
                    "      {\"reminder_minutes\": 60, \"reminder_method\": \"popup\"}\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}",
                    titre, description, timestampDebut, timestampFin
            );
            
            System.out.println("📤 Envoi requête à Nylas Calendar...");
            
            // Envoyer la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("📥 Code de réponse : " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    System.out.println("✓ Événement créé avec succès");
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
                    
                    throw new IOException("Erreur Nylas Calendar (code " + responseCode + ") : " + errorMessage.toString());
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 📋 LISTER LES ÉVÉNEMENTS
     * ========================
     */
    public String listerEvenements(LocalDate dateDebut, LocalDate dateFin) throws Exception {
        System.out.println("📋 Récupération des événements Nylas Calendar...");
        
        // Convertir en timestamp Unix
        long timestampDebut = dateDebut.atStartOfDay(ZoneId.of("Africa/Tunis")).toEpochSecond();
        long timestampFin = dateFin.atTime(23, 59, 59).atZone(ZoneId.of("Africa/Tunis")).toEpochSecond();
        
        String urlStr = String.format(
                "%s/v3/grants/me/events?start=%d&end=%d",
                apiUrl, timestampDebut, timestampFin
        );
        
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    System.out.println("✓ Événements récupérés");
                    return response.toString();
                }
            } else {
                throw new IOException("Erreur récupération événements (code " + responseCode + ")");
            }
            
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 🗑️ SUPPRIMER UN ÉVÉNEMENT
     * =========================
     */
    public void supprimerEvenement(String eventId) throws Exception {
        System.out.println("🗑️ Suppression événement Nylas Calendar...");
        
        String urlStr = apiUrl + "/v3/grants/me/events/" + eventId;
        
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("✓ Événement supprimé");
            } else {
                throw new IOException("Erreur suppression événement (code " + responseCode + ")");
            }
            
        } finally {
            connection.disconnect();
        }
    }
}
