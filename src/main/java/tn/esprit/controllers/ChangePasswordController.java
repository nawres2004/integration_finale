package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.AuthService;
import tn.esprit.services.SessionService;

public class ChangePasswordController {

    @FXML private Label         userEmailLabel;
    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label         errorOld;
    @FXML private Label         errorNew;
    @FXML private Label         errorConfirm;
    @FXML private Label         statusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        Utilisateur u = SessionService.getInstance().getCurrentUser();
        if (u != null && u.getEmail() != null) {
            userEmailLabel.setText(u.getEmail());
        }
    }

    @FXML
    public void save() {
        clearErrors();

        String oldPass  = oldPassword.getText();
        String newPass  = newPassword.getText();
        String confirm  = confirmPassword.getText();
        boolean valid   = true;

        // Validation ancien mot de passe
        if (oldPass.isEmpty()) {
            errorOld.setText("⚠ Champ obligatoire.");
            valid = false;
        }

        // Validation nouveau mot de passe
        if (newPass.isEmpty()) {
            errorNew.setText("⚠ Champ obligatoire.");
            valid = false;
        } else if (newPass.length() < 8) {
            errorNew.setText("⚠ Minimum 8 caractères.");
            valid = false;
        } else if (!newPass.matches(".*[A-Z].*")) {
            errorNew.setText("⚠ Doit contenir au moins une majuscule.");
            valid = false;
        } else if (!newPass.matches(".*[0-9].*")) {
            errorNew.setText("⚠ Doit contenir au moins un chiffre.");
            valid = false;
        }

        // Validation confirmation
        if (confirm.isEmpty()) {
            errorConfirm.setText("⚠ Champ obligatoire.");
            valid = false;
        } else if (!confirm.equals(newPass)) {
            errorConfirm.setText("⚠ Les mots de passe ne correspondent pas.");
            valid = false;
        }

        if (!valid) return;

        // Vérifier l'ancien mot de passe
        Utilisateur currentUser = SessionService.getInstance().getCurrentUser();
        if (authService.login(currentUser.getEmail(), oldPass) == null) {
            errorOld.setText("⚠ Mot de passe actuel incorrect.");
            return;
        }

        // Mettre à jour
        boolean ok = authService.updatePassword(currentUser.getIdUtilisateur(), newPass);
        if (ok) {
            showStatus("✅ Mot de passe mis à jour avec succès !", true);
            oldPassword.clear();
            newPassword.clear();
            confirmPassword.clear();
            // Fermer après 1.5s
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(this::closeModal);
            }).start();
        } else {
            showStatus("❌ Erreur lors de la mise à jour.", false);
        }
    }

    @FXML
    public void cancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) oldPassword.getScene().getWindow();
        stage.close();
    }

    private void clearErrors() {
        errorOld.setText("");
        errorNew.setText("");
        errorConfirm.setText("");
        statusLabel.setText("");
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + (success ? "#22C55E;" : "#EF4444;"));
    }
}
