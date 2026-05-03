package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.models.Medicament;
import tn.esprit.models.Ordonnance;
import tn.esprit.services.ServiceMedicament;
import tn.esprit.services.ServiceOrdonnance;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterMedicamentController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField dosageField;
    @FXML
    private TextField formeField;
    @FXML
    private TextField frequenceField;
    @FXML
    private DatePicker dateExpirationPicker;
    @FXML
    private TextArea contreIndicationsArea;

    private ServiceMedicament sm = new ServiceMedicament();
    private tn.esprit.services.AIService aiService = new tn.esprit.services.AIService();

    @FXML
    public void initialize() {
        // Initialization without ordonnance selection
    }

    @FXML
    void handleAICheck(ActionEvent event) {
        String nom = nomField.getText();
        String dosage = dosageField.getText();

        if (nom.isEmpty()) {
            showAlert("Attention", "Veuillez saisir au moins le nom du médicament.", Alert.AlertType.WARNING);
            return;
        }

        // Show loading state (could be improved with a real loader)
        contreIndicationsArea.setPromptText("L'IA réfléchit...");
        contreIndicationsArea.setDisable(true);

        new Thread(() -> {
            try {
                String result = aiService.getContreIndications(nom, dosage);
                javafx.application.Platform.runLater(() -> {
                    contreIndicationsArea.setText(result);
                    contreIndicationsArea.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Erreur IA", "Impossible de contacter l'IA : " + e.getMessage(), Alert.AlertType.ERROR);
                    contreIndicationsArea.setDisable(false);
                    contreIndicationsArea.setPromptText("Précisez les allergies ou contre-indications...");
                });
            }
        }).start();
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        String nom = nomField.getText();
        String dosage = dosageField.getText();
        String forme = formeField.getText();
        String frequence = frequenceField.getText();
        LocalDate dateExp = dateExpirationPicker.getValue();
        String contreIndications = contreIndicationsArea.getText();

        if (nom.isEmpty() || dosage.isEmpty() || forme.isEmpty() || dateExp == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
            return;
        }

        if (!nom.matches("^[a-zA-Z\\s]+$")) {
            showAlert("Erreur", "Le nom du médicament ne doit contenir que des lettres.", Alert.AlertType.WARNING);
            return;
        }

        if (dateExp.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date d'expiration doit être dans le futur.", Alert.AlertType.WARNING);
            return;
        }

        Medicament m = new Medicament(nom, dosage, forme, frequence, contreIndications, dateExp, 0);

        try {
            if (sm.exists(nom, dosage)) {
                showAlert("Avertissement", "Ce médicament (" + nom + ") existe déjà avec ce dosage.", Alert.AlertType.WARNING);
                return;
            }
            sm.insertOnePS(m);
            showAlert("Succès", "Médicament ajouté avec succès !", Alert.AlertType.INFORMATION);
            showList(null);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void showList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherMedicament.fxml"));
            Parent root = loader.load();
            
            // Look for Dashboard contentPane
            StackPane contentPane = (StackPane) nomField.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(root);
            } else {
                nomField.getScene().setRoot(root);
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
        nomField.clear();
        dosageField.clear();
        formeField.clear();
        frequenceField.clear();
        dateExpirationPicker.setValue(null);
        contreIndicationsArea.clear();
    }
}
