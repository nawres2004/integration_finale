package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;
import tn.esprit.suivie_nawres.services.VonageSmsService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TraiterDemandesRendezVousController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox cardsContainer;
    @FXML private Label lblMessage;

    private final RendezVousService rendezVousService = new RendezVousService();

    @FXML
    private void initialize() {
        actualiser();
    }

    @FXML
    private void actualiser() {
        try {
            List<RendezVous> rendezVous = rendezVousService.afficherRendezVous()
                    .stream()
                    .filter(rdv -> rdv.getStatutRendezVous() == StatutRendezVous.EN_ATTENTE)
                    .collect(Collectors.toList());
            renderCards(rendezVous);
            afficherMessage(rendezVous.isEmpty() ? "Aucune demande a traiter." : rendezVous.size() + " demande(s) en attente.");
        } catch (Exception exception) {
            afficherMessage("Erreur base de donnees.");
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
        Label patient = new Label(safe(rendezVous.getNom()) + "\n" + safe(rendezVous.getPrenom()));
        patient.getStyleClass().add("rdv-row-id");
        Label priorite = new Label("Priorite: " + safe(rendezVous.getPriorite()));
        priorite.getStyleClass().add("rdv-row-priority");
        left.getChildren().addAll(patient, priorite);

        VBox middle = new VBox(8);
        middle.getStyleClass().add("rdv-row-middle");
        middle.setPadding(new Insets(14));
        Label date = new Label("Date: " + (rendezVous.getDateRendezVous() == null ? "-" : rendezVous.getDateRendezVous().format(DATE_FORMAT)));
        Label heure = new Label("Heure: " + (rendezVous.getHeureRendezVous() == null ? "-" : rendezVous.getHeureRendezVous().format(TIME_FORMAT)));
        Label tel = new Label("Tel: " + safe(rendezVous.getTelephone()));
        Label mode = new Label("Mode: " + safe(rendezVous.getModeConsultation()));
        date.getStyleClass().add("rdv-row-line");
        heure.getStyleClass().add("rdv-row-line");
        tel.getStyleClass().add("rdv-row-line");
        mode.getStyleClass().add("rdv-row-line");
        middle.getChildren().addAll(date, heure, tel, mode);
        HBox.setHgrow(middle, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.getStyleClass().add("rdv-row-actions");
        right.setPadding(new Insets(14));
        Button btnVoir = new Button("Voir");
        btnVoir.getStyleClass().addAll("crud-icon-button", "crud-view");
        btnVoir.setOnAction(event -> ouvrirDetailsModale(rendezVous));
        Button btnAccepter = new Button("Accepter");
        btnAccepter.getStyleClass().addAll("crud-icon-button", "crud-edit");
        btnAccepter.setStyle("-fx-text-fill: #2e7d32;");
        btnAccepter.setOnAction(event -> accepterDemande(rendezVous));
        Button btnRefuser = new Button("Refuser");
        btnRefuser.getStyleClass().addAll("crud-icon-button", "crud-delete");
        btnRefuser.setStyle("-fx-text-fill: #d32f2f;");
        btnRefuser.setOnAction(event -> refuserDemande(rendezVous));
        right.getChildren().addAll(btnVoir, btnAccepter, btnRefuser);

        row.getChildren().addAll(left, middle, right);
        return row;
    }

    private void ouvrirDetailsModale(RendezVous rendezVous) {
        try {
            DetailRendezVousMedecinController.setSelectedRendezVous(rendezVous);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DetailRendezVousMedecin.fxml"));
            Scene scene = new Scene(loader.load(), 920, 680);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Details Demande");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception exception) {
            afficherMessage("Impossible d'ouvrir les details.");
        }
    }

    private void accepterDemande(RendezVous rendezVous) {
        try {
            System.out.println("=== 🔍 DÉBUT ACCEPTATION RDV ===");
            System.out.println("ID : " + rendezVous.getUtilisateurId());
            System.out.println("Patient : " + rendezVous.getNom() + " " + rendezVous.getPrenom());
            System.out.println("Téléphone : " + rendezVous.getTelephone());
            System.out.println("Date : " + rendezVous.getDateRendezVous());
            System.out.println("Heure : " + rendezVous.getHeureRendezVous());
            
            // Change le statut à ACCEPTE
            rendezVousService.changerStatutRendezVous(rendezVous.getUtilisateurId(), StatutRendezVous.ACCEPTE);
            System.out.println("✅ Statut changé à ACCEPTE dans la base de données");
            
            // Met à jour le statut dans l'objet pour l'envoi du SMS
            rendezVous.setStatutRendezVous(StatutRendezVous.ACCEPTE);
            
            // ========================================
            // 📱 ENVOI DE SMS D'ACCEPTATION AU PATIENT
            // ========================================
            System.out.println("📱 Tentative d'envoi de SMS...");
            boolean smsEnvoye = VonageSmsService.envoyerSmsAcceptationRendezVous(rendezVous);
            System.out.println("Résultat envoi SMS : " + (smsEnvoye ? "✅ SUCCÈS" : "❌ ÉCHEC"));
            
            if (smsEnvoye) {
                afficherMessage("✅ Demande acceptee. SMS envoye au patient.");
            } else {
                afficherMessage("⚠️ Demande acceptee. (SMS non envoye - verifiez la configuration)");
            }
            
            System.out.println("=== 🔍 FIN ACCEPTATION RDV ===\n");
            actualiser();
        } catch (Exception exception) {
            System.err.println("❌ ERREUR lors de l'acceptation : " + exception.getMessage());
            exception.printStackTrace();
            afficherMessage("Erreur lors de l'acceptation.");
        }
    }

    private void refuserDemande(RendezVous rendezVous) {
        try {
            rendezVousService.changerStatutRendezVous(rendezVous.getUtilisateurId(), StatutRendezVous.REFUSE);
            afficherMessage("Demande refusee.");
            actualiser();
        } catch (Exception exception) {
            afficherMessage("Erreur lors du refus.");
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void afficherMessage(String message) {
        lblMessage.setText(message);
    }
}

