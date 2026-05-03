package tn.esprit.controllers.backoffice;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.models.Don;
import tn.esprit.services.DonService;
import tn.esprit.services.DonPdfService;
import tn.esprit.services.ProjetService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class AfficherDonsController {

    @FXML private TableView<Don>             tableDons;
    @FXML private TableColumn<Don,String>    colNom;
    @FXML private TableColumn<Don,String>    colPrenom;
    @FXML private TableColumn<Don,Double>    colMontant;
    @FXML private TableColumn<Don,String>    colMode;
    @FXML private TableColumn<Don,String>    colStatut;
    @FXML private TableColumn<Don,LocalDate> colDate;
    @FXML private TableColumn<Don,Void>      colActions;

    private final DonService    ds  = new DonService();
    private final DonPdfService pdf = new DonPdfService();
    private final ProjetService ps  = new ProjetService();

    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomDonateur"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenomDonateur"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDon"));

        tableDons.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configurerColonneActions();
        chargerDonnees();
    }

    private void configurerColonneActions() {
        // Badges colorés pour la colonne Statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                badge.setText(item);
                if ("paid".equalsIgnoreCase(item)) {
                    badge.setText("Payé");
                    badge.setStyle("-fx-background-color:#d1fae5; -fx-text-fill:#065f46;" +
                            "-fx-background-radius:20; -fx-padding:3 10 3 10; -fx-font-weight:bold; -fx-font-size:11px;");
                } else if ("pending".equalsIgnoreCase(item)) {
                    badge.setText("En attente");
                    badge.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e;" +
                            "-fx-background-radius:20; -fx-padding:3 10 3 10; -fx-font-weight:bold; -fx-font-size:11px;");
                } else {
                    badge.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;" +
                            "-fx-background-radius:20; -fx-padding:3 10 3 10; -fx-font-weight:bold; -fx-font-size:11px;");
                }
                setGraphic(badge);
            }
        });

        // Boutons Actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnDelete = new Button("Supprimer");
            private final Button btnPdf    = new Button("📄 Reçu");
            private final HBox   box       = new HBox(6, btnPdf, btnDelete);
            {
                btnDelete.setStyle(
                    "-fx-background-color:#e53935; -fx-text-fill:white; -fx-cursor:hand;" +
                    "-fx-background-radius:8; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-font-weight:bold;");
                btnPdf.setStyle(
                    "-fx-background-color:#1a73e8; -fx-text-fill:white; -fx-cursor:hand;" +
                    "-fx-background-radius:8; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-font-weight:bold;");
                box.setStyle("-fx-alignment:center;");
                btnDelete.setOnAction(e -> supprimerDon(getTableView().getItems().get(getIndex())));
                btnPdf.setOnAction(e -> genererPdf(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Don don = getTableView().getItems().get(getIndex());
                boolean isPaid = "paid".equalsIgnoreCase(don.getPaymentStatus());
                if (isPaid) {
                    btnPdf.setStyle(
                        "-fx-background-color:#1a73e8; -fx-text-fill:white; -fx-cursor:hand;" +
                        "-fx-background-radius:8; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-font-weight:bold;");
                    btnPdf.setDisable(false);
                } else {
                    btnPdf.setStyle(
                        "-fx-background-color:#b0bec5; -fx-text-fill:white; -fx-cursor:default;" +
                        "-fx-background-radius:8; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-font-weight:bold;");
                    btnPdf.setDisable(true);
                }
                btnPdf.setVisible(true);
                btnPdf.setManaged(true);
                setGraphic(box);
            }
        });
    }

    private void chargerDonnees() {
        try {
            ArrayList<Don> list = ds.afficherAll();
            tableDons.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void genererPdf(Don don) {
        try {
            // Récupérer le titre du projet
            String titreProjet = ps.afficherAll().stream()
                    .filter(p -> p.getId() == don.getProjetId())
                    .map(p -> p.getTitreProjet())
                    .findFirst()
                    .orElse("Projet #" + don.getProjetId());

            String chemin = pdf.genererRecuDon(don, titreProjet);

            // Ouvrir le PDF automatiquement
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(chemin));
            }

            showAlert(Alert.AlertType.INFORMATION, "PDF généré",
                    "✅ Reçu enregistré dans :\n" + chemin);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur PDF", e.getMessage());
        }
    }

    private void supprimerDon(Don sel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le don de " + sel.getMontant() + " DT de "
                + sel.getPrenomDonateur() + " " + sel.getNomDonateur() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    ds.supprimer(sel.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Don supprimé !");
                    chargerDonnees();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
                }
            }
        });
    }

    @FXML public void goToProjets()   { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherProjets.fxml"); }
    @FXML public void goToDashboard() { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AdminDashboard.fxml"); }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            tableDons.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
