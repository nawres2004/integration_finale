package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.SessionService;

public class MedecinDashboardController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label specialiteLabel;
    @FXML private Label topUserLabel;
    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        loadUserProfile();
        tn.esprit.utils.SessionBridge.sync();
    }

    private void loadUserProfile() {
        Utilisateur u = SessionService.getInstance().getCurrentUser();
        if (u == null) return;
        String fullName = (u.getPrenom() != null ? u.getPrenom() : "") + " "
                        + (u.getNom() != null ? u.getNom() : "");
        if (topUserLabel != null) topUserLabel.setText("👨‍⚕️ Dr. " + fullName.trim());
        nomLabel.setText(u.getNom() != null ? u.getNom() : "");
        prenomLabel.setText(u.getPrenom() != null ? u.getPrenom() : "");
        emailLabel.setText(u.getEmail() != null ? u.getEmail() : "");
        telephoneLabel.setText(u.getTelephone() != null ? u.getTelephone() : "");
        specialiteLabel.setText(u.getSpecialite() != null ? u.getSpecialite() : "");
    }

    @FXML
    public void openLocation() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/medecin_location.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 900, 600);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("📍 Ma Localisation - VitaPlus");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openChangePassword(ActionEvent event) {
        tn.esprit.utils.ProfileHelper.openChangePassword(((Node)event.getSource()).getScene().getWindow());
    }

    @FXML
    public void openProfile(ActionEvent event) {
        tn.esprit.utils.ProfileHelper.open(((Node)event.getSource()).getScene().getWindow());
        loadUserProfile();
    }

    @FXML
    public void logout(ActionEvent event) {
        SessionService.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 550);
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - VitaPlus");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openOrdonnances() {
        loadView("/AfficherOrdonnance.fxml");
    }

    @FXML
    public void openMedicaments() {
        loadView("/AfficherMedicament.fxml");
    }

    @FXML
    public void openBlogEtInformations() {
        try {
            tn.esprit.utils.SessionBridge.sync(); // Sync session first
            org.example.App.integrationPane = contentPane;
            contentPane.getChildren().setAll(
                (javafx.scene.Node) new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/article_list.fxml")).load()
            );
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (contentPane != null) {
                contentPane.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
