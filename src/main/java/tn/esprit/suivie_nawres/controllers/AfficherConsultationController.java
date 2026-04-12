package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.services.ConsultationService;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

import java.util.List;
import java.util.Objects;

public class AfficherConsultationController {
    @FXML private TableView<Consultation> tableConsultation;
    @FXML private TableColumn<Consultation, String> colNom;
    @FXML private TableColumn<Consultation, String> colPrenom;
    @FXML private TableColumn<Consultation, Object> colDate;
    @FXML private TableColumn<Consultation, Object> colHeure;
    @FXML private TableColumn<Consultation, String> colMode;
    @FXML private TableColumn<Consultation, String> colMaladie;
    @FXML private TableColumn<Consultation, String> colDiagnostic;
    @FXML private TableColumn<Consultation, String> colTraitement;
    @FXML private TableColumn<Consultation, String> colExamens;
    @FXML private TableColumn<Consultation, String> colNotes;
    @FXML private TableColumn<Consultation, Object> colCout;
    @FXML private TextField txtRechercheId;
    @FXML private ComboBox<String> comboFiltreMode;
    @FXML private Label lblMessage;
    @FXML private Button btnSupprimer;
    @FXML private Button btnModifier;

    private final ConsultationService consultationService = new ConsultationService();
    private final ObservableList<Consultation> sourceConsultations = FXCollections.observableArrayList();
    private FilteredList<Consultation> consultationsFiltrees;

    @FXML
    private void initialize() {
        tableConsultation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateConsultation"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureConsultation"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modeConsultation"));
        colMaladie.setCellValueFactory(new PropertyValueFactory<>("maladie"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        colTraitement.setCellValueFactory(new PropertyValueFactory<>("traitement"));
        colExamens.setCellValueFactory(new PropertyValueFactory<>("examensComplementaires"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notesConsultation"));
        colCout.setCellValueFactory(new PropertyValueFactory<>("coutConsultation"));
        initialiserFiltres();
        appliquerVisibiliteSelonRole();
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            List<Consultation> consultations = consultationService.afficherConsultations();
            sourceConsultations.setAll(consultations);
            appliquerFiltres();
            afficherMessage(sourceConsultations.isEmpty() ? "Aucune consultation trouvee." : "Liste des consultations chargee.");
        } catch (Exception exception) {
            afficherMessage("Erreur base de donnees. Verifie la structure de la table consultation.");
        }
    }

    @FXML
    private void rechercher() {
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        txtRechercheId.clear();
        if (comboFiltreMode != null) {
            comboFiltreMode.getSelectionModel().select("Tous");
        }
        appliquerFiltres();
    }

    @FXML
    private void supprimer() {
        try {
            Consultation selection = tableConsultation.getSelectionModel().getSelectedItem();
            if (selection == null) {
                afficherMessage("Sélectionne une consultation.");
                return;
            }
            consultationService.supprimerConsultation(selection.getUtilisateurId());
            actualiser();
            afficherMessage("Consultation supprimée.");
        } catch (Exception exception) {
            afficherMessage("Suppression impossible. Verifie les donnees.");
        }
    }

    @FXML
    private void modifier() {
        try {
            Consultation selection = tableConsultation.getSelectionModel().getSelectedItem();
            if (selection == null) {
                afficherMessage("Sélectionne une consultation.");
                return;
            }

            AjouterConsultationController.setConsultationAEditer(selection);
            AjouterConsultationController.setParentConsultationController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterConsultation.fxml"));
            Scene scene = new Scene(loader.load(), 900, 760);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Consultation");
            stage.setScene(scene);
            stage.showAndWait();
            actualiser();
        } catch (Exception exception) {
            afficherMessage("Impossible d'ouvrir la modification.");
        }
    }

    public void rafraichirListe() {
        actualiser();
    }

    private void appliquerVisibiliteSelonRole() {
        boolean medecin = RoleContext.getCurrentRole() == UserRole.MEDECIN;
        if (btnModifier != null) {
            btnModifier.setVisible(medecin);
            btnModifier.setManaged(medecin);
        }
        btnSupprimer.setVisible(medecin);
        btnSupprimer.setManaged(medecin);
    }

    private void afficherMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    private void initialiserFiltres() {
        if (comboFiltreMode != null) {
            comboFiltreMode.setItems(FXCollections.observableArrayList("Tous", "A_DISTANCE", "PRESENTIEL", "TELECONSULTATION"));
            comboFiltreMode.getSelectionModel().select("Tous");
            comboFiltreMode.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        }
        if (txtRechercheId != null) {
            txtRechercheId.textProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        }

        consultationsFiltrees = new FilteredList<>(sourceConsultations, item -> true);
        SortedList<Consultation> consultationsTriees = new SortedList<>(consultationsFiltrees);
        consultationsTriees.comparatorProperty().bind(tableConsultation.comparatorProperty());
        tableConsultation.setItems(consultationsTriees);
    }

    private void appliquerFiltres() {
        if (consultationsFiltrees == null) {
            return;
        }
        String recherche = txtRechercheId == null || txtRechercheId.getText() == null
                ? ""
                : txtRechercheId.getText().trim().toLowerCase();
        String mode = comboFiltreMode == null ? "Tous" : comboFiltreMode.getValue();

        consultationsFiltrees.setPredicate(consultation -> {
            if (consultation == null) {
                return false;
            }
            boolean matchRecherche = recherche.isEmpty()
                    || String.valueOf(consultation.getUtilisateurId()).contains(recherche)
                    || contient(consultation.getNom(), recherche)
                    || contient(consultation.getPrenom(), recherche)
                    || contient(consultation.getMaladie(), recherche)
                    || contient(consultation.getDiagnostic(), recherche)
                    || contient(consultation.getTraitement(), recherche)
                    || contient(consultation.getNotesConsultation(), recherche);

            boolean matchMode = mode == null || "Tous".equals(mode)
                    || contientExact(consultation.getModeConsultation(), mode);

            return matchRecherche && matchMode;
        });

        afficherMessage(tableConsultation.getItems().isEmpty()
                ? "Aucune consultation ne correspond au filtre."
                : tableConsultation.getItems().size() + " consultations affichees.");
    }

    private boolean contient(String valeur, String recherche) {
        return valeur != null && valeur.toLowerCase().contains(recherche);
    }

    private boolean contientExact(String valeur, String attendu) {
        return valeur != null && valeur.trim().equalsIgnoreCase(attendu);
    }
}

