package tn.esprit.controllers.backoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.models.Projet;
import tn.esprit.services.ProjetService;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;

public class AjouterProjetController {

    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private TextField tfObjectif;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;

    ProjetService ps = new ProjetService();

    public void initialize() {
        StringConverter<LocalDate> converter = new StringConverter<>() {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate d) { return d != null ? fmt.format(d) : ""; }
            @Override public LocalDate fromString(String s) { return (s != null && !s.isEmpty()) ? LocalDate.parse(s, fmt) : null; }
        };
        dpDebut.setConverter(converter);
        dpFin.setConverter(converter);
    }

    @FXML
    public void ajouterProjet() {
        String titre = tfTitre.getText().trim();
        String desc = taDescription.getText().trim();
        String obj = tfObjectif.getText().trim();

        // ✅ Validation
        if (titre.isEmpty() || desc.isEmpty() || obj.isEmpty()
                || dpDebut.getValue() == null || dpFin.getValue() == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Champs vides",
                    "Tous les champs sont obligatoires.");
            return;
        }

        if (titre.length() < 3) {
            showAlert(Alert.AlertType.WARNING,
                    "Titre invalide",
                    "Minimum 3 caractères.");
            return;
        }

        if (titre.length() > 150) {
            showAlert(Alert.AlertType.WARNING,
                    "Titre trop long",
                    "Maximum 150 caractères.");
            return;
        }

        if (desc.length() < 10) {
            showAlert(Alert.AlertType.WARNING,
                    "Description invalide",
                    "Minimum 10 caractères.");
            return;
        }

        if (dpFin.getValue().isBefore(dpDebut.getValue())) {
            showAlert(Alert.AlertType.WARNING,
                    "Date invalide",
                    "La date de fin doit être >= date de début.");
            return;
        }

        try {
            double objectif = Double.parseDouble(obj);

            if (objectif <= 0) {
                showAlert(Alert.AlertType.WARNING,
                        "Objectif invalide",
                        "L'objectif doit être > 0.");
                return;
            }

            //  Conversion LocalDate → java.sql.Date
            Date dateDebut = Date.valueOf(dpDebut.getValue());
            Date dateFin = Date.valueOf(dpFin.getValue());

            // Création objet Projet
            Projet p = new Projet(
                    titre, desc, objectif,
                    0, // montant_collecte initial
                    dateDebut, dateFin
            );

            ps.ajouter(p);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès", "✅ Projet créé !");
            clearForm();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "L'objectif doit être un nombre.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    public void goToAfficher() {
        tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherProjets.fxml");
    }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void clearForm() {
        tfTitre.clear();
        taDescription.clear();
        tfObjectif.clear();
        dpDebut.setValue(null);
        dpFin.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}