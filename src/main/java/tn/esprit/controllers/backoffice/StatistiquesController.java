package tn.esprit.controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import tn.esprit.models.Projet;
import tn.esprit.services.ProjetService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class StatistiquesController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private Label lblTotalCollecte;
    @FXML private Label lblTotalObjectif;
    @FXML private Label lblProgression;

    private final ProjetService ps = new ProjetService();

    public void initialize() {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        try {
            ArrayList<Projet> projets = ps.afficherAll();

            // Séries du BarChart
            XYChart.Series<String, Number> serieCollecte = new XYChart.Series<>();
            serieCollecte.setName("Collecté (DT)");

            XYChart.Series<String, Number> serieObjectif = new XYChart.Series<>();
            serieObjectif.setName("Objectif (DT)");

            double totalCollecte = 0;
            double totalObjectif = 0;

            for (Projet p : projets) {
                // Tronquer le titre si trop long
                String titre = p.getTitreProjet().length() > 15
                        ? p.getTitreProjet().substring(0, 15) + "..."
                        : p.getTitreProjet();

                serieCollecte.getData().add(
                    new XYChart.Data<>(titre, p.getMontantCollecte()));
                serieObjectif.getData().add(
                    new XYChart.Data<>(titre, p.getObjectifFinancier()));

                totalCollecte += p.getMontantCollecte();
                totalObjectif += p.getObjectifFinancier();
            }

            barChart.getData().addAll(serieCollecte, serieObjectif);

            // Cartes résumé
            lblTotalCollecte.setText(String.format("%.2f DT", totalCollecte));
            lblTotalObjectif.setText(String.format("%.2f DT", totalObjectif));

            double pct = totalObjectif > 0 ? (totalCollecte / totalObjectif) * 100 : 0;
            lblProgression.setText(String.format("%.1f%%", pct));

        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }


    @FXML public void goToDashboard() { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AdminDashboard.fxml"); }
    @FXML public void goToProjets()   { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherProjets.fxml"); }
    @FXML public void goToDons()      { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherDons.fxml"); }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            barChart.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
