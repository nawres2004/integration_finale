package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.services.NotificationService;
import tn.esprit.suivie_nawres.utils.MedecinNavigationContext;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.SceneManager;
import tn.esprit.suivie_nawres.utils.UserRole;

public class DashboardMedecinController {
    @FXML
    private StackPane contentPane;
    
    private NotificationService notificationService;
    private static final int MEDECIN_ID = 1; // TODO: Récupérer l'ID du médecin connecté

    @FXML
    private void initialize() {
        RoleContext.setCurrentRole(UserRole.MEDECIN);
        MedecinNavigationContext.setContentContainer(contentPane);
        
        // Attendre que la scène soit prête avant de démarrer les notifications
        contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                demarrerNotifications();
            }
        });
    }
    
    /**
     * 🔔 Démarrer le service de notifications
     */
    private void demarrerNotifications() {
        try {
            System.out.println("\n🚀 Tentative de démarrage du service de notifications...");
            
            if (contentPane.getScene() == null) {
                System.err.println("⚠️ Scène non disponible, attente...");
                return;
            }
            
            if (contentPane.getScene().getWindow() == null) {
                System.err.println("⚠️ Fenêtre non disponible, attente...");
                return;
            }
            
            Stage stage = (Stage) contentPane.getScene().getWindow();
            
            if (stage == null) {
                System.err.println("❌ ERREUR: Impossible de récupérer le Stage");
                return;
            }
            
            System.out.println("✅ Stage récupéré avec succès");
            System.out.println("   Position: x=" + stage.getX() + ", y=" + stage.getY());
            System.out.println("   Taille: " + stage.getWidth() + "x" + stage.getHeight());
            
            notificationService = new NotificationService();
            notificationService.demarrer(stage, MEDECIN_ID);
            
            System.out.println("✅ Service de notifications démarré avec succès\n");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du démarrage des notifications:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Type: " + e.getClass().getName());
            e.printStackTrace();
        }
    }
    
    /**
     * 🧪 Tester les notifications (pour debug)
     */
    @FXML
    private void testerNotification() {
        System.out.println("\n");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🧪 TEST DE NOTIFICATION DEMANDÉ");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (notificationService == null) {
            System.err.println("⚠️ Service de notifications non initialisé");
            System.out.println("🔄 Tentative de redémarrage...");
            
            demarrerNotifications();
            
            // Attendre un peu pour que le service démarre
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if (notificationService != null) {
            System.out.println("✅ Service disponible, envoi de la notification de test...");
            notificationService.testerNotification();
            System.out.println("✅ Notification de test envoyée");
        } else {
            System.err.println("❌ ERREUR: Impossible de démarrer le service de notifications");
            System.err.println("💡 Vérifiez que vous êtes bien sur le Dashboard Médecin");
        }
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("\n");
    }

    @FXML
    private void gererRendezVous() {
        ouvrirModale("/views/AfficherRendezVous.fxml", "📅 Mes Rendez-vous", 1400, 850);
    }

    @FXML
    private void traiterDemandesRendezVous() {
        SceneManager.replaceContent(contentPane, "/views/TraiterDemandesRendezVous.fxml");
    }

    @FXML
    private void ajouterRendezVous() {
        ouvrirModale("/views/AjouterRendezVous.fxml", "Creer Rendez-vous", 860, 760);
    }

    @FXML
    private void gererConsultations() {
        ouvrirModale("/views/AfficherConsultation.fxml", "🏥 Mes Consultations", 1400, 850);
    }

    @FXML
    private void ajouterConsultation() {
        ouvrirModale("/views/AjouterConsultation.fxml", "Creer Consultation", 900, 760);
    }

    @FXML
    private void afficherCalendrier() {
        SceneManager.replaceContent(contentPane, "/views/Calendrier.fxml");
    }

    @FXML
    private void retourChoixRole() {
        SceneManager.show("/views/ChoixRole.fxml", "VitaPlus Medical - Choix du role");
    }

    private void ouvrirModale(String fxmlPath, String titre, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.NONE); // Permet de fermer la fenêtre librement
            stage.setTitle(titre);
            stage.setScene(scene);
            stage.show(); // show() au lieu de showAndWait() pour ne pas bloquer
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible d'ouvrir la fenetre : " + fxmlPath, exception);
        }
    }
}

