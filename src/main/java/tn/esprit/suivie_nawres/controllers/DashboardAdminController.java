package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.SceneManager;
import tn.esprit.suivie_nawres.utils.UserRole;

public class DashboardAdminController {
    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        RoleContext.setCurrentRole(UserRole.ADMIN);
    }

    @FXML
    private void afficherRendezVous() {
        ouvrirModale("/views/AfficherRendezVous.fxml", "📅 Tous les Rendez-vous", 1400, 850);
    }

    @FXML
    private void afficherConsultations() {
        ouvrirModale("/views/AfficherConsultation.fxml", "🏥 Toutes les Consultations", 1400, 850);
    }

    @FXML
    private void afficherStatistiques() {
        ouvrirModale("/views/StatistiquesAdmin.fxml", "📊 Statistiques Médicales", 1400, 900);
    }

    @FXML
    private void retourChoixRole() {
        SceneManager.show("/views/ChoixRole.fxml", "VitaPlus Medical - Choix du role");
    }

    private void ouvrirModale(String fxmlPath, String titre, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.NONE); // Permet de fermer la fenêtre librement
            stage.setTitle(titre);
            stage.setScene(scene);
            stage.show(); // show() au lieu de showAndWait() pour ne pas bloquer
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible d'ouvrir la fenetre : " + fxmlPath, exception);
        }
    }
}

