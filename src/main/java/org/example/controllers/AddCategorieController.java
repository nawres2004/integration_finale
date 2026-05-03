package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.entities.Categorie;
import org.example.services.ServiceCategorie;

import java.io.IOException;

public class AddCategorieController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private CheckBox patientPostCheck;

    @FXML
    private CheckBox doctorPostCheck;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private Label descErrorLabel;

    private ServiceCategorie serviceCategorie = new ServiceCategorie();

    @FXML
    void handleAddCategorie(ActionEvent event) {
        // Reset errors
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        descErrorLabel.setVisible(false);
        descErrorLabel.setManaged(false);

        String nom = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean allowPatients = patientPostCheck.isSelected();
        boolean allowDoctors = doctorPostCheck.isSelected();

        boolean isValid = true;

        if (nom.isEmpty()) {
            showError(nameErrorLabel, "Name is required.");
            isValid = false;
        } else if (nom.length() <= 5) {
            showError(nameErrorLabel, "Name must be more than 5 characters.");
            isValid = false;
        }

        if (description.isEmpty()) {
            showError(descErrorLabel, "Description is required.");
            isValid = false;
        } else if (description.length() <= 5) {
            showError(descErrorLabel, "Description must be more than 5 characters.");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        Categorie categorie = new Categorie(nom, description, allowPatients, allowDoctors);
        serviceCategorie.add(categorie);

        showAlert(Alert.AlertType.INFORMATION, "Success!", "Category added successfully.");
        clearForm();
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        App.setRoot("primary");
    }

    @FXML
    void handleLogOutFromCategorie(ActionEvent event) throws IOException {
        org.example.utils.UserSession.cleanUserSession();
        App.setRoot("signin");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void clearForm() {
        nameField.clear();
        descriptionArea.clear();
        patientPostCheck.setSelected(false);
        doctorPostCheck.setSelected(false);
    }
}
