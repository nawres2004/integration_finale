package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.RoleService;
import tn.esprit.services.SessionService;

import java.io.IOException;

public class DonateurDashboardController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label roleLabel;
    @FXML private Label topUserLabel;
    
    private RoleService roleService = new RoleService();

    @FXML
    public void initialize() {
        loadUserProfile();
    }

    private void loadUserProfile() {
        Utilisateur currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser != null) {
            String fullName = (currentUser.getPrenom() != null ? currentUser.getPrenom() : "") + " " +
                              (currentUser.getNom() != null ? currentUser.getNom() : "");
            if (topUserLabel != null) topUserLabel.setText("💰 " + fullName.trim());
            nomLabel.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
            prenomLabel.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            emailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            telephoneLabel.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
            
            // Charger le nom du rôle
            String roleName = roleService.getRoleNameById(currentUser.getIdRole());
            roleLabel.setText(roleName != null ? roleName : "Donateur");
        }
    }

    @FXML
    public void goToProjets() {
        naviguer("/frontoffice/FrontProjets.fxml");
    }

    @FXML
    public void goToStats() {
        naviguer("/frontoffice/StatistiquesFront.fxml");
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            nomLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openChangePassword() {
        tn.esprit.utils.ProfileHelper.openChangePassword(nomLabel.getScene().getWindow());
    }

    @FXML
    public void openProfile() {
        tn.esprit.utils.ProfileHelper.open(nomLabel.getScene().getWindow());
        loadUserProfile();
    }

    @FXML
    public void logout() {
        SessionService.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 550);
            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - VitaPlus");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
