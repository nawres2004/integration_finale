package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.PasswordResetService;
import tn.esprit.services.PasswordResetService.VerifyResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ForgotPasswordController {

    // ── Étape 1 : Email ──────────────────────────────────────
    @FXML private VBox      stepEmail;
    @FXML private TextField emailField;
    @FXML private Label     errorEmail;
    @FXML private Label     infoEmail;
    @FXML private Button    sendCodeBtn;

    // ── Étape 2 : Code ───────────────────────────────────────
    @FXML private VBox      stepCode;
    @FXML private Label     emailSentLabel;
    @FXML private TextField codeField;
    @FXML private Label     errorCode;
    @FXML private Button    resendBtn;

    // ── Étape 3 : Nouveau mot de passe ───────────────────────
    @FXML private VBox          stepNewPassword;
    @FXML private PasswordField newPassField;
    @FXML private PasswordField confirmPassField;
    @FXML private Label         errorNewPass;
    @FXML private Label         errorConfirmPass;
    @FXML private Label         successLabel;

    private final PasswordResetService resetService = new PasswordResetService();
    private String verifiedEmail;

    // Cooldown renvoi (30 secondes)
    private static final int RESEND_COOLDOWN = 30;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // ════════════════════════════════════════════════════════
    // ÉTAPE 1 — Envoyer le code
    // ════════════════════════════════════════════════════════
    @FXML
    public void sendCode() {
        clearAll();
        String email = emailField.getText().trim().toLowerCase();

        if (email.isEmpty()) {
            errorEmail.setText("⚠ L'email est obligatoire.");
            return;
        }
        if (!isValidEmail(email)) {
            errorEmail.setText("⚠ Format invalide (ex: nom@domaine.com).");
            return;
        }

        sendCodeBtn.setDisable(true);
        infoEmail.setText("⏳ Envoi en cours...");

        new Thread(() -> {
            // requestReset retourne toujours true (message générique)
            resetService.requestReset(email);
            Platform.runLater(() -> {
                verifiedEmail = email;
                // Message générique — ne révèle pas si l'email existe
                infoEmail.setText("✅ Si ce compte existe, un code a été envoyé.");
                showStep(2);
                emailSentLabel.setText("Code envoyé à : " + maskEmail(email));
                sendCodeBtn.setDisable(false);
            });
        }).start();
    }

    // ════════════════════════════════════════════════════════
    // ÉTAPE 2 — Vérifier le code
    // ════════════════════════════════════════════════════════
    @FXML
    public void verifyCode() {
        errorCode.setText("");
        String code = codeField.getText().trim();

        if (code.isEmpty() || !code.matches("\\d{6}")) {
            errorCode.setText("⚠ Entrez le code à 6 chiffres reçu par email.");
            return;
        }

        VerifyResult result = resetService.verifyCode(verifiedEmail, code);

        if (result == VerifyResult.SUCCESS) {
            showStep(3);
        } else {
            // Message générique — ne révèle pas le code stocké
            errorCode.setText("❌ " + result.getMessage());
            codeField.clear();

            // Si max tentatives ou expiré → retour étape 1
            if (result == VerifyResult.MAX_ATTEMPTS || result == VerifyResult.EXPIRED) {
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        codeField.clear();
                        showStep(1);
                    });
                }).start();
            }
        }
    }

    @FXML
    public void resendCode() {
        resendBtn.setDisable(true);
        errorCode.setText("");

        new Thread(() -> {
            resetService.resendCode(verifiedEmail);
            Platform.runLater(() -> {
                codeField.clear();
                errorCode.setStyle("-fx-text-fill: #22C55E;");
                errorCode.setText("✅ Nouveau code envoyé !");
                startResendCooldown();
            });
        }).start();
    }

    /** Cooldown 30 secondes sur le bouton Renvoyer */
    private void startResendCooldown() {
        AtomicInteger remaining = new AtomicInteger(RESEND_COOLDOWN);
        scheduler.scheduleAtFixedRate(() -> {
            int left = remaining.getAndDecrement();
            Platform.runLater(() -> {
                if (left > 0) {
                    resendBtn.setText("Renvoyer (" + left + "s)");
                } else {
                    resendBtn.setText("Renvoyer le code");
                    resendBtn.setDisable(false);
                    errorCode.setText("");
                    errorCode.setStyle("-fx-text-fill: #EF4444;");
                }
            });
            if (left <= 0) throw new RuntimeException("stop"); // arrêter le scheduler
        }, 1, 1, TimeUnit.SECONDS);
    }

    @FXML
    public void backToEmail() {
        codeField.clear();
        errorCode.setText("");
        showStep(1);
    }

    // ════════════════════════════════════════════════════════
    // ÉTAPE 3 — Nouveau mot de passe
    // ════════════════════════════════════════════════════════
    @FXML
    public void resetPassword() {
        errorNewPass.setText("");
        errorConfirmPass.setText("");
        successLabel.setText("");

        String newPass  = newPassField.getText();
        String confirm  = confirmPassField.getText();
        boolean valid   = true;

        if (newPass.isEmpty()) {
            errorNewPass.setText("⚠ Champ obligatoire.");
            valid = false;
        } else if (newPass.length() < 8) {
            errorNewPass.setText("⚠ Minimum 8 caractères.");
            valid = false;
        } else if (!newPass.matches(".*[A-Z].*")) {
            errorNewPass.setText("⚠ Doit contenir au moins une majuscule.");
            valid = false;
        } else if (!newPass.matches(".*[0-9].*")) {
            errorNewPass.setText("⚠ Doit contenir au moins un chiffre.");
            valid = false;
        }

        if (confirm.isEmpty()) {
            errorConfirmPass.setText("⚠ Confirmez le mot de passe.");
            valid = false;
        } else if (!confirm.equals(newPass)) {
            errorConfirmPass.setText("⚠ Les mots de passe ne correspondent pas.");
            valid = false;
        }

        if (!valid) return;

        boolean ok = resetService.resetPassword(verifiedEmail, newPass);
        if (ok) {
            newPassField.setDisable(true);
            confirmPassField.setDisable(true);
            successLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold; -fx-font-size: 13px;");
            successLabel.setText("✅ Mot de passe réinitialisé ! Redirection...");

            // Redirection vers login après 2 secondes
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                Platform.runLater(this::goToLogin);
            }).start();
        } else {
            successLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px;");
            successLabel.setText("❌ Une erreur est survenue. Réessayez.");
        }
    }

    // ════════════════════════════════════════════════════════
    // Navigation
    // ════════════════════════════════════════════════════════
    @FXML
    public void goToLogin() {
        scheduler.shutdownNow();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 550);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - VitaPlus");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("[ForgotPasswordController] goToLogin: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────
    private void showStep(int step) {
        stepEmail.setVisible(step == 1);       stepEmail.setManaged(step == 1);
        stepCode.setVisible(step == 2);        stepCode.setManaged(step == 2);
        stepNewPassword.setVisible(step == 3); stepNewPassword.setManaged(step == 3);
    }

    private void clearAll() {
        errorEmail.setText("");
        infoEmail.setText("");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /** Masque l'email pour l'affichage : j***@gmail.com */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
