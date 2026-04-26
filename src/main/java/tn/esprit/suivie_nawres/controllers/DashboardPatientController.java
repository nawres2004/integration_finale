package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

public class DashboardPatientController {
    @FXML
    private StackPane contentPane;

    @FXML
    private void initialize() {
        RoleContext.setCurrentRole(UserRole.PATIENT);
        contentPane.setManaged(false);
        contentPane.setVisible(false);
    }

    @FXML
    private void demanderRendezVous() {
        AjouterRendezVousController.setFermerApresEnregistrement(true);
        ouvrirFenetreModale("/views/AjouterRendezVous.fxml", "VitaPlus Medical - Nouveau rendez-vous", 860, 760);
    }

    @FXML
    private void voirMesRendezVous() {
        ouvrirFenetreModale("/views/AfficherRendezVous.fxml", "VitaPlus Medical - Mes rendez-vous", 1280, 780);
    }


    @FXML
    private void retourChoixRole() {
        tn.esprit.suivie_nawres.utils.SceneManager.show("/views/ChoixRole.fxml", "VitaPlus Medical - Choix du role");
    }

    private void ouvrirFenetreModale(String fxmlPath, String titre, int width, int height) {
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

