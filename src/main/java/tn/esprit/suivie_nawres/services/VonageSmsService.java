package tn.esprit.suivie_nawres.services;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import tn.esprit.suivie_nawres.models.RendezVous;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * 📱 SERVICE SMS VONAGE - Gestion de l'envoi de SMS via Vonage (Nexmo)
 * 
 * Ce service permet d'envoyer des SMS aux patients pour :
 * 1. Confirmer un rendez-vous créé par le médecin
 * 2. Notifier l'acceptation d'un rendez-vous par le médecin
 * 
 * ✅ AVANTAGE : Vonage supporte l'envoi de SMS vers la Tunisie !
 * 
 * Configuration requise dans vonage.properties :
 * - vonage.api.key : Votre API Key Vonage
 * - vonage.api.secret : Votre API Secret Vonage
 * - vonage.from.name : Nom de l'expéditeur (max 11 caractères)
 * - vonage.enabled : true/false pour activer/désactiver l'envoi
 */
public class VonageSmsService {
    
    private static final String CONFIG_FILE = "vonage.properties";
    private static String API_KEY;
    private static String API_SECRET;
    private static String FROM_NAME;
    private static boolean SMS_ENABLED;
    private static boolean isInitialized = false;
    private static VonageClient vonageClient;
    
    /**
     * 🔧 INITIALISE LA CONFIGURATION VONAGE
     * 
     * Charge les paramètres depuis vonage.properties
     * Cette méthode est appelée automatiquement au premier envoi de SMS
     */
    private static void initializeVonage() {
        if (isInitialized) {
            return; // Déjà initialisé
        }
        
        try (InputStream input = VonageSmsService.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("❌ Fichier de configuration Vonage introuvable : " + CONFIG_FILE);
                SMS_ENABLED = false;
                return;
            }
            
            Properties prop = new Properties();
            prop.load(input);
            
            API_KEY = prop.getProperty("vonage.api.key");
            API_SECRET = prop.getProperty("vonage.api.secret");
            FROM_NAME = prop.getProperty("vonage.from.name", "VitaPlus");
            SMS_ENABLED = Boolean.parseBoolean(prop.getProperty("vonage.enabled", "false"));
            
            // Vérifie que les paramètres sont configurés
            if (SMS_ENABLED && (API_KEY == null || API_SECRET == null ||
                API_KEY.equals("VOTRE_API_KEY") || 
                API_SECRET.equals("VOTRE_API_SECRET"))) {
                System.err.println("⚠️ Configuration Vonage incomplète. SMS désactivés.");
                SMS_ENABLED = false;
            }
            
            if (SMS_ENABLED) {
                // Initialise le client Vonage
                vonageClient = VonageClient.builder()
                    .apiKey(API_KEY)
                    .apiSecret(API_SECRET)
                    .build();
                System.out.println("✅ Service SMS Vonage initialisé avec succès");
                System.out.println("📤 Expéditeur : " + FROM_NAME);
            } else {
                System.out.println("ℹ️ Service SMS désactivé (vonage.enabled=false ou configuration incomplète)");
            }
            
            isInitialized = true;
            
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la configuration Vonage : " + e.getMessage());
            SMS_ENABLED = false;
        }
    }
    
    /**
     * 📤 ENVOIE UN SMS À UN NUMÉRO DE TÉLÉPHONE
     * 
     * @param numeroDestinataire Numéro de téléphone du destinataire (format international avec +)
     * @param message Contenu du SMS à envoyer
     * @return true si l'envoi a réussi, false sinon
     */
    private static boolean envoyerSms(String numeroDestinataire, String message) {
        // Initialise Vonage si ce n'est pas déjà fait
        if (!isInitialized) {
            initializeVonage();
        }
        
        // Vérifie si le service SMS est activé
        if (!SMS_ENABLED) {
            System.out.println("ℹ️ SMS non envoyé (service désactivé)");
            System.out.println("📱 Destinataire : " + numeroDestinataire);
            System.out.println("📝 Message : " + message);
            return false;
        }
        
        try {
            // Nettoie le numéro de téléphone (enlève les espaces)
            String numeroNettoye = numeroDestinataire.replaceAll("\\s+", "");
            
            // Vérifie que le numéro commence par +
            if (!numeroNettoye.startsWith("+")) {
                System.err.println("❌ Numéro de téléphone invalide (doit commencer par +) : " + numeroDestinataire);
                return false;
            }
            
            // Crée le message SMS
            TextMessage textMessage = new TextMessage(FROM_NAME, numeroNettoye, message);
            
            // Envoie le SMS via Vonage
            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(textMessage);
            
            // Vérifie le statut de l'envoi
            if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
                System.out.println("✅ SMS envoyé avec succès !");
                System.out.println("📱 Destinataire : " + numeroDestinataire);
                System.out.println("🆔 Message ID : " + response.getMessages().get(0).getId());
                System.out.println("💰 Prix : " + response.getMessages().get(0).getMessagePrice() + " EUR");
                return true;
            } else {
                System.err.println("❌ Échec de l'envoi du SMS");
                System.err.println("📱 Destinataire : " + numeroDestinataire);
                System.err.println("⚠️ Erreur : " + response.getMessages().get(0).getErrorText());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 📧 ENVOIE UN SMS DE CONFIRMATION DE RENDEZ-VOUS (MÉDECIN CRÉE LE RDV)
     * 
     * Cette méthode est appelée quand un MÉDECIN crée un rendez-vous
     * Le patient reçoit immédiatement un SMS avec les détails
     * 
     * @param rendezVous Le rendez-vous créé par le médecin
     * @return true si l'envoi a réussi, false sinon
     */
    public static boolean envoyerSmsConfirmationRendezVous(RendezVous rendezVous) {
        // Formate la date et l'heure
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String dateFormatee = rendezVous.getDateRendezVous().format(dateFormatter);
        String heureFormatee = rendezVous.getHeureRendezVous().format(heureFormatter);
        
        // Construit le message SMS
        String message = String.format(
            "VitaPlus - Confirmation de rendez-vous\n\n" +
            "Bonjour %s %s,\n\n" +
            "Votre rendez-vous a ete confirme :\n" +
            "Date : %s\n" +
            "Heure : %s\n" +
            "Mode : %s\n" +
            "Priorite : %s\n\n" +
            "A bientot !",
            rendezVous.getPrenom(),
            rendezVous.getNom(),
            dateFormatee,
            heureFormatee,
            formatModeConsultation(rendezVous.getModeConsultation()),
            formatPriorite(rendezVous.getPriorite())
        );
        
        // Envoie le SMS
        return envoyerSms(rendezVous.getTelephone(), message);
    }
    
    /**
     * ✅ ENVOIE UN SMS D'ACCEPTATION DE RENDEZ-VOUS (MÉDECIN ACCEPTE LE RDV)
     * 
     * Cette méthode est appelée quand un MÉDECIN accepte un rendez-vous en attente
     * Le patient reçoit un SMS de notification
     * 
     * @param rendezVous Le rendez-vous accepté par le médecin
     * @return true si l'envoi a réussi, false sinon
     */
    public static boolean envoyerSmsAcceptationRendezVous(RendezVous rendezVous) {
        // Formate la date et l'heure
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String dateFormatee = rendezVous.getDateRendezVous().format(dateFormatter);
        String heureFormatee = rendezVous.getHeureRendezVous().format(heureFormatter);
        
        // Construit le message SMS
        String message = String.format(
            "VitaPlus - Rendez-vous accepte\n\n" +
            "Bonjour %s %s,\n\n" +
            "Bonne nouvelle ! Votre demande de rendez-vous a ete acceptee :\n" +
            "Date : %s\n" +
            "Heure : %s\n" +
            "Mode : %s\n" +
            "Priorite : %s\n\n" +
            "Nous vous attendons !",
            rendezVous.getPrenom(),
            rendezVous.getNom(),
            dateFormatee,
            heureFormatee,
            formatModeConsultation(rendezVous.getModeConsultation()),
            formatPriorite(rendezVous.getPriorite())
        );
        
        // Envoie le SMS
        return envoyerSms(rendezVous.getTelephone(), message);
    }
    
    /**
     * 🔄 FORMATE LE MODE DE CONSULTATION POUR L'AFFICHAGE
     */
    private static String formatModeConsultation(String mode) {
        if (mode == null) return "Non specifie";
        
        switch (mode) {
            case "A_DISTANCE":
                return "A distance";
            case "PRESENTIEL":
                return "Presentiel";
            case "TELECONSULTATION":
                return "Teleconsultation";
            default:
                return mode;
        }
    }
    
    /**
     * 🔄 FORMATE LA PRIORITÉ POUR L'AFFICHAGE
     */
    private static String formatPriorite(String priorite) {
        if (priorite == null) return "Non specifiee";
        
        switch (priorite) {
            case "BASSE":
                return "Basse";
            case "NORMALE":
                return "Normale";
            case "HAUTE":
                return "Haute";
            case "URGENTE":
                return "Urgente";
            default:
                return priorite;
        }
    }
    
    /**
     * 🧪 MÉTHODE DE TEST POUR VÉRIFIER LA CONFIGURATION
     * 
     * Envoie un SMS de test pour vérifier que tout fonctionne
     * 
     * @param numeroTest Numéro de téléphone pour le test (format international avec +)
     * @return true si le test a réussi, false sinon
     */
    public static boolean envoyerSmsTest(String numeroTest) {
        String message = "Test SMS VitaPlus\n\nSi vous recevez ce message, la configuration Vonage fonctionne correctement !";
        return envoyerSms(numeroTest, message);
    }
    
    /**
     * 💰 VÉRIFIE LE SOLDE DU COMPTE VONAGE
     * 
     * Affiche le crédit restant sur votre compte Vonage
     */
    public static void verifierSolde() {
        if (!isInitialized) {
            initializeVonage();
        }
        
        if (!SMS_ENABLED) {
            System.out.println("ℹ️ Service SMS désactivé");
            return;
        }
        
        try {
            var balanceResponse = vonageClient.getAccountClient().getBalance();
            double balance = balanceResponse.getValue();
            System.out.println("💰 Solde Vonage : " + balance + " EUR");
            
            if (balance < 0.50) {
                System.out.println("⚠️ Attention : Solde faible ! Rechargez votre compte Vonage.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du solde : " + e.getMessage());
        }
    }
}
