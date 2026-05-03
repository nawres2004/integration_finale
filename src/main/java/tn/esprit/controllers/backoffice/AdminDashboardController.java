package tn.esprit.controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.services.DonService;
import tn.esprit.services.ProjetService;

import java.io.IOException;
import java.sql.SQLException;

public class AdminDashboardController {

    @FXML private Label lblNbProjets;
    @FXML private Label lblNbDons;
    @FXML private Label lblTotal;

    ProjetService ps = new ProjetService();
    DonService    ds = new DonService();

    public void initialize() {
        try {
            lblNbProjets.setText(String.valueOf(ps.compterProjets()));
            lblNbDons.setText(String.valueOf(ds.compterDons()));
            lblTotal.setText(ds.getTotalDons() + " DT");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", e.getMessage());
        }
    }

    @FXML public void goToProjets() { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherProjets.fxml"); }
    @FXML public void goToDons()    { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherDons.fxml"); }
    @FXML public void goToFront()   { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AdminDashboard.fxml"); }
    @FXML public void goToStats()   { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/Statistiques.fxml"); }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            lblNbProjets.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type,
                           String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}