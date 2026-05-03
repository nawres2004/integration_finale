package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.SessionService;
import tn.esprit.services.UtilisateurService;

public class ProfileController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorTelephone;
    @FXML private Label statusLabel;
    @FXML private Label headerNameLabel;
    @FXML private Label headerRoleLabel;
    @FXML private Label avatarLabel;
    @FXML private Button saveBtn;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur currentUser;

    // Snapshot des valeurs originales pour détecter les changements
    private String originalNom;
    private String originalPrenom;
    private String originalTelephone;

    @FXML
    public void initialize() {
        currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Pré-remplir les champs
        nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
        prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

        // Snapshot
        originalNom       = nomField.getText();
        originalPrenom    = prenomField.getText();
        originalTelephone = telephoneField.getText();

        // Header
        headerNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        headerRoleLabel.setText(getRoleLabel(currentUser.getIdRole()));
        avatarLabel.setText(getAvatarEmoji(currentUser.getIdRole()));

        // Désactiver Save si aucun changement
        saveBtn.setDisable(true);
        nomField.textProperty().addListener((o, ov, nv) -> checkChanges());
        prenomField.textProperty().addListener((o, ov, nv) -> checkChanges());
        telephoneField.textProperty().addListener((o, ov, nv) -> checkChanges());

        // Hover effects
        applyHover(saveBtn,
            "-fx-background-color: linear-gradient(to right, #0055AA, #0088BB); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;",
            "-fx-background-color: linear-gradient(to right, #0066CC, #0099CC); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        applyHover(saveBtn.getScene() != null ? null : null, null, null); // lazy — done after show
    }

    private void checkChanges() {
        boolean changed = !nomField.getText().equals(originalNom)
                || !prenomField.getText().equals(originalPrenom)
                || !telephoneField.getText().equals(originalTelephone);
        saveBtn.setDisable(!changed);
        clearErrors();
    }
//modifier profile
    @FXML
    public void save() {
        clearErrors();
        if (!validate()) return;

        currentUser.setNom(nomField.getText().trim());
        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim());

        boolean ok = utilisateurService.updateProfile(currentUser);

        if (ok) {
            // Mettre à jour la session
            SessionService.getInstance().setCurrentUser(currentUser);

            // Mettre à jour le header
            headerNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

            // Snapshot mis à jour
            originalNom       = currentUser.getNom();
            originalPrenom    = currentUser.getPrenom();
            originalTelephone = currentUser.getTelephone();

            saveBtn.setDisable(true);
            showStatus("✅ Profil mis à jour avec succès !", true);
        } else {
            showStatus("❌ Erreur lors de la mise à jour.", false);
        }
    }

    @FXML
    public void cancel() {
        closeModal();
    }

    private boolean validate() {
        boolean valid = true;

        if (nomField.getText().trim().isEmpty()) {
            errorNom.setText("⚠ Le nom est obligatoire.");
            highlight(nomField, true);
            valid = false;
        } else if (!nomField.getText().trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            errorNom.setText("⚠ Lettres uniquement.");
            highlight(nomField, true);
            valid = false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            errorPrenom.setText("⚠ Le prénom est obligatoire.");
            highlight(prenomField, true);
            valid = false;
        } else if (!prenomField.getText().trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            errorPrenom.setText("⚠ Lettres uniquement.");
            highlight(prenomField, true);
            valid = false;
        }

        if (!telephoneField.getText().trim().isEmpty()) {
            if (!telephoneField.getText().trim().matches("[0-9+\\s\\-]{8,15}")) {
                errorTelephone.setText("⚠ Format invalide (8-15 chiffres).");
                highlight(telephoneField, true);
                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        errorNom.setText("");
        errorPrenom.setText("");
        errorTelephone.setText("");
        statusLabel.setText("");
        highlight(nomField, false);
        highlight(prenomField, false);
        highlight(telephoneField, false);
    }

    private void highlight(TextField field, boolean error) {
        if (error) {
            field.setStyle(field.getStyle().replace("#E2E8F0", "#EF4444"));
        } else {
            field.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0;" +
                    "-fx-border-radius: 8; -fx-background-radius: 8;" +
                    "-fx-padding: 10 14 10 14; -fx-font-size: 14px;");
        }
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#22C55E;" : "#EF4444;"));
    }

    private void closeModal() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void applyHover(Button btn, String hoverStyle, String normalStyle) {
        if (btn == null) return;
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
    }

    private String getRoleLabel(int idRole) {
        switch (idRole) {
            case 1: return "👤 Patient";
            case 2: return "💰 Donateur";
            case 3: return "🩺 Médecin";
            case 4: return "🔑 Administrateur";
            default: return "Utilisateur";
        }
    }

    private String getAvatarEmoji(int idRole) {
        switch (idRole) {
            case 3: return "👨‍⚕️";
            case 4: return "🔑";
            case 2: return "💰";
            default: return "👤";
        }
    }
}
