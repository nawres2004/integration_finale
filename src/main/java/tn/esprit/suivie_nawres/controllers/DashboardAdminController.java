package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.SceneManager;
import tn.esprit.suivie_nawres.utils.UserRole;

public class DashboardAdminController {
    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        RoleContext.setCurrentRole(UserRole.ADMIN);
        afficherRendezVous();
    }

    @FXML
    private void afficherRendezVous() {
        SceneManager.replaceContent(contentPane, "/views/AfficherRendezVous.fxml");
    }

    @FXML
    private void afficherConsultations() {
        SceneManager.replaceContent(contentPane, "/views/AfficherConsultation.fxml");
    }

    @FXML
    private void retourChoixRole() {
        SceneManager.show("/views/ChoixRole.fxml", "VitaPlus Medical - Choix du role");
    }
}

