package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 🎨 CONTRÔLEUR MODERNE POUR L'AFFICHAGE DES RENDEZ-VOUS
 * 
 * Affiche les rendez-vous sous forme de cartes élégantes au lieu d'un tableau
 */
public class AfficherRendezVousModerneController {
    
    @FXML private TextField txtRechercheId;
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private ComboBox<String> comboFiltreMode;
    @FXML private Label lblMessage;
    @FXML private VBox cardsContainer;
    @FXML private ScrollPane scrollPane;
    
    private final RendezVousService rendezVousService = new RendezVousService();
    private final ObservableList<RendezVous> sourceRendezVous = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    @FXML
    private void initialize() {
        initialiserFiltres();
        actualiser();
        
        // Ajouter des listeners pour filtrage en temps réel
        txtRechercheId.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Texte changé: " + newValue);
            afficherCartes();
        });
        
        comboFiltreStatut.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Statut changé: " + newValue);
            afficherCartes();
        });
        
        comboFiltreMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Mode changé: " + newValue);
            afficherCartes();
        });
    }
    
    @FXML
    private void actualiser() {
        try {
            if (RoleContext.getCurrentRole() == UserRole.PATIENT) {
                chargerRendezVousPatient();
            } else {
                List<RendezVous> rendezVous = rendezVousService.afficherRendezVous();
                sourceRendezVous.setAll(rendezVous);
            }
            afficherCartes();
            afficherMessage("✓ " + sourceRendezVous.size() + " rendez-vous chargé(s)");
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors du chargement des rendez-vous");
        }
    }
    
    @FXML
    private void rechercher() {
        System.out.println("=== RECHERCHE DÉCLENCHÉE ===");
        System.out.println("Texte recherche: " + (txtRechercheId.getText() != null ? txtRechercheId.getText() : "null"));
        System.out.println("Statut sélectionné: " + comboFiltreStatut.getValue());
        System.out.println("Mode sélectionné: " + comboFiltreMode.getValue());
        System.out.println("Nombre total de RDV: " + sourceRendezVous.size());
        afficherCartes();
    }
    
    @FXML
    private void reinitialiserFiltres() {
        txtRechercheId.clear();
        comboFiltreStatut.getSelectionModel().select("Tous");
        comboFiltreMode.getSelectionModel().select("Tous");
        afficherCartes();
    }
    
    /**
     * ✖ FERME LA FENÊTRE
     */
    @FXML
    private void fermerFenetre() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) cardsContainer.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la fenêtre : " + e.getMessage());
        }
    }
    
    /**
     * 🎨 AFFICHE LES RENDEZ-VOUS SOUS FORME DE CARTES
     */
    private void afficherCartes() {
        cardsContainer.getChildren().clear();
        
        // Applique les filtres
        List<RendezVous> rendezVousFiltres = filtrerRendezVous();
        
        if (rendezVousFiltres.isEmpty()) {
            afficherMessageVide();
            return;
        }
        
        // Crée une carte pour chaque rendez-vous
        for (RendezVous rdv : rendezVousFiltres) {
            HBox carte = creerCarteRendezVous(rdv);
            cardsContainer.getChildren().add(carte);
        }
        
        afficherMessage("✓ " + rendezVousFiltres.size() + " rendez-vous affiché(s)");
    }
    
    /**
     * 🎨 CRÉE UNE CARTE POUR UN RENDEZ-VOUS
     */
    private HBox creerCarteRendezVous(RendezVous rdv) {
        HBox carte = new HBox();
        carte.getStyleClass().add("rdv-card");
        
        // Panneau latéral gauche (coloré selon le statut)
        VBox panneauGauche = creerPanneauGauche(rdv);
        
        // Contenu principal
        VBox contenu = creerContenuCarte(rdv);
        
        carte.getChildren().addAll(panneauGauche, contenu);
        HBox.setHgrow(contenu, Priority.ALWAYS);
        
        return carte;
    }
    
    /**
     * 🎨 CRÉE LE PANNEAU LATÉRAL GAUCHE (COLORÉ)
     */
    private VBox creerPanneauGauche(RendezVous rdv) {
        VBox panneau = new VBox(12);
        panneau.setAlignment(Pos.CENTER);
        panneau.getStyleClass().add("rdv-card-left");
        
        // Ajoute la classe CSS selon le statut
        StatutRendezVous statut = rdv.getStatutRendezVous();
        if (statut == StatutRendezVous.ACCEPTE) {
            panneau.getStyleClass().add("rdv-card-left-accepte");
        } else if (statut == StatutRendezVous.REFUSE) {
            panneau.getStyleClass().add("rdv-card-left-refuse");
        } else {
            panneau.getStyleClass().add("rdv-card-left-attente");
        }
        
        // Nom du patient
        Label nomPatient = new Label(rdv.getPrenom() + "\n" + rdv.getNom());
        nomPatient.getStyleClass().add("rdv-patient-name");
        nomPatient.setWrapText(true);
        nomPatient.setAlignment(Pos.CENTER);
        nomPatient.setMaxWidth(Double.MAX_VALUE);
        
        // Badge de date
        Label dateBadge = new Label("📅 " + rdv.getDateRendezVous().format(dateFormatter));
        dateBadge.getStyleClass().add("rdv-date-badge");
        
        // Badge d'heure
        Label heureBadge = new Label("🕐 " + rdv.getHeureRendezVous().format(heureFormatter));
        heureBadge.getStyleClass().add("rdv-time-badge");
        
        panneau.getChildren().addAll(nomPatient, dateBadge, heureBadge);
        
        return panneau;
    }
    
    /**
     * 🎨 CRÉE LE CONTENU PRINCIPAL DE LA CARTE
     */
    private VBox creerContenuCarte(RendezVous rdv) {
        VBox contenu = new VBox(14);
        contenu.getStyleClass().add("rdv-card-content");
        
        // Ligne 1 : Badges (Priorité, Statut, Mode)
        HBox badges = creerLigneBadges(rdv);
        
        // Ligne 2 : Informations (Pays, Téléphone)
        VBox infos = creerLigneInfos(rdv);
        
        // Ligne 3 : Notes
        VBox notes = creerLigneNotes(rdv);
        
        // Ligne 4 : Actions
        HBox actions = creerLigneActions(rdv);
        
        contenu.getChildren().addAll(badges, infos, notes, actions);
        
        return contenu;
    }
    
    /**
     * 🎨 CRÉE LA LIGNE DES BADGES
     */
    private HBox creerLigneBadges(RendezVous rdv) {
        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER_LEFT);
        
        // Badge de priorité
        Label prioriteBadge = new Label(formatPriorite(rdv.getPriorite()));
        prioriteBadge.getStyleClass().addAll("rdv-priorite-badge", getClassePriorite(rdv.getPriorite()));
        
        // Badge de statut
        Label statutBadge = new Label(formatStatut(rdv.getStatutRendezVous()));
        statutBadge.getStyleClass().addAll("rdv-statut-badge", getClasseStatut(rdv.getStatutRendezVous()));
        
        // Badge de mode
        Label modeBadge = new Label(formatMode(rdv.getModeConsultation()));
        modeBadge.getStyleClass().addAll("rdv-mode-badge", getClasseMode(rdv.getModeConsultation()));
        
        badges.getChildren().addAll(prioriteBadge, statutBadge, modeBadge);
        
        return badges;
    }
    
    /**
     * 🎨 CRÉE LA LIGNE DES INFORMATIONS
     */
    private VBox creerLigneInfos(RendezVous rdv) {
        VBox infos = new VBox(8);
        
        // Pays
        HBox paysRow = new HBox(8);
        paysRow.setAlignment(Pos.CENTER_LEFT);
        Label paysIcon = new Label("🌍");
        paysIcon.getStyleClass().add("rdv-info-icon");
        Label paysLabel = new Label("Pays :");
        paysLabel.getStyleClass().add("rdv-info-label");
        Label paysValue = new Label(rdv.getPays());
        paysValue.getStyleClass().add("rdv-info-value");
        paysRow.getChildren().addAll(paysIcon, paysLabel, paysValue);
        
        // Téléphone
        HBox telRow = new HBox(8);
        telRow.setAlignment(Pos.CENTER_LEFT);
        Label telIcon = new Label("📱");
        telIcon.getStyleClass().add("rdv-info-icon");
        Label telLabel = new Label("Téléphone :");
        telLabel.getStyleClass().add("rdv-info-label");
        Label telValue = new Label(rdv.getTelephone());
        telValue.getStyleClass().add("rdv-info-value");
        telRow.getChildren().addAll(telIcon, telLabel, telValue);
        
        infos.getChildren().addAll(paysRow, telRow);
        
        return infos;
    }
    
    /**
     * 🎨 CRÉE LA LIGNE DES NOTES
     */
    private VBox creerLigneNotes(RendezVous rdv) {
        VBox notesBox = new VBox(6);
        
        Label notesLabel = new Label("📝 Notes :");
        notesLabel.getStyleClass().add("rdv-info-label");
        
        Label notesValue = new Label(rdv.getNotesRendezVous());
        notesValue.getStyleClass().add("rdv-notes");
        notesValue.setWrapText(true);
        
        notesBox.getChildren().addAll(notesLabel, notesValue);
        
        return notesBox;
    }
    
    /**
     * 🎨 CRÉE LA LIGNE DES ACTIONS (BOUTONS)
     */
    private HBox creerLigneActions(RendezVous rdv) {
        HBox actions = new HBox(10);
        actions.getStyleClass().add("rdv-actions");
        actions.setAlignment(Pos.CENTER_LEFT);
        
        UserRole role = RoleContext.getCurrentRole();
        StatutRendezVous statut = rdv.getStatutRendezVous();
        boolean enAttente = statut == StatutRendezVous.EN_ATTENTE;
        
        // Boutons Accepter/Refuser (médecin uniquement ET seulement si EN_ATTENTE)
        // Les RDV créés par le médecin sont déjà ACCEPTE, donc pas besoin de ces boutons
        if (role == UserRole.MEDECIN && enAttente) {
            Button btnAccepter = new Button("✓ Accepter");
            btnAccepter.getStyleClass().add("rdv-btn-accepter");
            btnAccepter.setOnAction(e -> accepterRendezVous(rdv));
            actions.getChildren().add(btnAccepter);
            
            Button btnRefuser = new Button("✗ Refuser");
            btnRefuser.getStyleClass().add("rdv-btn-refuser");
            btnRefuser.setOnAction(e -> refuserRendezVous(rdv));
            actions.getChildren().add(btnRefuser);
        }
        
        // Bouton Modifier
        // Médecin : peut modifier tous les RDV
        // Patient : peut modifier uniquement les RDV en attente
        if (role == UserRole.MEDECIN || (role == UserRole.PATIENT && enAttente)) {
            Button btnModifier = new Button("✏ Modifier");
            btnModifier.getStyleClass().add("rdv-btn-modifier");
            btnModifier.setOnAction(e -> modifierRendezVous(rdv));
            actions.getChildren().add(btnModifier);
        }
        
        // Bouton Supprimer
        // Médecin : peut supprimer tous les RDV
        // Patient : peut supprimer uniquement les RDV en attente
        if (role == UserRole.MEDECIN || (role == UserRole.PATIENT && enAttente)) {
            Button btnSupprimer = new Button("🗑 Supprimer");
            btnSupprimer.getStyleClass().add("rdv-btn-supprimer");
            btnSupprimer.setOnAction(e -> supprimerRendezVous(rdv));
            actions.getChildren().add(btnSupprimer);
        }
        
        // Si aucun bouton n'a été ajouté, affiche un message
        if (actions.getChildren().isEmpty()) {
            Label lblAucuneAction = new Label("✓ Rendez-vous confirmé");
            lblAucuneAction.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px;");
            actions.getChildren().add(lblAucuneAction);
        }
        
        return actions;
    }
    
    /**
     * 🔄 FILTRE LES RENDEZ-VOUS SELON LES CRITÈRES
     */
    private List<RendezVous> filtrerRendezVous() {
        String recherche = txtRechercheId.getText() != null ? txtRechercheId.getText().trim().toLowerCase() : "";
        String statut = comboFiltreStatut.getValue();
        String mode = comboFiltreMode.getValue();
        
        System.out.println("=== FILTRAGE ===");
        System.out.println("Recherche: '" + recherche + "'");
        System.out.println("Statut: " + statut);
        System.out.println("Mode: " + mode);
        
        List<RendezVous> resultats = sourceRendezVous.stream()
            .filter(rdv -> {
                // Filtre de recherche
                boolean matchRecherche = recherche.isEmpty()
                    || (rdv.getNom() != null && rdv.getNom().toLowerCase().contains(recherche))
                    || (rdv.getPrenom() != null && rdv.getPrenom().toLowerCase().contains(recherche))
                    || (rdv.getNotesRendezVous() != null && rdv.getNotesRendezVous().toLowerCase().contains(recherche));
                
                // Filtre de statut
                boolean matchStatut = statut == null || "Tous".equals(statut)
                    || (rdv.getStatutRendezVous() != null && rdv.getStatutRendezVous().name().equals(statut));
                
                // Filtre de mode
                boolean matchMode = mode == null || "Tous".equals(mode)
                    || (rdv.getModeConsultation() != null && rdv.getModeConsultation().equals(mode));
                
                boolean match = matchRecherche && matchStatut && matchMode;
                
                if (!match) {
                    System.out.println("RDV filtré: " + rdv.getNom() + " " + rdv.getPrenom() + 
                                     " (recherche:" + matchRecherche + ", statut:" + matchStatut + ", mode:" + matchMode + ")");
                }
                
                return match;
            })
            .collect(Collectors.toList());
        
        System.out.println("Résultats filtrés: " + resultats.size() + " / " + sourceRendezVous.size());
        
        return resultats;
    }
    
    /**
     * ✅ ACCEPTE UN RENDEZ-VOUS
     */
    private void accepterRendezVous(RendezVous rdv) {
        try {
            rendezVousService.changerStatutRendezVous(rdv.getUtilisateurId(), StatutRendezVous.ACCEPTE);
            actualiser();
            afficherMessageSucces("✓ Rendez-vous accepté");
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors de l'acceptation");
        }
    }
    
    /**
     * ✗ REFUSE UN RENDEZ-VOUS
     */
    private void refuserRendezVous(RendezVous rdv) {
        try {
            rendezVousService.changerStatutRendezVous(rdv.getUtilisateurId(), StatutRendezVous.REFUSE);
            actualiser();
            afficherMessageSucces("✓ Rendez-vous refusé");
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors du refus");
        }
    }
    
    /**
     * ✏ MODIFIE UN RENDEZ-VOUS
     */
    private void modifierRendezVous(RendezVous rdv) {
        try {
            AjouterRendezVousController.setRendezVousAEditer(rdv);
            AjouterRendezVousController.setFermerApresEnregistrement(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterRendezVous.fxml"));
            Scene scene = new Scene(loader.load(), 860, 760);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Rendez-vous");
            stage.setScene(scene);
            stage.showAndWait();
            actualiser();
        } catch (Exception e) {
            afficherMessageErreur("❌ Impossible d'ouvrir la modification");
        }
    }
    
    /**
     * 🗑 SUPPRIME UN RENDEZ-VOUS
     */
    private void supprimerRendezVous(RendezVous rdv) {
        try {
            rendezVousService.supprimerRendezVous(rdv.getUtilisateurId());
            actualiser();
            afficherMessageSucces("✓ Rendez-vous supprimé");
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors de la suppression");
        }
    }
    
    /**
     * 🔧 INITIALISE LES FILTRES
     */
    private void initialiserFiltres() {
        comboFiltreStatut.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "ACCEPTE", "REFUSE"));
        comboFiltreStatut.getSelectionModel().select("Tous");
        
        comboFiltreMode.setItems(FXCollections.observableArrayList("Tous", "A_DISTANCE", "PRESENTIEL", "TELECONSULTATION"));
        comboFiltreMode.getSelectionModel().select("Tous");
    }
    
    /**
     * 📥 CHARGE LES RENDEZ-VOUS DU PATIENT
     */
    private void chargerRendezVousPatient() throws Exception {
        Integer utilisateurId = RoleContext.getCurrentUtilisateurId();
        if (utilisateurId == null) {
            sourceRendezVous.clear();
            return;
        }
        rendezVousService.chercherParId(utilisateurId).ifPresent(rdv -> sourceRendezVous.setAll(rdv));
    }
    
    /**
     * 📝 AFFICHE UN MESSAGE VIDE
     */
    private void afficherMessageVide() {
        Label messageVide = new Label("📭 Aucun rendez-vous à afficher");
        messageVide.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-padding: 40px;");
        cardsContainer.getChildren().add(messageVide);
        cardsContainer.setAlignment(Pos.CENTER);
    }
    
    /**
     * 📝 AFFICHE UN MESSAGE
     */
    private void afficherMessage(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("status-message-success", "status-message-error", "status-message-warning");
        lblMessage.getStyleClass().add("status-message");
    }
    
    private void afficherMessageSucces(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("status-message", "status-message-error", "status-message-warning");
        lblMessage.getStyleClass().add("status-message-success");
    }
    
    private void afficherMessageErreur(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("status-message", "status-message-success", "status-message-warning");
        lblMessage.getStyleClass().add("status-message-error");
    }
    
    // ========================================
    // MÉTHODES DE FORMATAGE
    // ========================================
    
    private String formatPriorite(String priorite) {
        switch (priorite) {
            case "URGENTE": return "🔴 Urgente";
            case "HAUTE": return "🟠 Haute";
            case "NORMALE": return "🔵 Normale";
            case "BASSE": return "🟣 Basse";
            default: return priorite;
        }
    }
    
    private String formatStatut(StatutRendezVous statut) {
        switch (statut) {
            case ACCEPTE: return "✓ Accepté";
            case REFUSE: return "✗ Refusé";
            case EN_ATTENTE: return "⏳ En attente";
            default: return statut.name();
        }
    }
    
    private String formatMode(String mode) {
        switch (mode) {
            case "PRESENTIEL": return "🏥 Présentiel";
            case "A_DISTANCE": return "💻 À distance";
            case "TELECONSULTATION": return "📹 Téléconsultation";
            default: return mode;
        }
    }
    
    private String getClassePriorite(String priorite) {
        switch (priorite) {
            case "URGENTE": return "rdv-priorite-urgente";
            case "HAUTE": return "rdv-priorite-haute";
            case "NORMALE": return "rdv-priorite-normale";
            case "BASSE": return "rdv-priorite-basse";
            default: return "";
        }
    }
    
    private String getClasseStatut(StatutRendezVous statut) {
        switch (statut) {
            case ACCEPTE: return "rdv-statut-accepte";
            case REFUSE: return "rdv-statut-refuse";
            case EN_ATTENTE: return "rdv-statut-attente";
            default: return "";
        }
    }
    
    private String getClasseMode(String mode) {
        switch (mode) {
            case "PRESENTIEL": return "rdv-mode-presentiel";
            case "A_DISTANCE": return "rdv-mode-distance";
            case "TELECONSULTATION": return "rdv-mode-teleconsultation";
            default: return "";
        }
    }
}
