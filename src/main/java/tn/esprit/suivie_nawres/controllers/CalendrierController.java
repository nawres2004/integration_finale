package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.services.ConsultationService;
import tn.esprit.suivie_nawres.services.NylasCalendarService;
import tn.esprit.suivie_nawres.services.RendezVousService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 📅 CONTROLLER DU CALENDRIER MÉDICAL
 * ===================================
 * Ce controller gère l'affichage du calendrier avec :
 * - Les rendez-vous existants
 * - Les consultations existantes
 * - Navigation mois par mois
 * - Création de nouveau rendez-vous
 * 
 * 💡 ANALOGIE :
 * C'est comme un agenda papier, mais numérique !
 * - Tu vois tous les jours du mois
 * - Les jours avec RDV/consultations sont colorés
 * - Tu peux cliquer sur un jour pour voir les détails
 */
public class CalendrierController {

    @FXML private Label lblMoisAnnee;
    @FXML private GridPane gridCalendrier;
    @FXML private VBox vboxDetails;
    @FXML private Label lblDetailsDate;
    @FXML private Label lblDetailsContenu;

    private final RendezVousService rendezVousService = new RendezVousService();
    private final ConsultationService consultationService = new ConsultationService();
    private final NylasCalendarService nylasCalendarService = new NylasCalendarService();
    
    private YearMonth moisActuel;
    private LocalDate dateSelectionnee;
    private List<RendezVous> tousLesRendezVous;
    private List<Consultation> toutesLesConsultations;

    /**
     * 🎬 INITIALISATION DU CALENDRIER
     * ===============================
     * Appelée automatiquement au chargement de la vue
     */
    @FXML
    private void initialize() {
        // Définir le mois actuel (aujourd'hui)
        moisActuel = YearMonth.now();
        
        // Charger les données depuis la base
        chargerDonnees();
        
        // Afficher le calendrier
        afficherCalendrier();
    }

    /**
     * 📊 CHARGER LES DONNÉES DEPUIS LA BASE
     * =====================================
     * Récupère tous les rendez-vous et consultations
     */
    private void chargerDonnees() {
        try {
            tousLesRendezVous = rendezVousService.afficherRendezVous();
            toutesLesConsultations = consultationService.afficherConsultations();
        } catch (Exception e) {
            System.err.println("Erreur chargement données : " + e.getMessage());
            tousLesRendezVous = List.of();
            toutesLesConsultations = List.of();
        }
    }

    /**
     * 📅 AFFICHER LE CALENDRIER
     * ========================
     * Crée la grille du calendrier avec tous les jours du mois
     */
    private void afficherCalendrier() {
        // Vider la grille
        gridCalendrier.getChildren().clear();
        
        // Afficher le mois et l'année en haut
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        lblMoisAnnee.setText(moisActuel.format(formatter).toUpperCase());
        
        // Ajouter les en-têtes des jours (Lun, Mar, Mer, etc.)
        String[] joursEnTete = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label lblJour = new Label(joursEnTete[i]);
            lblJour.getStyleClass().add("calendrier-entete-jour");
            lblJour.setMaxWidth(Double.MAX_VALUE);
            lblJour.setAlignment(Pos.CENTER);
            gridCalendrier.add(lblJour, i, 0);
        }
        
        // Calculer le premier jour du mois
        LocalDate premierJour = moisActuel.atDay(1);
        int jourSemaine = premierJour.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche
        
        // Calculer le nombre de jours dans le mois
        int nombreJours = moisActuel.lengthOfMonth();
        
        // Remplir le calendrier
        int ligne = 1;
        int colonne = jourSemaine - 1; // Ajuster pour commencer à Lundi
        
        for (int jour = 1; jour <= nombreJours; jour++) {
            LocalDate date = moisActuel.atDay(jour);
            
            // Créer le bouton pour ce jour
            Button btnJour = creerBoutonJour(date);
            
            // Ajouter le bouton à la grille
            gridCalendrier.add(btnJour, colonne, ligne);
            
            // Passer au jour suivant
            colonne++;
            if (colonne > 6) {
                colonne = 0;
                ligne++;
            }
        }
    }

    /**
     * 🔘 CRÉER UN BOUTON POUR UN JOUR
     * ===============================
     * Crée un bouton stylisé pour chaque jour du calendrier
     * 
     * @param date La date du jour
     * @return Le bouton créé
     */
    private Button creerBoutonJour(LocalDate date) {
        Button btn = new Button(String.valueOf(date.getDayOfMonth()));
        btn.getStyleClass().add("calendrier-jour");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setMaxHeight(Double.MAX_VALUE);
        
        // Compter les rendez-vous et consultations pour ce jour
        int nbRendezVous = compterRendezVous(date);
        int nbConsultations = compterConsultations(date);
        
        // Appliquer un style selon le contenu
        if (nbRendezVous > 0 && nbConsultations > 0) {
            // Jour avec RDV ET consultations
            btn.getStyleClass().add("calendrier-jour-mixte");
        } else if (nbRendezVous > 0) {
            // Jour avec seulement des RDV
            btn.getStyleClass().add("calendrier-jour-rdv");
        } else if (nbConsultations > 0) {
            // Jour avec seulement des consultations
            btn.getStyleClass().add("calendrier-jour-consultation");
        }
        
        // Marquer aujourd'hui
        if (date.equals(LocalDate.now())) {
            btn.getStyleClass().add("calendrier-jour-aujourd-hui");
        }
        
        // Ajouter un tooltip (info-bulle)
        if (nbRendezVous > 0 || nbConsultations > 0) {
            String tooltipText = String.format("%d RDV, %d Consultations", nbRendezVous, nbConsultations);
            Tooltip tooltip = new Tooltip(tooltipText);
            btn.setTooltip(tooltip);
        }
        
        // Action au clic : afficher les détails
        btn.setOnAction(e -> afficherDetails(date));
        
        return btn;
    }

    /**
     * 🔢 COMPTER LES RENDEZ-VOUS POUR UNE DATE
     * ========================================
     */
    private int compterRendezVous(LocalDate date) {
        return (int) tousLesRendezVous.stream()
                .filter(rdv -> rdv.getDateRendezVous() != null && rdv.getDateRendezVous().equals(date))
                .count();
    }

    /**
     * 🔢 COMPTER LES CONSULTATIONS POUR UNE DATE
     * ==========================================
     */
    private int compterConsultations(LocalDate date) {
        return (int) toutesLesConsultations.stream()
                .filter(c -> c.getDateConsultation() != null && c.getDateConsultation().equals(date))
                .count();
    }

    /**
     * 📋 AFFICHER LES DÉTAILS D'UN JOUR
     * =================================
     * Affiche la liste des RDV et consultations pour un jour donné
     */
    private void afficherDetails(LocalDate date) {
        dateSelectionnee = date;
        
        // Formater la date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        lblDetailsDate.setText(date.format(formatter));
        
        // Récupérer les RDV et consultations pour ce jour
        List<RendezVous> rdvDuJour = tousLesRendezVous.stream()
                .filter(rdv -> rdv.getDateRendezVous() != null && rdv.getDateRendezVous().equals(date))
                .collect(Collectors.toList());
        
        List<Consultation> consultationsDuJour = toutesLesConsultations.stream()
                .filter(c -> c.getDateConsultation() != null && c.getDateConsultation().equals(date))
                .collect(Collectors.toList());
        
        // Construire le texte des détails
        StringBuilder details = new StringBuilder();
        
        // Ajouter les rendez-vous
        if (!rdvDuJour.isEmpty()) {
            details.append("📅 RENDEZ-VOUS (" + rdvDuJour.size() + ")\n");
            details.append("─".repeat(40)).append("\n");
            for (RendezVous rdv : rdvDuJour) {
                details.append(String.format("• %s - %s %s\n",
                        rdv.getHeureRendezVous() != null ? rdv.getHeureRendezVous().toString() : "??:??",
                        rdv.getNom() != null ? rdv.getNom() : "?",
                        rdv.getPrenom() != null ? rdv.getPrenom() : "?"));
                details.append(String.format("  Statut: %s | Mode: %s\n",
                        rdv.getStatutRendezVous() != null ? rdv.getStatutRendezVous().name() : "?",
                        rdv.getModeConsultation() != null ? rdv.getModeConsultation() : "?"));
                details.append("\n");
            }
        }
        
        // Ajouter les consultations
        if (!consultationsDuJour.isEmpty()) {
            details.append("🩺 CONSULTATIONS (" + consultationsDuJour.size() + ")\n");
            details.append("─".repeat(40)).append("\n");
            for (Consultation c : consultationsDuJour) {
                details.append(String.format("• %s - %s %s\n",
                        c.getHeureConsultation() != null ? c.getHeureConsultation().toString() : "??:??",
                        c.getNom() != null ? c.getNom() : "?",
                        c.getPrenom() != null ? c.getPrenom() : "?"));
                details.append(String.format("  Maladie: %s | Mode: %s\n",
                        c.getMaladie() != null ? c.getMaladie() : "?",
                        c.getModeConsultation() != null ? c.getModeConsultation() : "?"));
                details.append("\n");
            }
        }
        
        // Si aucun événement
        if (rdvDuJour.isEmpty() && consultationsDuJour.isEmpty()) {
            details.append("Aucun rendez-vous ou consultation pour ce jour.");
        }
        
        lblDetailsContenu.setText(details.toString());
        
        // Rendre visible le panneau de détails
        vboxDetails.setVisible(true);
    }

    /**
     * ⬅️ MOIS PRÉCÉDENT
     * =================
     */
    @FXML
    private void moisPrecedent() {
        moisActuel = moisActuel.minusMonths(1);
        afficherCalendrier();
        vboxDetails.setVisible(false);
    }

    /**
     * ➡️ MOIS SUIVANT
     * ===============
     */
    @FXML
    private void moisSuivant() {
        moisActuel = moisActuel.plusMonths(1);
        afficherCalendrier();
        vboxDetails.setVisible(false);
    }

    /**
     * 🔄 ACTUALISER
     * ============
     * Recharge les données et rafraîchit le calendrier
     */
    @FXML
    private void actualiser() {
        chargerDonnees();
        afficherCalendrier();
        if (dateSelectionnee != null) {
            afficherDetails(dateSelectionnee);
        }
    }

    /**
     * ➕ CRÉER UN NOUVEAU RENDEZ-VOUS
     * ===============================
     * Ouvre la fenêtre de création de rendez-vous
     * ET synchronise avec Nylas Calendar
     */
    @FXML
    private void creerRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterRendezVous.fxml"));
            Scene scene = new Scene(loader.load(), 860, 760);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Créer un Rendez-vous");
            stage.setScene(scene);
            stage.showAndWait();
            
            // Actualiser après la création
            actualiser();
            
            // Synchroniser avec Nylas Calendar
            synchroniserAvecNylas();
        } catch (Exception e) {
            System.err.println("Erreur ouverture fenêtre : " + e.getMessage());
        }
    }

    /**
     * ➕ CRÉER UNE NOUVELLE CONSULTATION
     * ==================================
     * Ouvre la fenêtre de création de consultation
     * ET synchronise avec Nylas Calendar
     */
    @FXML
    private void creerConsultation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterConsultation.fxml"));
            Scene scene = new Scene(loader.load(), 900, 760);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Créer une Consultation");
            stage.setScene(scene);
            stage.showAndWait();
            
            // Actualiser après la création
            actualiser();
            
            // Synchroniser avec Nylas Calendar
            synchroniserAvecNylas();
        } catch (Exception e) {
            System.err.println("Erreur ouverture fenêtre : " + e.getMessage());
        }
    }

    /**
     * 🔄 SYNCHRONISER AVEC NYLAS CALENDAR
     * ===================================
     * Envoie tous les RDV et consultations vers Nylas Calendar
     */
    private void synchroniserAvecNylas() {
        try {
            System.out.println("🔄 Synchronisation avec Nylas Calendar...");
            
            // Synchroniser les rendez-vous
            for (RendezVous rdv : tousLesRendezVous) {
                try {
                    nylasCalendarService.creerEvenementRendezVous(rdv);
                } catch (Exception e) {
                    System.err.println("Erreur sync RDV : " + e.getMessage());
                }
            }
            
            // Synchroniser les consultations
            for (Consultation c : toutesLesConsultations) {
                try {
                    nylasCalendarService.creerEvenementConsultation(c);
                } catch (Exception e) {
                    System.err.println("Erreur sync consultation : " + e.getMessage());
                }
            }
            
            System.out.println("✓ Synchronisation terminée");
        } catch (Exception e) {
            System.err.println("Erreur synchronisation Nylas Calendar : " + e.getMessage());
        }
    }

    /**
     * 📅 AUJOURD'HUI
     * =============
     * Revenir au mois actuel
     */
    @FXML
    private void aujourdhui() {
        moisActuel = YearMonth.now();
        afficherCalendrier();
        afficherDetails(LocalDate.now());
    }
}
