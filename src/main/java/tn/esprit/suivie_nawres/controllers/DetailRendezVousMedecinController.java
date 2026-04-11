package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;

import java.time.format.DateTimeFormatter;

public class DetailRendezVousMedecinController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static RendezVous selectedRendezVous;
    private static AfficherRendezVousMedecinController parentController;

    @FXML
    private Pane rootPane;
    @FXML
    private Label lblTitre;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblHeure;
    @FXML
    private Label lblPriorite;
    @FXML
    private Label lblMode;
    @FXML
    private Label lblStatut;
    @FXML
    private Label lblPays;
    @FXML
    private Label lblTelephone;
    @FXML
    private Label lblNotes;
    @FXML
    private Label lblMessage;

    private final RendezVousService rendezVousService = new RendezVousService();

    public static void setSelectedRendezVous(RendezVous rendezVous) {
        selectedRendezVous = rendezVous;
    }

    public static void setParentController(AfficherRendezVousMedecinController controller) {
        parentController = controller;
    }

    @FXML
    private void initialize() {
        if (selectedRendezVous == null) {
            retour();
            return;
        }
        remplirDetail(selectedRendezVous);
    }

    @FXML
    private void retour() {
        fermerModale();
    }

    @FXML
    private void supprimer() {
        if (selectedRendezVous == null) {
            retour();
            return;
        }
        try {
            rendezVousService.supprimerRendezVous(selectedRendezVous.getUtilisateurId());
            lblMessage.setText("Rendez-vous supprime.");
            selectedRendezVous = null;
            if (parentController != null) {
                parentController.rafraichirListe();
            }
            fermerModale();
        } catch (Exception exception) {
            lblMessage.setText("Erreur : " + exception.getMessage());
        }
    }

    private void fermerModale() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    private void remplirDetail(RendezVous rendezVous) {
        lblTitre.setText("Details rendez-vous #" + rendezVous.getUtilisateurId());
        lblDate.setText(rendezVous.getDateRendezVous() == null ? "-" : rendezVous.getDateRendezVous().format(DATE_FORMAT));
        lblHeure.setText(rendezVous.getHeureRendezVous() == null ? "-" : rendezVous.getHeureRendezVous().format(TIME_FORMAT));
        lblPriorite.setText(safe(rendezVous.getPriorite()));
        lblMode.setText(safe(rendezVous.getModeConsultation()));
        lblStatut.setText(rendezVous.getStatutRendezVous() == null ? "EN_ATTENTE" : rendezVous.getStatutRendezVous().name());
        lblPays.setText(safe(rendezVous.getPays()));
        lblTelephone.setText(safe(rendezVous.getTelephone()));
        lblNotes.setText(safe(rendezVous.getNotesRendezVous()));
        lblMessage.setText("Pret.");
    }


    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}

