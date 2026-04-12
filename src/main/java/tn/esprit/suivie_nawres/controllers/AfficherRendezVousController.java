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
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

import java.util.List;
import java.util.Objects;

public class AfficherRendezVousController {
    @FXML private TableView<RendezVous> tableRendezVous;
    @FXML private TableColumn<RendezVous, String> colNom;
    @FXML private TableColumn<RendezVous, String> colPrenom;
    @FXML private TableColumn<RendezVous, Object> colDate;
    @FXML private TableColumn<RendezVous, Object> colHeure;
    @FXML private TableColumn<RendezVous, String> colPriorite;
    @FXML private TableColumn<RendezVous, String> colMode;
    @FXML private TableColumn<RendezVous, String> colStatut;
    @FXML private TableColumn<RendezVous, String> colNotes;
    @FXML private TableColumn<RendezVous, String> colPays;
    @FXML private TableColumn<RendezVous, String> colTelephone;
    @FXML private TextField txtRechercheId;
    @FXML private ComboBox<String> comboFiltreStatut;
    @FXML private ComboBox<String> comboFiltreMode;
    @FXML private Label lblMessage;
    @FXML private Button btnSupprimer;
    @FXML private Button btnModifier;
    @FXML private Button btnAccepter;
    @FXML private Button btnRefuser;

    private final RendezVousService rendezVousService = new RendezVousService();
    private final ObservableList<RendezVous> sourceRendezVous = FXCollections.observableArrayList();
    private FilteredList<RendezVous> rendezVousFiltres;

    @FXML
    private void initialize() {
        tableRendezVous.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRendezVous"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureRendezVous"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modeConsultation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutRendezVous"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notesRendezVous"));
        colPays.setCellValueFactory(new PropertyValueFactory<>("pays"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        initialiserFiltres();
        appliquerVisibiliteSelonRole();
        tableRendezVous.getSelectionModel().selectedItemProperty().addListener((observable, ancien, selection) -> actualiserActions(selection));
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            if (RoleContext.getCurrentRole() == UserRole.PATIENT) {
                chargerRendezVousPatient();
                appliquerFiltres();
                return;
            }
            List<RendezVous> rendezVous = rendezVousService.afficherRendezVous();
            sourceRendezVous.setAll(rendezVous);
            appliquerFiltres();
            afficherMessage(sourceRendezVous.isEmpty() ? "Aucun rendez-vous trouve." : "Liste des rendez-vous chargee.");
        } catch (Exception exception) {
            afficherMessage("Erreur base de donnees. Verifie la structure de la table rendez_vous.");
        }
    }

    @FXML
    private void rechercher() {
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        txtRechercheId.clear();
        if (comboFiltreStatut != null) {
            comboFiltreStatut.getSelectionModel().select("Tous");
        }
        if (comboFiltreMode != null) {
            comboFiltreMode.getSelectionModel().select("Tous");
        }
        appliquerFiltres();
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
            afficherMessage("Suppression impossible. Verifie les donnees.");
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
            afficherMessage("Impossible d'ouvrir la modification.");
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
            afficherMessage("Mise a jour du statut impossible.");
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

    private void initialiserFiltres() {
        if (comboFiltreStatut != null) {
            comboFiltreStatut.setItems(FXCollections.observableArrayList("Tous", "EN_ATTENTE", "ACCEPTE", "REFUSE"));
            comboFiltreStatut.getSelectionModel().select("Tous");
            comboFiltreStatut.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        }
        if (comboFiltreMode != null) {
            comboFiltreMode.setItems(FXCollections.observableArrayList("Tous", "A_DISTANCE", "PRESENTIEL", "TELECONSULTATION"));
            comboFiltreMode.getSelectionModel().select("Tous");
            comboFiltreMode.valueProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        }
        if (txtRechercheId != null) {
            txtRechercheId.textProperty().addListener((obs, oldValue, newValue) -> appliquerFiltres());
        }

        rendezVousFiltres = new FilteredList<>(sourceRendezVous, item -> true);
        SortedList<RendezVous> rendezVousTries = new SortedList<>(rendezVousFiltres);
        rendezVousTries.comparatorProperty().bind(tableRendezVous.comparatorProperty());
        tableRendezVous.setItems(rendezVousTries);
    }

    private void appliquerFiltres() {
        if (rendezVousFiltres == null) {
            return;
        }
        String recherche = txtRechercheId == null || txtRechercheId.getText() == null
                ? ""
                : txtRechercheId.getText().trim().toLowerCase();
        String statut = comboFiltreStatut == null ? "Tous" : comboFiltreStatut.getValue();
        String mode = comboFiltreMode == null ? "Tous" : comboFiltreMode.getValue();

        rendezVousFiltres.setPredicate(rendezVous -> {
            if (rendezVous == null) {
                return false;
            }
            boolean matchRecherche = recherche.isEmpty()
                    || String.valueOf(rendezVous.getUtilisateurId()).contains(recherche)
                    || contient(rendezVous.getNom(), recherche)
                    || contient(rendezVous.getPrenom(), recherche)
                    || contient(rendezVous.getNotesRendezVous(), recherche)
                    || contient(rendezVous.getPays(), recherche)
                    || contient(rendezVous.getTelephone(), recherche);

            boolean matchStatut = statut == null || "Tous".equals(statut)
                    || (rendezVous.getStatutRendezVous() != null && statut.equals(rendezVous.getStatutRendezVous().name()));

            boolean matchMode = mode == null || "Tous".equals(mode)
                    || contientExact(rendezVous.getModeConsultation(), mode);

            return matchRecherche && matchStatut && matchMode;
        });

        afficherMessage(tableRendezVous.getItems().isEmpty()
                ? "Aucun rendez-vous ne correspond au filtre."
                : tableRendezVous.getItems().size() + " rendez-vous affiches.");
    }

    private boolean contient(String valeur, String recherche) {
        return valeur != null && valeur.toLowerCase().contains(recherche);
    }

    private boolean contientExact(String valeur, String attendu) {
        return valeur != null && valeur.trim().equalsIgnoreCase(attendu);
    }

    private void chargerRendezVousPatient() throws Exception {
        Integer utilisateurCourant = RoleContext.getCurrentUtilisateurId();
        if (utilisateurCourant == null) {
            sourceRendezVous.clear();
            afficherMessage("Reserve d'abord un rendez-vous, puis utilise ton id pour rechercher.");
            return;
        }
        rendezVousService.chercherParId(utilisateurCourant)
                .ifPresentOrElse(
                        rendezVous -> {
                            sourceRendezVous.setAll(rendezVous);
                            afficherMessage("Ton rendez-vous et son statut sont affichés.");
                        },
                        () -> {
                            sourceRendezVous.clear();
                            afficherMessage("Aucun rendez-vous trouvé pour ton identifiant.");
                        }
                );
    }
}

