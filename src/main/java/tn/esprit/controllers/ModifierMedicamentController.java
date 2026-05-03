package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import tn.esprit.models.Medicament;
import tn.esprit.services.ServiceMedicament;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierMedicamentController {

    @FXML private TextField idModifField;
    @FXML private TextField newNom;
    @FXML private TextField newDosage;
    @FXML private TextField newForme;
    @FXML private TextField newFrequence;
    @FXML private DatePicker newExpirationPicker;
    @FXML private TextArea newContreIndications;

    private ServiceMedicament sm = new ServiceMedicament();
    private int ordonnanceId; // Keep track of parent ordonnance

    public void setData(Medicament m) {
        idModifField.setText(String.valueOf(m.getId()));
        newNom.setText(m.getNom());
        newDosage.setText(m.getDosage());
        newForme.setText(m.getForme());
        newFrequence.setText(m.getFrequence());
        newExpirationPicker.setValue(m.getDateExpiration());
        newContreIndications.setText(m.getContreIndications());
        this.ordonnanceId = m.getOrdonnanceId();
    }

    @FXML
    void handleModifier(ActionEvent event) {
        int id = Integer.parseInt(idModifField.getText());
        String nom = newNom.getText();
        String dosage = newDosage.getText();
        String forme = newForme.getText();
        String frequence = newFrequence.getText();
        LocalDate date = newExpirationPicker.getValue();
        String contre = newContreIndications.getText();

        if (nom.isEmpty() || dosage.isEmpty() || date == null) {
            showAlert("Erreur", "Veuillez remplir les champs obligatoires.", Alert.AlertType.ERROR);
            return;
        }

        if (!nom.matches("^[a-zA-Z\\s]+$")) {
            showAlert("Erreur", "Le nom du médicament ne doit contenir que des lettres.", Alert.AlertType.WARNING);
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date d'expiration doit être dans le futur.", Alert.AlertType.WARNING);
            return;
        }

        Medicament m = new Medicament(id, nom, dosage, forme, frequence, contre, date, ordonnanceId);

        try {
            if (sm.existsForUpdate(nom, dosage, id)) {
                showAlert("Avertissement", "Un autre médicament existe déjà avec ce nom et ce dosage.", Alert.AlertType.WARNING);
                return;
            }
            sm.updateOne(m);
            showAlert("Succès", "Médicament modifié avec succès !", Alert.AlertType.INFORMATION);
            showList(null);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void showList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherMedicament.fxml"));
            Parent root = loader.load();
            StackPane contentPane = (StackPane) newNom.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(root);
            else newNom.getScene().setRoot(root);
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
