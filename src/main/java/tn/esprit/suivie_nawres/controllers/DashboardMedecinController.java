package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.utils.MedecinNavigationContext;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.SceneManager;
import tn.esprit.suivie_nawres.utils.UserRole;

public class DashboardMedecinController {
    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        RoleContext.setCurrentRole(UserRole.MEDECIN);
        MedecinNavigationContext.setContentContainer(contentPane);
    }

    @FXML
    private void gererRendezVous() {
        ouvrirModale("/views/AfficherRendezVous.fxml", "📅 Mes Rendez-vous", 1400, 850);
    }

    @FXML
    private void traiterDemandesRendezVous() {
        SceneManager.replaceContent(contentPane, "/views/TraiterDemandesRendezVous.fxml");
    }

    @FXML
    private void ajouterRendezVous() {
        ouvrirModale("/views/AjouterRendezVous.fxml", "Creer Rendez-vous", 860, 760);
    }

    @FXML
    private void gererConsultations() {
        ouvrirModale("/views/AfficherConsultation.fxml", "🏥 Mes Consultations", 1400, 850);
    }

    @FXML
    private void ajouterConsultation() {
        ouvrirModale("/views/AjouterConsultation.fxml", "Creer Consultation", 900, 760);
    }

    @FXML
    private void afficherCalendrier() {
        SceneManager.replaceContent(contentPane, "/views/Calendrier.fxml");
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

