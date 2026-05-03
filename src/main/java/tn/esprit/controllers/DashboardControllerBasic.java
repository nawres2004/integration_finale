package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardControllerBasic {

    @FXML
    public void initialize() {
        System.out.println("=== DASHBOARD BASIC INITIALISÉ ===");
        System.out.println("✅ Controller initialisé sans @FXML");
    }

    @FXML
    public void handleLogout() {
        System.out.println("=== BOUTON DÉCONNEXION CLIQUÉ ===");
        System.exit(0);
    }
}
