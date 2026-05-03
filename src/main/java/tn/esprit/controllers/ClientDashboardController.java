package tn.esprit.controllers;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.RoleService;
import tn.esprit.services.SessionService;

public class ClientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label topUserLabel;
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentPane;
    
    private RoleService roleService = new RoleService();

    @FXML
    public void initialize() {
        loadUserProfile();
        tn.esprit.utils.SessionBridge.sync();
    }

    private void loadUserProfile() {
        Utilisateur currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser != null) {
            String fullName = (currentUser.getPrenom() != null ? currentUser.getPrenom() : "") + " " +
                              (currentUser.getNom() != null ? currentUser.getNom() : "");
            if (welcomeLabel != null) welcomeLabel.setText("👋 Bonjour " + fullName.trim());
            if (topUserLabel != null) topUserLabel.setText("👤 " + fullName.trim());
            nomLabel.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
            prenomLabel.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            emailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            telephoneLabel.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
            
            // Charger le nom du rôle
            String roleName = roleService.getRoleNameById(currentUser.getIdRole());
            roleLabel.setText(roleName != null ? roleName : "Client");
        }
    }

    @FXML
    public void openMap() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/map_view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1100, 700);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("🗺️ Médecins proches - VitaPlus");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openChangePassword(Event event) {
        tn.esprit.utils.ProfileHelper.openChangePassword(((Node)event.getSource()).getScene().getWindow());
    }

    @FXML
    public void openProfile(Event event) {
        tn.esprit.utils.ProfileHelper.open(((Node)event.getSource()).getScene().getWindow());
        loadUserProfile(); // Rafraîchir après modification
    }

    @FXML
    public void logout(Event event) {
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
    public void openRendezVous() {
        loadView("/views/AfficherRendezVous.fxml");
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
