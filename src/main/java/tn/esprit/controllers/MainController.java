package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.services.ServiceMedicament;
import tn.esprit.services.ServiceOrdonnance;

import java.io.IOException;
import java.sql.SQLException;

public class MainController {

    @FXML
    private Button btnAccueil;
    @FXML
    private Button btnOrdonnances;
    @FXML
    private Button btnMedicaments;
    @FXML
    private Button btnSuiviMedical;
    @FXML
    private Button btnProfil;
    @FXML
    private Button btnRendezVous;

    @FXML
    private StackPane contentPane;

    @FXML
    private Label lblMedCount;
    @FXML
    private Label lblOrdCount;

    private ServiceMedicament sm = new ServiceMedicament();
    private ServiceOrdonnance so = new ServiceOrdonnance();

    @FXML
    public void initialize() {
        showWelcome(null); // Default view is now the Welcome dashboard
    }

    @FXML
    void showWelcome(ActionEvent event) {
        loadView("/Welcome.fxml");
    }

    @FXML
    void showMedicaments(ActionEvent event) {
        loadView("/AfficherMedicament.fxml");
    }

    @FXML
    void showOrdonnances(ActionEvent event) {
        loadView("/AfficherOrdonnance.fxml");
    }

    @FXML
    void showAjoutOrdonnance(ActionEvent event) {
        loadView("/AjouterOrdonnance.fxml");
    }

    @FXML
    void openAddMedicament(ActionEvent event) {
        loadView("/AjouterMedicament.fxml");
    }

    @FXML
    void showGestionMedicament(ActionEvent event) {
        loadView("/AfficherMedicament.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    // Placeholder methods to match user provided code
    @FXML
    void filterMedicaments(ActionEvent event) {
    }

    @FXML
    void loadMedicaments(ActionEvent event) {
    }

    @FXML
    void onMedicamentSelected(javafx.scene.input.MouseEvent event) {
    }

    @FXML
    void searchMedicaments(javafx.scene.input.KeyEvent event) {
    }
}
