package tn.esprit.controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import tn.esprit.models.Projet;
import tn.esprit.services.ProjetService;
import tn.esprit.services.SessionService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class StatistiquesFrontController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private Label lblTotalCollecte;
    @FXML private Label lblTotalObjectif;
    @FXML private Label lblProgression;
    @FXML private Label topUserLabel;

    private final ProjetService ps = new ProjetService();

    public void initialize() {
        chargerStatistiques();
        if (topUserLabel != null) {
            tn.esprit.models.Utilisateur u = SessionService.getInstance().getCurrentUser();
            if (u != null) {
                String full = (u.getPrenom() != null ? u.getPrenom() : "") + " " +
                              (u.getNom() != null ? u.getNom() : "");
                topUserLabel.setText("💰 " + full.trim());
            }
        }
    }

    private void chargerStatistiques() {
        try {
            ArrayList<Projet> projets = ps.afficherAll();

            XYChart.Series<String, Number> serieCollecte = new XYChart.Series<>();
            serieCollecte.setName("Collecté (DT)");

            XYChart.Series<String, Number> serieObjectif = new XYChart.Series<>();
            serieObjectif.setName("Objectif (DT)");

            double totalCollecte = 0;
            double totalObjectif = 0;

            for (Projet p : projets) {
                String titre = p.getTitreProjet().length() > 15
                        ? p.getTitreProjet().substring(0, 15) + "..."
                        : p.getTitreProjet();

                serieCollecte.getData().add(new XYChart.Data<>(titre, p.getMontantCollecte()));
                serieObjectif.getData().add(new XYChart.Data<>(titre, p.getObjectifFinancier()));

                totalCollecte += p.getMontantCollecte();
                totalObjectif += p.getObjectifFinancier();
            }

            barChart.getData().addAll(serieCollecte, serieObjectif);

            lblTotalCollecte.setText(String.format("%.2f DT", totalCollecte));
            lblTotalObjectif.setText(String.format("%.2f DT", totalObjectif));

            double pct = totalObjectif > 0 ? (totalCollecte / totalObjectif) * 100 : 0;
            lblProgression.setText(String.format("%.1f%%", pct));

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML public void goToProjets() { naviguer("/frontoffice/FrontProjets.fxml"); }
    @FXML public void goToAdmin()   { naviguer("/backoffice/AdminDashboard.fxml"); }

    @FXML
    public void logout() {
        SessionService.getInstance().clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            barChart.getScene().setRoot(root);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            barChart.getScene().setRoot(root);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }
}
