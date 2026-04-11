package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AfficherRendezVousMedecinController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox cardsContainer;
    @FXML private TextField txtRechercheId;
    @FXML private Label lblMessage;

    private final RendezVousService rendezVousService = new RendezVousService();

    @FXML
    private void initialize() {
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            List<RendezVous> rendezVous = rendezVousService.afficherRendezVous();
            renderCards(rendezVous);
            afficherMessage(rendezVous.isEmpty() ? "Aucun rendez-vous trouve." : "Liste chargee.");
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
            rendezVousService.chercherParId(utilisateurId)
                    .ifPresentOrElse(
                            rdv -> {
                                renderCards(List.of(rdv));
                                afficherMessage("1 rendez-vous trouve.");
                            },
                            () -> {
                                renderCards(List.of());
                                afficherMessage("Aucun rendez-vous pour cet id.");
                            }
                    );
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    public void rafraichirListe() {
        actualiser();
    }

    private void renderCards(List<RendezVous> rendezVousList) {
        cardsContainer.getChildren().clear();
        for (RendezVous rendezVous : rendezVousList) {
            cardsContainer.getChildren().add(createCardRow(rendezVous));
        }
    }

    private HBox createCardRow(RendezVous rendezVous) {
        HBox row = new HBox(14);
        row.getStyleClass().add("rdv-card-row");

        VBox left = new VBox(10);
        left.getStyleClass().add("rdv-row-left");
        left.setPadding(new Insets(14));
        Label id = new Label("#" + rendezVous.getUtilisateurId());
        id.getStyleClass().add("rdv-row-id");
        Label priorite = new Label(safe(rendezVous.getPriorite()));
        priorite.getStyleClass().add("rdv-row-priority");
        left.getChildren().addAll(id, priorite);

        VBox middle = new VBox(8);
        middle.getStyleClass().add("rdv-row-middle");
        middle.setPadding(new Insets(14));
        Label patient = new Label("Patient #" + rendezVous.getUtilisateurId());
        patient.getStyleClass().add("rdv-row-patient");
        Label date = new Label("Date: " + (rendezVous.getDateRendezVous() == null ? "-" : rendezVous.getDateRendezVous().format(DATE_FORMAT)));
        Label heure = new Label("Heure: " + (rendezVous.getHeureRendezVous() == null ? "-" : rendezVous.getHeureRendezVous().format(TIME_FORMAT)));
        Label tel = new Label("Telephone: " + safe(rendezVous.getTelephone()));
        Label mode = new Label("Mode: " + safe(rendezVous.getModeConsultation()));
        Label statut = new Label("Statut: " + (rendezVous.getStatutRendezVous() == null ? "EN_ATTENTE" : rendezVous.getStatutRendezVous().name()));
        date.getStyleClass().add("rdv-row-line");
        heure.getStyleClass().add("rdv-row-line");
        tel.getStyleClass().add("rdv-row-line");
        mode.getStyleClass().add("rdv-row-line");
        statut.getStyleClass().add("rdv-row-line");
        middle.getChildren().addAll(patient, date, heure, tel, mode, statut);
        HBox.setHgrow(middle, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.getStyleClass().add("rdv-row-actions");
        right.setPadding(new Insets(14));
        Button btnVoir = new Button("Voir");
        btnVoir.getStyleClass().addAll("crud-icon-button", "crud-view");
        btnVoir.setOnAction(event -> ouvrirDetailsModale(rendezVous));
        Button btnModifier = new Button("Modifier");
        btnModifier.getStyleClass().addAll("crud-icon-button", "crud-edit");
        btnModifier.setOnAction(event -> ouvrirEditionModale(rendezVous));
        right.getChildren().addAll(btnVoir, btnModifier);

        row.getChildren().addAll(left, middle, right);
        return row;
    }

    private void ouvrirDetailsModale(RendezVous rendezVous) {
        try {
            DetailRendezVousMedecinController.setSelectedRendezVous(rendezVous);
            DetailRendezVousMedecinController.setParentController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DetailRendezVousMedecin.fxml"));
            Scene scene = new Scene(loader.load(), 920, 680);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Details Rendez-vous");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception exception) {
            afficherMessage("Impossible d'ouvrir les details: " + exception.getMessage());
        }
    }

    private void ouvrirEditionModale(RendezVous rendezVous) {
        try {
            AjouterRendezVousController.setRendezVousAEditer(rendezVous);
            AjouterRendezVousController.setParentMedecinController(this);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterRendezVous.fxml"));
            Scene scene = new Scene(loader.load(), 860, 760);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Rendez-vous");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception exception) {
            afficherMessage("Impossible d'ouvrir la modification: " + exception.getMessage());
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void afficherMessage(String message) {
        lblMessage.setText(message);
    }
}

