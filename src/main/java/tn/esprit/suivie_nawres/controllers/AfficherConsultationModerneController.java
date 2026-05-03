package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.services.ConsultationApi2PdfService;
import tn.esprit.suivie_nawres.services.ConsultationService;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🎨 CONTRÔLEUR MODERNE POUR L'AFFICHAGE DES CONSULTATIONS
 */
public class AfficherConsultationModerneController {
    
    @FXML private TextField txtRechercheId;
    @FXML private ComboBox<String> comboFiltreMode;
    @FXML private Label lblMessage;
    @FXML private VBox cardsContainer;
    @FXML private ScrollPane scrollPane;
    
    private final ConsultationService consultationService = new ConsultationService();
    private final ConsultationApi2PdfService pdfService = new ConsultationApi2PdfService();
    private final ObservableList<Consultation> sourceConsultations = FXCollections.observableArrayList();
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
        
        comboFiltreMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Mode changé: " + newValue);
            afficherCartes();
        });
    }
    
    @FXML
    private void actualiser() {
        try {
            List<Consultation> consultations = consultationService.afficherConsultations();
            sourceConsultations.setAll(consultations);
            afficherCartes();
            afficherMessage("✓ " + sourceConsultations.size() + " consultation(s) chargée(s)");
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors du chargement des consultations");
        }
    }
    
    @FXML
    private void rechercher() {
        System.out.println("=== RECHERCHE CONSULTATIONS DÉCLENCHÉE ===");
        System.out.println("Texte recherche: " + (txtRechercheId.getText() != null ? txtRechercheId.getText() : "null"));
        System.out.println("Mode sélectionné: " + comboFiltreMode.getValue());
        System.out.println("Nombre total de consultations: " + sourceConsultations.size());
        afficherCartes();
    }
    
    @FXML
    private void reinitialiserFiltres() {
        txtRechercheId.clear();
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
    
    private void afficherCartes() {
        cardsContainer.getChildren().clear();
        
        List<Consultation> consultationsFiltrees = filtrerConsultations();
        
        if (consultationsFiltrees.isEmpty()) {
            afficherMessageVide();
            return;
        }
        
        for (Consultation consultation : consultationsFiltrees) {
            VBox carte = creerCarteConsultation(consultation);
            cardsContainer.getChildren().add(carte);
        }
        
        afficherMessage("✓ " + consultationsFiltrees.size() + " consultation(s) affichée(s)");
    }
    
    private VBox creerCarteConsultation(Consultation consultation) {
        VBox carte = new VBox(10);
        carte.getStyleClass().add("consultation-card-compact");
        
        // Ligne 1 : En-tête (Nom + Date/Heure + Badge Mode)
        HBox ligne1 = new HBox(15);
        ligne1.setAlignment(Pos.CENTER_LEFT);
        
        Label nom = new Label("👤 " + consultation.getPrenom() + " " + consultation.getNom());
        nom.getStyleClass().add("consultation-nom-compact");
        
        Label dateHeure = new Label("📅 " + consultation.getDateConsultation().format(dateFormatter) + 
                                   " • 🕐 " + consultation.getHeureConsultation().format(heureFormatter));
        dateHeure.getStyleClass().add("consultation-date-compact");
        
        Label modeBadge = new Label(formatMode(consultation.getModeConsultation()));
        modeBadge.getStyleClass().addAll("consultation-badge-compact", getClasseMode(consultation.getModeConsultation()));
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        ligne1.getChildren().addAll(nom, dateHeure, spacer1, modeBadge);
        
        // Ligne 2 : Informations médicales (Maladie + Diagnostic)
        HBox ligne2 = new HBox(20);
        ligne2.setAlignment(Pos.CENTER_LEFT);
        
        HBox maladieBox = new HBox(5);
        maladieBox.setAlignment(Pos.CENTER_LEFT);
        Label maladieLabel = new Label("🦠 Maladie:");
        maladieLabel.getStyleClass().add("consultation-label-compact");
        Label maladieValue = new Label(consultation.getMaladie());
        maladieValue.getStyleClass().add("consultation-maladie-compact");
        maladieBox.getChildren().addAll(maladieLabel, maladieValue);
        
        HBox diagnosticBox = new HBox(5);
        diagnosticBox.setAlignment(Pos.CENTER_LEFT);
        Label diagnosticLabel = new Label("🔬 Diagnostic:");
        diagnosticLabel.getStyleClass().add("consultation-label-compact");
        Label diagnosticValue = new Label(consultation.getDiagnostic());
        diagnosticValue.getStyleClass().add("consultation-value-compact");
        diagnosticValue.setMaxWidth(300);
        diagnosticValue.setWrapText(false);
        diagnosticValue.setStyle("-fx-text-overflow: ellipsis;");
        diagnosticBox.getChildren().addAll(diagnosticLabel, diagnosticValue);
        
        ligne2.getChildren().addAll(maladieBox, diagnosticBox);
        
        // Ligne 3 : Traitement + Coût + Actions
        HBox ligne3 = new HBox(20);
        ligne3.setAlignment(Pos.CENTER_LEFT);
        
        HBox traitementBox = new HBox(5);
        traitementBox.setAlignment(Pos.CENTER_LEFT);
        Label traitementLabel = new Label("💊 Traitement:");
        traitementLabel.getStyleClass().add("consultation-label-compact");
        Label traitementValue = new Label(consultation.getTraitement());
        traitementValue.getStyleClass().add("consultation-value-compact");
        traitementValue.setMaxWidth(250);
        traitementValue.setWrapText(false);
        traitementValue.setStyle("-fx-text-overflow: ellipsis;");
        traitementBox.getChildren().addAll(traitementLabel, traitementValue);
        
        HBox coutBox = new HBox(5);
        coutBox.setAlignment(Pos.CENTER_LEFT);
        Label coutLabel = new Label("💰");
        coutLabel.getStyleClass().add("consultation-label-compact");
        Label coutValue = new Label(String.format("%.2f DT", consultation.getCoutConsultation()));
        coutValue.getStyleClass().add("consultation-cout-compact");
        coutBox.getChildren().addAll(coutLabel, coutValue);
        
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        
        // Actions compactes
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnModifier = new Button("✏");
        btnModifier.getStyleClass().add("consultation-btn-icon");
        btnModifier.setTooltip(new Tooltip("Modifier"));
        btnModifier.setOnAction(e -> modifierConsultation(consultation));
        
        Button btnSupprimer = new Button("🗑");
        btnSupprimer.getStyleClass().add("consultation-btn-icon");
        btnSupprimer.setTooltip(new Tooltip("Supprimer"));
        btnSupprimer.setOnAction(e -> supprimerConsultation(consultation));
        
        Button btnPdf = new Button("📄");
        btnPdf.getStyleClass().add("consultation-btn-icon");
        btnPdf.setTooltip(new Tooltip("Générer PDF"));
        btnPdf.setOnAction(e -> telechargerPdf(consultation));
        
        actions.getChildren().addAll(btnModifier, btnSupprimer, btnPdf);
        
        ligne3.getChildren().addAll(traitementBox, coutBox, spacer3, actions);
        
        carte.getChildren().addAll(ligne1, ligne2, ligne3);
        
        return carte;
    }
    
    private List<Consultation> filtrerConsultations() {
        String recherche = txtRechercheId.getText() != null ? txtRechercheId.getText().trim().toLowerCase() : "";
        String mode = comboFiltreMode.getValue();
        
        System.out.println("=== FILTRAGE CONSULTATIONS ===");
        System.out.println("Recherche: '" + recherche + "'");
        System.out.println("Mode: " + mode);
        
        List<Consultation> resultats = sourceConsultations.stream()
            .filter(c -> {
                boolean matchRecherche = recherche.isEmpty()
                    || (c.getNom() != null && c.getNom().toLowerCase().contains(recherche))
                    || (c.getPrenom() != null && c.getPrenom().toLowerCase().contains(recherche))
                    || (c.getMaladie() != null && c.getMaladie().toLowerCase().contains(recherche));
                
                boolean matchMode = mode == null || "Tous".equals(mode)
                    || (c.getModeConsultation() != null && c.getModeConsultation().equals(mode));
                
                boolean match = matchRecherche && matchMode;
                
                if (!match) {
                    System.out.println("Consultation filtrée: " + c.getNom() + " " + c.getPrenom() + 
                                     " (recherche:" + matchRecherche + ", mode:" + matchMode + ")");
                }
                
                return match;
            })
            .collect(Collectors.toList());
        
        System.out.println("Résultats filtrés: " + resultats.size() + " / " + sourceConsultations.size());
        
        return resultats;
    }
    
    private void modifierConsultation(Consultation consultation) {
        try {
            System.out.println("🔧 Modification de la consultation : ID=" + consultation.getUtilisateurId());
            
            // Préparer les données pour le contrôleur d'édition
            AjouterConsultationController.setConsultationAEditer(consultation);
            
            // Charger la fenêtre de modification
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/views/AjouterConsultation.fxml")
            );
            
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 900, 760);
            scene.getStylesheets().add(
                java.util.Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm()
            );
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("✏ Modifier Consultation");
            stage.setScene(scene);
            
            System.out.println("✅ Ouverture de la fenêtre de modification...");
            stage.showAndWait();
            
            // Actualiser la liste après modification
            System.out.println("🔄 Actualisation de la liste...");
            actualiser();
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR lors de la modification :");
            e.printStackTrace();
            afficherMessageErreur("❌ Impossible d'ouvrir la modification : " + e.getMessage());
        }
    }
    
    private void supprimerConsultation(Consultation consultation) {
        try {
            consultationService.supprimerConsultation(consultation.getUtilisateurId());
            actualiser();
            afficherMessageSucces("✓ Consultation supprimée");
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors de la suppression");
        }
    }
    
    /**
     * 📄 TÉLÉCHARGE LE PDF DE LA CONSULTATION
     */
    private void telechargerPdf(Consultation consultation) {
        try {
            // Ouvre une boîte de dialogue pour choisir où sauvegarder le PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF de la consultation");
            
            // Nom de fichier par défaut : "Consultation_NOM_PRENOM_DATE.pdf"
            String nomFichier = String.format("Consultation_%s_%s_%s.pdf",
                    consultation.getNom(),
                    consultation.getPrenom(),
                    consultation.getDateConsultation().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            fileChooser.setInitialFileName(nomFichier);
            
            // Filtre pour n'afficher que les fichiers PDF
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            
            // Affiche la boîte de dialogue et récupère le fichier choisi
            File fichier = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());
            
            if (fichier != null) {
                // Génère le PDF
                pdfService.genererPdfConsultation(consultation, fichier.getAbsolutePath());
                afficherMessageSucces("✓ PDF généré avec succès : " + fichier.getName());
            }
        } catch (Exception e) {
            afficherMessageErreur("❌ Erreur lors de la génération du PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initialiserFiltres() {
        comboFiltreMode.setItems(FXCollections.observableArrayList("Tous", "A_DISTANCE", "PRESENTIEL", "TELECONSULTATION"));
        comboFiltreMode.getSelectionModel().select("Tous");
    }
    
    private void afficherMessageVide() {
        Label messageVide = new Label("📭 Aucune consultation à afficher");
        messageVide.setStyle("-fx-font-size: 18px; -fx-text-fill: #64748b; -fx-padding: 40px;");
        cardsContainer.getChildren().add(messageVide);
        cardsContainer.setAlignment(Pos.CENTER);
    }
    
    private void afficherMessage(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("status-message-success", "status-message-error");
        lblMessage.getStyleClass().add("status-message");
    }
    
    private void afficherMessageSucces(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("status-message", "status-message-error");
        lblMessage.getStyleClass().add("status-message-success");
    }
    
    private void afficherMessageErreur(String message) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("status-message", "status-message-success");
        lblMessage.getStyleClass().add("status-message-error");
    }
    
    private String formatMode(String mode) {
        switch (mode) {
            case "PRESENTIEL": return "🏥 Présentiel";
            case "A_DISTANCE": return "💻 À distance";
            case "TELECONSULTATION": return "📹 Téléconsultation";
            default: return mode;
        }
    }
    
    private String getClasseMode(String mode) {
        switch (mode) {
            case "PRESENTIEL": return "consultation-badge-presentiel";
            case "A_DISTANCE": return "consultation-badge-distance";
            case "TELECONSULTATION": return "consultation-badge-teleconsultation";
            default: return "";
        }
    }
}
