package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.App;
import org.example.entities.User;
import org.example.services.ServiceUser;
import org.example.utils.UserSession;

import java.io.IOException;

public class SignInController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         emailErrorLabel;
    @FXML private Label         passwordErrorLabel;

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML
    void handleLogin(ActionEvent event) throws IOException {
        // Reset errors
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        boolean isValid = true;

        if (email.isEmpty()) {
            showError(emailErrorLabel, "Email is required.");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError(emailErrorLabel, "Invalid email format.");
            isValid = false;
        }

        if (password.isEmpty()) {
            showError(passwordErrorLabel, "Password is required.");
            isValid = false;
        }

        if (!isValid) return;

        User user = serviceUser.authenticate(email, password);
        if (user != null) {
            UserSession.getInstance(user);
            App.setRoot("primary");
        } else {
            showError(emailErrorLabel, "❌  Invalid email or password.");
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }
}
