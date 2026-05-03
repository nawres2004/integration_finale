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

public class ModifierOrdonnanceController {

    @FXML
    private TextField idField;
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
        // We still need to populate users to show the name, but we will lock selection
        try {
            List<Utilisateur> users = su.findALL();
            userCombo.setItems(FXCollections.observableArrayList(users));
            userCombo.setConverter(new StringConverter<Utilisateur>() {
                @Override public String toString(Utilisateur u) { return u == null ? "" : u.getNom(); }
                @Override public Utilisateur fromString(String string) { return null; }
            });
            
            // Lock the combo box as requested
            userCombo.setDisable(true);
            userCombo.setStyle("-fx-opacity: 1; -fx-text-fill: black;"); // Keep it readable
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setData(Ordonnance o) {
        idField.setText(String.valueOf(o.getId()));
        datePicker.setValue(o.getDateOrdonnance());
        dureeField.setText(o.getDureeTraitement());
        instructionsArea.setText(o.getInstructions());
        
        // Find and select the user in the combo
        for (Utilisateur u : userCombo.getItems()) {
            if (u.getIdUtilisateur() == o.getIdUtilisateur()) {
                userCombo.setValue(u);
                break;
            }
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        int id = Integer.parseInt(idField.getText());
        LocalDate date = datePicker.getValue();
        String duree = dureeField.getText();
        String instructions = instructionsArea.getText();
        Utilisateur user = userCombo.getValue();

        if (date == null || duree.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires.", Alert.AlertType.ERROR);
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

        Ordonnance o = new Ordonnance(id, date, instructions, duree, user.getIdUtilisateur(), user.getNom(), false);

        try {
            so.updateOne(o);
            showAlert("Succès", "Ordonnance modifiée avec succès !", Alert.AlertType.INFORMATION);
            showList(null);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void showList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherOrdonnance.fxml"));
            Parent root = loader.load();
            StackPane contentPane = (StackPane) datePicker.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(root);
            else datePicker.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
