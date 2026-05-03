package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.RoleService;
import tn.esprit.services.AuthService;

import java.util.Date;
import java.util.List;

public class RegisterController {

    @FXML private TextField nom;
    @FXML private TextField prenom;
    @FXML private TextField email;
    @FXML private TextField telephone;
    @FXML private TextField specialite;
    @FXML private PasswordField motDePasse;
    @FXML private PasswordField confirmMotDePasse;
    @FXML private ComboBox<Role> role;

    // Labels d'erreur par champ
    @FXML private Label errorNom;
    @FXML private Label errorPrenom;
    @FXML private Label errorEmail;
    @FXML private Label errorPassword;
    @FXML private Label errorConfirm;
    @FXML private Label errorTelephone;
    @FXML private Label errorRole;
    @FXML private Label errorGeneral;
    @FXML private Label infoRole;  // message info selon le rôle choisi

    private RoleService roleService = new RoleService();
    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        List<Role> roles = roleService.afficher();
        role.getItems().clear();
        role.getItems().addAll(roles);
        role.setPromptText("Choisir un rôle");

        // Afficher un message info quand le rôle change
        role.setOnAction(e -> {
            Role selected = role.getValue();
            if (selected == null) return;
            // idRole 3 = MEDECIN
            if (selected.getIdRole() == 3) {
                infoRole.setText("ℹ️ Les comptes médecin nécessitent une validation par l'administrateur avant de pouvoir se connecter.");
                infoRole.setStyle("-fx-text-fill: #EAB308; -fx-font-size: 12px;");
                if (specialite != null) specialite.setVisible(true);
            } else {
                infoRole.setText("");
                if (specialite != null) specialite.setVisible(selected.getIdRole() == 3);
            }
        });
    }

    @FXML
    public void register() {
        clearErrors();
        boolean valid = true;

        // --- Nom ---
        if (nom.getText().trim().isEmpty()) {
            errorNom.setText("⚠ Le nom est obligatoire.");
            valid = false;
        } else if (nom.getText().trim().length() < 2) {
            errorNom.setText("⚠ Le nom doit contenir au moins 2 caractères.");
            valid = false;
        } else if (!nom.getText().trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            errorNom.setText("⚠ Le nom ne doit contenir que des lettres.");
            valid = false;
        }

        // --- Prénom ---
        if (prenom.getText().trim().isEmpty()) {
            errorPrenom.setText("⚠ Le prénom est obligatoire.");
            valid = false;
        } else if (prenom.getText().trim().length() < 2) {
            errorPrenom.setText("⚠ Le prénom doit contenir au moins 2 caractères.");
            valid = false;
        } else if (!prenom.getText().trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            errorPrenom.setText("⚠ Le prénom ne doit contenir que des lettres.");
            valid = false;
        }

        // --- Email ---
        if (email.getText().trim().isEmpty()) {
            errorEmail.setText("⚠ L'email est obligatoire.");
            valid = false;
        } else if (!isValidEmail(email.getText().trim())) {
            errorEmail.setText("⚠ Format invalide (ex: nom@domaine.com).");
            valid = false;
        }

        // --- Mot de passe ---
        if (motDePasse.getText().isEmpty()) {
            errorPassword.setText("⚠ Le mot de passe est obligatoire.");
            valid = false;
        } else if (motDePasse.getText().length() < 8) {
            errorPassword.setText("⚠ Minimum 8 caractères requis.");
            valid = false;
        } else if (!motDePasse.getText().matches(".*[A-Z].*")) {
            errorPassword.setText("⚠ Doit contenir au moins une majuscule.");
            valid = false;
        } else if (!motDePasse.getText().matches(".*[0-9].*")) {
            errorPassword.setText("⚠ Doit contenir au moins un chiffre.");
            valid = false;
        }

        // --- Confirmation mot de passe ---
        if (confirmMotDePasse.getText().isEmpty()) {
            errorConfirm.setText("⚠ Veuillez confirmer le mot de passe.");
            valid = false;
        } else if (!confirmMotDePasse.getText().equals(motDePasse.getText())) {
            errorConfirm.setText("⚠ Les mots de passe ne correspondent pas.");
            valid = false;
        }

        // --- Téléphone (optionnel mais validé si rempli) ---
        if (!telephone.getText().trim().isEmpty()) {
            if (!telephone.getText().trim().matches("[0-9+\\s\\-]{8,15}")) {
                errorTelephone.setText("⚠ Numéro invalide (8 à 15 chiffres).");
                valid = false;
            }
        }

        // --- Rôle ---
        if (role.getValue() == null) {
            errorRole.setText("⚠ Veuillez choisir un rôle.");
            valid = false;
        }

        if (!valid) return;

        // isActive automatique selon le rôle:
        // Médecin (idRole=3) → false (en attente de validation admin)
        // Tous les autres → true (accès immédiat)
        int selectedRole = role.getValue().getIdRole();
        boolean active = (selectedRole != 3);

        Utilisateur u = new Utilisateur(
                nom.getText().trim(),
                prenom.getText().trim(),
                email.getText().trim(),
                motDePasse.getText(),
                telephone.getText().trim(),
                selectedRole,
                specialite.getText().trim(),
                new Date(),
                active
        );

        if (authService.register(u)) {
            // Success Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Inscription réussie ! Vous pouvez maintenant vous connecter.");
            alert.showAndWait();

            // Redirection vers login après inscription
            goToLogin();
        } else {
            // Error Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Échec de l'inscription");
            alert.setContentText("Une erreur est survenue lors de l'enregistrement. Veuillez vérifier les logs console ou contacter un administrateur.");
            alert.show();
        }
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nom.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 550));
            stage.setTitle("Login - VitaPlus");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearErrors() {
        errorNom.setText("");
        errorPrenom.setText("");
        errorEmail.setText("");
        errorPassword.setText("");
        errorConfirm.setText("");
        errorTelephone.setText("");
        errorRole.setText("");
        errorGeneral.setText("");
        if (infoRole != null) infoRole.setText("");
    }

    private boolean isValidEmail(String mail) {
        return mail.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }
}
