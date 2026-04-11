package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
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
    @FXML private TableColumn<Consultation, Integer> colUtilisateurId;
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
    @FXML private Label lblMessage;
    @FXML private Button btnSupprimer;
    @FXML private Button btnModifier;

    private final ConsultationService consultationService = new ConsultationService();

    @FXML
    private void initialize() {
        tableConsultation.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colUtilisateurId.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateConsultation"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureConsultation"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modeConsultation"));
        colMaladie.setCellValueFactory(new PropertyValueFactory<>("maladie"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        colTraitement.setCellValueFactory(new PropertyValueFactory<>("traitement"));
        colExamens.setCellValueFactory(new PropertyValueFactory<>("examensComplementaires"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notesConsultation"));
        colCout.setCellValueFactory(new PropertyValueFactory<>("coutConsultation"));
        appliquerVisibiliteSelonRole();
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            List<Consultation> consultations = consultationService.afficherConsultations();
            tableConsultation.setItems(FXCollections.observableArrayList(consultations));
            afficherMessage(consultations.isEmpty() ? "Aucune consultation trouvée." : "Liste des consultations chargée.");
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    @FXML
    private void rechercher() {
        try {
            String texte = txtRechercheId.getText();
            if (texte == null || texte.isBlank()) {
                actualiser();
                return;
            }
            int utilisateurId = Integer.parseInt(texte.trim());
            consultationService.chercherParId(utilisateurId)
                    .ifPresentOrElse(consultation -> tableConsultation.setItems(FXCollections.observableArrayList(consultation)),
                            () -> tableConsultation.setItems(FXCollections.observableArrayList()));
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
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
            afficherMessage("Erreur : " + exception.getMessage());
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
            afficherMessage("Impossible d'ouvrir la modification: " + exception.getMessage());
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
}

