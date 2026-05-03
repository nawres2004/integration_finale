package tn.esprit.suivie_nawres.services;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.suivie_nawres.models.RendezVous;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🔔 SERVICE DE NOTIFICATIONS
 * ===========================
 * Gère les notifications popup pour les rendez-vous à venir
 */
public class NotificationService {

    private final RendezVousService rendezVousService;
    private final ScheduledExecutorService scheduler;
    private Stage primaryStage;
    private int medecinId;
    private static final int MINUTES_AVANT = 15; // Notification 15 minutes avant

    public NotificationService() {
        this.rendezVousService = new RendezVousService();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Démarrer le service de notifications
     */
    public void demarrer(Stage stage, int medecinId) {
        this.primaryStage = stage;
        this.medecinId = medecinId;
        
        System.out.println("\n");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔔 SERVICE DE NOTIFICATIONS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ Démarré pour le médecin ID: " + medecinId);
        System.out.println("⏰ Vérification toutes les 1 minute");
        System.out.println("📢 Notification " + MINUTES_AVANT + " minutes avant le RDV");
        System.out.println("🚨 Notification urgente 0-2 minutes avant");
        
        if (primaryStage != null) {
            System.out.println("✅ Stage OK - Position: x=" + primaryStage.getX() + ", y=" + primaryStage.getY());
            System.out.println("✅ Stage OK - Taille: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
        } else {
            System.err.println("❌ ERREUR: primaryStage est NULL !");
        }
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("\n");
        
        // Vérifier immédiatement au démarrage
        System.out.println("🚀 Première vérification immédiate...");
        verifierRendezVousProches();
        
        // Vérifier les RDV toutes les minutes
        scheduler.scheduleAtFixedRate(this::verifierRendezVousProches, 1, 1, TimeUnit.MINUTES);
        System.out.println("⏰ Planification activée : vérification toutes les 60 secondes\n");
    }

    /**
     * Arrêter le service de notifications
     */
    public void arreter() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("🔔 Service de notifications arrêté");
        }
    }

    /**
     * Vérifier les rendez-vous proches
     */
    private void verifierRendezVousProches() {
        try {
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("🔍 VÉRIFICATION DES RDV PROCHES");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            List<RendezVous> tousLesRdv = rendezVousService.afficherRendezVous();
            System.out.println("📋 Nombre total de RDV dans la base: " + tousLesRdv.size());
            
            LocalDateTime maintenant = LocalDateTime.now();
            LocalDate aujourdhui = LocalDate.now();
            System.out.println("📅 Date actuelle: " + aujourdhui.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            System.out.println("🕐 Heure actuelle: " + maintenant.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
            int rdvAujourdhui = 0;
            int rdvNotifies = 0;
            
            for (RendezVous rdv : tousLesRdv) {
                if (rdv.getDateRendezVous() != null && rdv.getHeureRendezVous() != null) {
                    LocalDateTime dateHeureRdv = LocalDateTime.of(
                        rdv.getDateRendezVous(),
                        rdv.getHeureRendezVous()
                    );
                    
                    // Calculer la différence en minutes
                    long minutesRestantes = java.time.Duration.between(maintenant, dateHeureRdv).toMinutes();
                    
                    // Debug : afficher tous les RDV du jour
                    if (rdv.getDateRendezVous().equals(aujourdhui)) {
                        rdvAujourdhui++;
                        System.out.println("\n  📅 RDV #" + rdvAujourdhui + ":");
                        System.out.println("     👤 Patient: " + rdv.getNom() + " " + rdv.getPrenom());
                        System.out.println("     🕐 Heure: " + rdv.getHeureRendezVous().format(DateTimeFormatter.ofPattern("HH:mm")));
                        System.out.println("     ⏱️  Dans: " + minutesRestantes + " minutes");
                        System.out.println("     📍 Mode: " + rdv.getModeConsultation());
                        
                        // Si le RDV est dans 15 minutes (±1 minute pour éviter les doublons)
                        if (minutesRestantes >= MINUTES_AVANT - 1 && minutesRestantes <= MINUTES_AVANT + 1) {
                            System.out.println("     🔔 → DÉCLENCHEMENT NOTIFICATION (15 min)");
                            rdvNotifies++;
                            Platform.runLater(() -> afficherNotification(rdv, (int) minutesRestantes));
                        }
                        // Si le RDV est maintenant (0-2 minutes)
                        else if (minutesRestantes >= 0 && minutesRestantes <= 2) {
                            System.out.println("     🚨 → DÉCLENCHEMENT NOTIFICATION URGENTE (maintenant)");
                            rdvNotifies++;
                            Platform.runLater(() -> afficherNotificationUrgente(rdv));
                        }
                        else if (minutesRestantes < 0) {
                            System.out.println("     ⏰ → RDV passé (il y a " + Math.abs(minutesRestantes) + " min)");
                        }
                        else {
                            System.out.println("     ⏳ → Pas encore l'heure de notifier");
                        }
                    }
                }
            }
            
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📊 RÉSUMÉ:");
            System.out.println("   • Total RDV dans la base: " + tousLesRdv.size());
            System.out.println("   • RDV aujourd'hui: " + rdvAujourdhui);
            System.out.println("   • Notifications envoyées: " + rdvNotifies);
            
            if (rdvAujourdhui == 0) {
                System.out.println("\n⚠️  AUCUN RDV AUJOURD'HUI");
                System.out.println("💡 Pour tester, créez un RDV avec cette requête SQL:");
                System.out.println("   INSERT INTO rendezvous (utilisateur_id, nom, prenom, date_rendez_vous, heure_rendez_vous,");
                System.out.println("   priorite, mode_consultation, statut_rendez_vous, pays, telephone)");
                System.out.println("   VALUES (1, 'Test', 'Patient', CURDATE(), ADDTIME(CURTIME(), '00:15:00'),");
                System.out.println("   'NORMALE', 'PRESENTIEL', 'ACCEPTE', 'Tunisie', '+216 12 345 678');");
            }
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR lors de la vérification des RDV:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Type: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println();
        }
    }

    /**
     * Afficher une notification popup
     */
    private void afficherNotification(RendezVous rdv, int minutes) {
        if (primaryStage == null) return;

        Popup popup = new Popup();
        
        // Conteneur principal
        VBox container = new VBox(12);
        container.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #3b82f6, #2563eb);" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 20, 0, 0, 10);" +
            "-fx-min-width: 350;" +
            "-fx-max-width: 350;"
        );
        
        // Icône et titre
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 32px;");
        
        Label titre = new Label("Rendez-vous dans " + minutes + " minutes");
        titre.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;"
        );
        
        header.getChildren().addAll(icon, titre);
        
        // Informations du patient
        Label patient = new Label("👤 " + rdv.getNom() + " " + rdv.getPrenom());
        patient.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );
        
        // Heure
        Label heure = new Label("🕐 " + rdv.getHeureRendezVous().format(DateTimeFormatter.ofPattern("HH:mm")));
        heure.setStyle(
            "-fx-text-fill: #dbeafe;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;"
        );
        
        // Mode
        String modeIcon = switch (rdv.getModeConsultation()) {
            case "PRESENTIEL" -> "🏥";
            case "A_DISTANCE" -> "💻";
            case "TELECONSULTATION" -> "📹";
            default -> "📋";
        };
        
        Label mode = new Label(modeIcon + " " + formatMode(rdv.getModeConsultation()));
        mode.setStyle(
            "-fx-text-fill: #dbeafe;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;"
        );
        
        // Téléphone
        Label telephone = new Label("📞 " + rdv.getTelephone());
        telephone.setStyle(
            "-fx-text-fill: #dbeafe;" +
            "-fx-font-size: 13px;"
        );
        
        // Notes si présentes
        if (rdv.getNotesRendezVous() != null && !rdv.getNotesRendezVous().isEmpty()) {
            Label notes = new Label("📝 " + rdv.getNotesRendezVous());
            notes.setStyle(
                "-fx-text-fill: #fef3c7;" +
                "-fx-font-size: 12px;" +
                "-fx-wrap-text: true;"
            );
            notes.setMaxWidth(310);
            container.getChildren().add(notes);
        }
        
        container.getChildren().addAll(header, patient, heure, mode, telephone);
        
        popup.getContent().add(container);
        
        // Positionner en haut à droite
        double x = primaryStage.getX() + primaryStage.getWidth() - 370;
        double y = primaryStage.getY() + 20;
        popup.show(primaryStage, x, y);
        
        // Jouer un son (optionnel)
        jouerSonNotification();
        
        // Fermer automatiquement après 10 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> popup.hide()));
        timeline.play();
        
        // Permettre de fermer en cliquant
        container.setOnMouseClicked(e -> popup.hide());
        
        System.out.println("🔔 Notification affichée pour RDV: " + rdv.getNom() + " " + rdv.getPrenom());
    }

    /**
     * Afficher une notification urgente (RDV maintenant)
     */
    private void afficherNotificationUrgente(RendezVous rdv) {
        if (primaryStage == null) return;

        Popup popup = new Popup();
        
        // Conteneur principal (rouge pour urgence)
        VBox container = new VBox(12);
        container.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ef4444, #dc2626);" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.5), 25, 0, 0, 10);" +
            "-fx-min-width: 350;" +
            "-fx-max-width: 350;"
        );
        
        // Animation de pulsation
        container.setStyle(container.getStyle() + "-fx-border-color: white; -fx-border-width: 3; -fx-border-radius: 16;");
        
        // Icône et titre
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("🚨");
        icon.setStyle("-fx-font-size: 32px;");
        
        Label titre = new Label("RENDEZ-VOUS MAINTENANT !");
        titre.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;"
        );
        
        header.getChildren().addAll(icon, titre);
        
        // Informations du patient
        Label patient = new Label("👤 " + rdv.getNom() + " " + rdv.getPrenom());
        patient.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );
        
        // Heure
        Label heure = new Label("🕐 " + rdv.getHeureRendezVous().format(DateTimeFormatter.ofPattern("HH:mm")));
        heure.setStyle(
            "-fx-text-fill: #fecaca;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;"
        );
        
        container.getChildren().addAll(header, patient, heure);
        
        popup.getContent().add(container);
        
        // Positionner en haut à droite
        double x = primaryStage.getX() + primaryStage.getWidth() - 370;
        double y = primaryStage.getY() + 20;
        popup.show(primaryStage, x, y);
        
        // Jouer un son d'urgence
        jouerSonUrgence();
        
        // Fermer automatiquement après 15 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(15), e -> popup.hide()));
        timeline.play();
        
        // Permettre de fermer en cliquant
        container.setOnMouseClicked(e -> popup.hide());
        
        System.out.println("🚨 Notification URGENTE affichée pour RDV: " + rdv.getNom() + " " + rdv.getPrenom());
    }

    /**
     * Jouer un son de notification
     */
    private void jouerSonNotification() {
        try {
            // Son système (beep)
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("❌ Impossible de jouer le son : " + e.getMessage());
        }
    }

    /**
     * Jouer un son d'urgence
     */
    private void jouerSonUrgence() {
        try {
            // Double beep pour urgence
            java.awt.Toolkit.getDefaultToolkit().beep();
            Thread.sleep(200);
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("❌ Impossible de jouer le son : " + e.getMessage());
        }
    }

    /**
     * Formater le mode de consultation
     */
    private String formatMode(String mode) {
        return switch (mode) {
            case "PRESENTIEL" -> "Présentiel";
            case "A_DISTANCE" -> "À distance";
            case "TELECONSULTATION" -> "Téléconsultation";
            default -> mode;
        };
    }

    /**
     * Tester une notification (pour debug)
     */
    public void testerNotification() {
        System.out.println("\n🧪 TEST DE NOTIFICATION");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (primaryStage == null) {
            System.err.println("❌ primaryStage est null !");
            return;
        }
        
        System.out.println("✅ primaryStage OK");
        System.out.println("📐 Position: x=" + primaryStage.getX() + ", y=" + primaryStage.getY());
        System.out.println("📏 Taille: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
        
        // Créer un RDV de test
        RendezVous rdvTest = new RendezVous();
        rdvTest.setNom("Test");
        rdvTest.setPrenom("Patient");
        rdvTest.setDateRendezVous(LocalDate.now());
        rdvTest.setHeureRendezVous(LocalTime.now().plusMinutes(15));
        rdvTest.setModeConsultation("PRESENTIEL");
        rdvTest.setTelephone("+216 12 345 678");
        rdvTest.setNotesRendezVous("Ceci est une notification de test");
        
        System.out.println("📋 RDV de test créé");
        System.out.println("👤 Patient: " + rdvTest.getNom() + " " + rdvTest.getPrenom());
        System.out.println("🕐 Heure: " + rdvTest.getHeureRendezVous());
        
        Platform.runLater(() -> {
            System.out.println("🚀 Affichage de la notification...");
            afficherNotification(rdvTest, 15);
        });
        
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
