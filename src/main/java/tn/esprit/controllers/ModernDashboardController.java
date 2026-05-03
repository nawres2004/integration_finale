package tn.esprit.controllers;

import tn.esprit.services.SessionService;
import tn.esprit.models.Utilisateur;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class ModernDashboardController {

    @FXML private StackPane mainContainer;
    @FXML private VBox sidebarContainer;

    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionService.getInstance().getCurrentUser();

        if (currentUser == null) {
            System.out.println("ERREUR: Aucun utilisateur en session");
            return;
        }

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));

        String roleName;
        switch (currentUser.getIdRole()) {
            case 4: roleName = "Administrateur"; break;
            case 3: roleName = "Médecin"; break;
            case 2: roleName = "Donateur"; break;
            case 1: roleName = "Patient"; break;
            default: roleName = "Utilisateur"; break;
        }

        Label welcomeLabel = new Label("🏥 Vita+ - " + roleName);
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #0066CC;");

        Label userLabel = new Label("Bienvenue " + currentUser.getPrenom() + " " + currentUser.getNom());
        userLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #1E293B;");

        content.getChildren().addAll(welcomeLabel, userLabel);

        if (mainContainer != null) {
            mainContainer.getChildren().add(content);
            mainContainer.setStyle("-fx-background-color: #F1F5F9;");
        }
    }

    @FXML
    public void handleLogout() {
        SessionService.getInstance().clearSession();
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) mainContainer.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Login - VitaPlus");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
