package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import tn.esprit.models.Ordonnance;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceOrdonnance;
import tn.esprit.services.ServiceUtilisateur;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterOrdonnanceController {

    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField dureeField;
    @FXML
    private TextArea instructionsArea;
    @FXML
    private ComboBox<Utilisateur> userCombo;

    private ServiceOrdonnance so = new ServiceOrdonnance();
    private ServiceUtilisateur su = new ServiceUtilisateur();

    @FXML
    public void initialize() {
        try {
            List<Utilisateur> users = su.findALL();
            ObservableList<Utilisateur> observableUsers = FXCollections.observableArrayList(users);
            userCombo.setItems(observableUsers);

            // Setup Converter for Displaying User Names
            userCombo.setConverter(new StringConverter<Utilisateur>() {
                @Override
                public String toString(Utilisateur user) {
                    return user == null ? "" : user.getNom();
                }

                @Override
                public Utilisateur fromString(String string) {
                    return null; // Not needed
                }
            });
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        LocalDate date = datePicker.getValue();
        String duree = dureeField.getText();
        String instructions = instructionsArea.getText();
        Utilisateur selectedUser = userCombo.getValue();

        if (date == null || duree.isEmpty() || selectedUser == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
            return;
        }

        if (!date.equals(LocalDate.now())) {
            showAlert("Erreur", "La date de l'ordonnance doit être celle d'aujourd'hui (" + LocalDate.now() + ").", Alert.AlertType.ERROR);
            return;
        }

        if (instructions.trim().length() < 7) {
            showAlert("Erreur", "Les instructions doivent contenir au moins 7 caractères.", Alert.AlertType.WARNING);
            return;
        }

        if (!duree.matches(".*\\d+.*")) {
            showAlert("Erreur", "La durée doit spécifier un nombre (ex: 7 jours, 1 mois).", Alert.AlertType.WARNING);
            return;
        }

        Ordonnance o = new Ordonnance(date, instructions, duree, selectedUser.getIdUtilisateur(), false);

        try {
            so.insertOne(o);
            showAlert("Succès", "Ordonnance enregistrée avec succès !", Alert.AlertType.INFORMATION);
            showList(null);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void showList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherOrdonnance.fxml"));
            Parent root = loader.load();
            
            StackPane contentPane = (StackPane) datePicker.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(root);
            } else {
                datePicker.getScene().setRoot(root);
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la liste : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        datePicker.setValue(null);
        dureeField.clear();
        instructionsArea.clear();
        userCombo.setValue(null);
    }
}
