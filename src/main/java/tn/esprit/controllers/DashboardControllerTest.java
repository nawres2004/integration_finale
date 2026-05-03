package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardControllerTest {

    @FXML private Label userNameLabel;
    @FXML private Label testLabel;

    @FXML
    public void initialize() {
        System.out.println("=== DASHBOARD CONTROLLER TEST INITIALISÉ ===");
        System.out.println("userNameLabel: " + (userNameLabel != null ? "NULL" : "OK"));
        System.out.println("testLabel: " + (testLabel != null ? "NULL" : "OK"));
    }

    @FXML
    public void handleLogout() {
        System.out.println("=== BOUTON DÉCONNEXION CLIQUÉ ===");
        System.exit(0);
    }
}
