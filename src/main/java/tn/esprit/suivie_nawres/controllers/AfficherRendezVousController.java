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
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

import java.util.List;
import java.util.Objects;

public class AfficherRendezVousController {
    @FXML private TableView<RendezVous> tableRendezVous;
    @FXML private TableColumn<RendezVous, Integer> colUtilisateurId;
    @FXML private TableColumn<RendezVous, Object> colDate;
    @FXML private TableColumn<RendezVous, Object> colHeure;
    @FXML private TableColumn<RendezVous, String> colPriorite;
    @FXML private TableColumn<RendezVous, String> colMode;
    @FXML private TableColumn<RendezVous, String> colStatut;
    @FXML private TableColumn<RendezVous, String> colNotes;
    @FXML private TableColumn<RendezVous, String> colPays;
    @FXML private TableColumn<RendezVous, String> colTelephone;
    @FXML private TextField txtRechercheId;
    @FXML private Label lblMessage;
    @FXML private Button btnSupprimer;
    @FXML private Button btnModifier;
    @FXML private Button btnAccepter;
    @FXML private Button btnRefuser;

    private final RendezVousService rendezVousService = new RendezVousService();

    @FXML
    private void initialize() {
        tableRendezVous.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colUtilisateurId.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRendezVous"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureRendezVous"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modeConsultation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutRendezVous"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notesRendezVous"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        appliquerVisibiliteSelonRole();
        tableRendezVous.getSelectionModel().selectedItemProperty().addListener((observable, ancien, selection) -> actualiserActions(selection));
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            if (RoleContext.getCurrentRole() == UserRole.PATIENT) {
                chargerRendezVousPatient();
                return;
            }
            List<RendezVous> rendezVous = rendezVousService.afficherRendezVous();
            tableRendezVous.setItems(FXCollections.observableArrayList(rendezVous));
            afficherMessage(rendezVous.isEmpty() ? "Aucun rendez-vous trouvé." : "Liste des rendez-vous chargée.");
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
            if (RoleContext.getCurrentRole() == UserRole.PATIENT) {
                Integer utilisateurCourant = RoleContext.getCurrentUtilisateurId();
                if (utilisateurCourant != null && utilisateurCourant != utilisateurId) {
                    afficherMessage("Tu peux rechercher uniquement ton identifiant patient.");
                    return;
                }
                RoleContext.setCurrentUtilisateurId(utilisateurId);
            }
            rendezVousService.chercherParId(utilisateurId)
                    .ifPresentOrElse(rendezVous -> tableRendezVous.setItems(FXCollections.observableArrayList(rendezVous)),
                            () -> tableRendezVous.setItems(FXCollections.observableArrayList()));
            afficherMessage(tableRendezVous.getItems().isEmpty() ? "Aucun rendez-vous trouvé." : "Rendez-vous chargé.");
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    @FXML
    private void supprimer() {
        try {
            RendezVous selection = tableRendezVous.getSelectionModel().getSelectedItem();
            if (selection == null) {
                afficherMessage("Sélectionne un rendez-vous.");
                return;
            }
            if (RoleContext.getCurrentRole() == UserRole.PATIENT && !selection.getUtilisateurId().equals(RoleContext.getCurrentUtilisateurId())) {
                afficherMessage("Tu peux supprimer uniquement ton propre rendez-vous.");
                return;
            }
            if (RoleContext.getCurrentRole() == UserRole.PATIENT && selection.getStatutRendezVous() != StatutRendezVous.EN_ATTENTE) {
                afficherMessage("Un rendez-vous accepté ou refusé ne peut plus être supprimé.");
                return;
            }
            rendezVousService.supprimerRendezVous(selection.getUtilisateurId());
            actualiser();
            afficherMessage("Rendez-vous supprimé.");
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    @FXML
    private void modifier() {
        try {
            RendezVous selection = tableRendezVous.getSelectionModel().getSelectedItem();
            if (selection == null) {
                afficherMessage("Sélectionne un rendez-vous.");
                return;
            }
            if (RoleContext.getCurrentRole() == UserRole.PATIENT && !selection.getUtilisateurId().equals(RoleContext.getCurrentUtilisateurId())) {
                afficherMessage("Tu peux modifier uniquement ton propre rendez-vous.");
                return;
            }
            if (RoleContext.getCurrentRole() == UserRole.PATIENT && selection.getStatutRendezVous() != StatutRendezVous.EN_ATTENTE) {
                afficherMessage("Seuls les rendez-vous EN_ATTENTE peuvent être modifiés.");
                return;
            }

            AjouterRendezVousController.setRendezVousAEditer(selection);
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
        } catch (Exception exception) {
            afficherMessage("Impossible d'ouvrir la modification: " + exception.getMessage());
        }
    }

    @FXML
    private void accepter() {
        changerStatut(StatutRendezVous.ACCEPTE, "Rendez-vous accepté.");
    }

    @FXML
    private void refuser() {
        changerStatut(StatutRendezVous.REFUSE, "Rendez-vous refusé.");
    }

    private void changerStatut(StatutRendezVous statut, String message) {
        try {
            RendezVous selection = tableRendezVous.getSelectionModel().getSelectedItem();
            if (selection == null) {
                afficherMessage("Sélectionne un rendez-vous.");
                return;
            }
            rendezVousService.changerStatutRendezVous(selection.getUtilisateurId(), statut);
            actualiser();
            afficherMessage(message);
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    private void appliquerVisibiliteSelonRole() {
        UserRole role = RoleContext.getCurrentRole();
        boolean medecin = role == UserRole.MEDECIN;
        boolean patient = role == UserRole.PATIENT;
        txtRechercheId.setDisable(false);
        if (btnModifier != null) {
            boolean modifierVisible = medecin || patient;
            btnModifier.setVisible(modifierVisible);
            btnModifier.setManaged(modifierVisible);
        }
        boolean supprimerVisible = medecin || patient;
        btnSupprimer.setVisible(supprimerVisible);
        btnSupprimer.setManaged(supprimerVisible);
        btnAccepter.setVisible(medecin);
        btnAccepter.setManaged(medecin);
        btnRefuser.setVisible(medecin);
        btnRefuser.setManaged(medecin);
        if (patient && RoleContext.getCurrentUtilisateurId() != null) {
            txtRechercheId.setText(String.valueOf(RoleContext.getCurrentUtilisateurId()));
        }
    }

    private void actualiserActions(RendezVous selection) {
        UserRole role = RoleContext.getCurrentRole();
        if (selection == null) {
            if (btnModifier != null) btnModifier.setDisable(true);
            btnSupprimer.setDisable(true);
            btnAccepter.setDisable(true);
            btnRefuser.setDisable(true);
            return;
        }
        boolean enAttente = selection.getStatutRendezVous() == StatutRendezVous.EN_ATTENTE;
        if (btnModifier != null) {
            btnModifier.setDisable(role == UserRole.PATIENT && !enAttente);
        }
        btnSupprimer.setDisable(role == UserRole.PATIENT && !enAttente);
        btnAccepter.setDisable(role != UserRole.MEDECIN);
        btnRefuser.setDisable(role != UserRole.MEDECIN);
    }

    private void afficherMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    private void chargerRendezVousPatient() throws Exception {
        Integer utilisateurCourant = RoleContext.getCurrentUtilisateurId();
        if (utilisateurCourant == null) {
            tableRendezVous.setItems(FXCollections.observableArrayList());
            afficherMessage("Reserve d'abord un rendez-vous, puis utilise ton utilisateur_id pour rechercher.");
            return;
        }
        rendezVousService.chercherParId(utilisateurCourant)
                .ifPresentOrElse(
                        rendezVous -> {
                            tableRendezVous.setItems(FXCollections.observableArrayList(rendezVous));
                            afficherMessage("Ton rendez-vous et son statut sont affichés.");
                        },
                        () -> {
                            tableRendezVous.setItems(FXCollections.observableArrayList());
                            afficherMessage("Aucun rendez-vous trouvé pour ton identifiant.");
                        }
                );
    }
}

