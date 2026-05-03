package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.AuthService;
import tn.esprit.services.BehaviorCaptchaService;
import tn.esprit.services.SessionService;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    @FXML private TextField     email;
    @FXML private PasswordField motDePasse;
    @FXML private Label         errorEmail;
    @FXML private Label         errorPassword;
    @FXML private Label         errorGeneral;

    // CAPTCHA mathématique
    @FXML private Label     captchaQuestion;
    @FXML private TextField captchaAnswer;
    @FXML private Label     errorCaptcha;

    // Score IA comportemental
    @FXML private Label behaviorScoreLabel;

    private final AuthService           service         = new AuthService();
    private final BehaviorCaptchaService behaviorService = new BehaviorCaptchaService();
    private final SecureRandom          random          = new SecureRandom();
    private int captchaResult;

    // Timestamps de frappe pour l'analyse comportementale
    private final List<Long> emailTimestamps    = new ArrayList<>();
    private final List<Long> passwordTimestamps = new ArrayList<>();

    @FXML
    public void initialize() {
        generateCaptcha();
        setupBehaviorTracking();
    }

    // ── Tracking comportemental ──────────────────────────────
    private void setupBehaviorTracking() {
        // Enregistrer chaque frappe dans email
        email.setOnKeyTyped(e -> emailTimestamps.add(System.currentTimeMillis()));

        // Enregistrer chaque frappe dans mot de passe
        motDePasse.setOnKeyTyped(e -> passwordTimestamps.add(System.currentTimeMillis()));
    }

    // ── Générer un nouveau CAPTCHA ───────────────────────────
    private void generateCaptcha() {
        int a = random.nextInt(10) + 1;  // 1-10
        int b = random.nextInt(10) + 1;  // 1-10
        int op = random.nextInt(3);      // 0=+, 1=-, 2=×

        String question;
        switch (op) {
            case 0:
                captchaResult = a + b;
                question = a + " + " + b;
                break;
            case 1:
                // S'assurer que le résultat est positif
                if (a < b) { int tmp = a; a = b; b = tmp; }
                captchaResult = a - b;
                question = a + " - " + b;
                break;
            default:
                captchaResult = a * b;
                question = a + " × " + b;
                break;
        }

        captchaQuestion.setText(question);
        if (captchaAnswer != null) captchaAnswer.clear();
        if (errorCaptcha != null) errorCaptcha.setText("");
    }

    @FXML
    public void refreshCaptcha() {
        generateCaptcha();
    }

    // ── Login ────────────────────────────────────────────────
    @FXML
    public void login() {
        clearErrors();
        boolean valid = true;

        String mail = email.getText().trim();
        String pass = motDePasse.getText();

        // Validation email
        if (mail.isEmpty()) {
            errorEmail.setText("⚠ L'email est obligatoire.");
            valid = false;
        } else if (!isValidEmail(mail)) {
            errorEmail.setText("⚠ Format d'email invalide.");
            valid = false;
        }

        // Validation mot de passe
        if (pass.isEmpty()) {
            errorPassword.setText("⚠ Le mot de passe est obligatoire.");
            valid = false;
        } else if (pass.length() < 8) {
            errorPassword.setText("⚠ Minimum 8 caractères.");
            valid = false;
        }

        // Validation CAPTCHA
        String answerText = captchaAnswer.getText().trim();
        if (answerText.isEmpty()) {
            errorCaptcha.setText("⚠ Répondez à la question de sécurité.");
            valid = false;
        } else {
            try {
                int userAnswer = Integer.parseInt(answerText);
                if (userAnswer != captchaResult) {
                    errorCaptcha.setText("⚠ Réponse incorrecte. Réessayez.");
                    generateCaptcha(); // Nouveau CAPTCHA après échec
                    valid = false;
                }
            } catch (NumberFormatException e) {
                errorCaptcha.setText("⚠ Entrez un nombre entier.");
                valid = false;
            }
        }

        if (!valid) return;

        // ── Analyse IA comportementale ───────────────────────
        List<Long> allTimestamps = new ArrayList<>(emailTimestamps);
        allTimestamps.addAll(passwordTimestamps);
        BehaviorCaptchaService.AnalysisResult behavior = behaviorService.analyze(allTimestamps);

        // ════════════════ LOGIN ANALYSIS ════════════════
        System.out.println("================================================");
        System.out.println("           LOGIN ANALYSIS - VitaPlus            ");
        System.out.println("================================================");
        System.out.printf("  Email            : %s%n", mail);
        System.out.printf("  Keystrokes       : %d%n", allTimestamps.size());
        System.out.printf("  Human Score      : %.2f / 1.00%n", behavior.score);
        System.out.printf("  Is Human         : %s%n", behavior.isHuman ? "true ✅" : "false ❌");
        System.out.printf("  Result           : %s%n", behavior.getScoreLabel());
        System.out.printf("  Reason           : %s%n", behavior.reason);
        System.out.println("================================================");

        if (behaviorScoreLabel != null) {
            behaviorScoreLabel.setText(behavior.getScoreLabel());
            behaviorScoreLabel.setStyle(behavior.isHuman
                ? "-fx-text-fill: #22C55E; -fx-font-size: 11px;"
                : "-fx-text-fill: #EF4444; -fx-font-size: 11px;");
        }

        if (!behavior.isHuman) {
            errorGeneral.setText("🤖 Comportement automatisé détecté. Veuillez réessayer.");
            generateCaptcha();
            emailTimestamps.clear();
            passwordTimestamps.clear();
            return;
        }

        Utilisateur u = service.login(mail, pass);

        if (u != null) {
            // Médecin non validé
            if (u.getIdRole() == 3 && !u.isActive()) {
                errorGeneral.setText("⏳ Votre compte médecin est en attente de validation.");
                generateCaptcha();
                return;
            }
            // Compte désactivé (non-admin)
            if (u.getIdRole() != 4 && !u.isActive()) {
                errorGeneral.setText("🚫 Votre compte est désactivé. Contactez l'administrateur.");
                generateCaptcha();
                return;
            }

            SessionService.getInstance().setCurrentUser(u);
            try {
                Stage stage = (Stage) email.getScene().getWindow();
                FXMLLoader loader;
                String title;

                switch (u.getIdRole()) {
                    case 4:
                        loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
                        title = "Admin Dashboard - VitaPlus";
                        break;
                    case 3:
                        loader = new FXMLLoader(getClass().getResource("/medecin_dashboard.fxml"));
                        title = "Médecin Dashboard - VitaPlus";
                        break;
                    case 2:
                        loader = new FXMLLoader(getClass().getResource("/frontoffice/FrontProjets.fxml"));
                        title = "Espace Donateur - VitaPlus";
                        break;
                    case 1:
                    default:
                        loader = new FXMLLoader(getClass().getResource("/client_dashboard.fxml"));
                        title = "Patient Dashboard - VitaPlus";
                        break;
                }

                Scene scene = new Scene(loader.load(), 1200, 750);
                stage.setScene(scene);
                stage.setTitle(title);
                stage.setResizable(true);
                stage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
                errorGeneral.setText("❌ Erreur de chargement du dashboard.");
            }
        } else {
            errorGeneral.setText("❌ Email ou mot de passe incorrect.");
            generateCaptcha();
            emailTimestamps.clear();
            passwordTimestamps.clear();
        }
    }

    @FXML
    public void goToForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forgot_password.fxml"));
            Scene scene = new Scene(loader.load(), 900, 560);
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mot de passe oublié - VitaPlus");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 680);
            Stage stage = (Stage) email.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription - VitaPlus");
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearErrors() {
        errorEmail.setText("");
        errorPassword.setText("");
        errorGeneral.setText("");
        errorCaptcha.setText("");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }
}
