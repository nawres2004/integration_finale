package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private VBox cardOrdonnances;
    @FXML
    private VBox cardMedicaments;

    @FXML
    public void initialize() {
        setupHoverEffect(cardOrdonnances, "#0EA5E9", "rgba(14, 165, 233, 0.2)");
        setupHoverEffect(cardMedicaments, "#A855F7", "rgba(168, 85, 247, 0.2)");
    }

    private void setupHoverEffect(VBox card, String color, String shadowColor) {
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 25px; "
                    + "-fx-effect: dropshadow(three-pass-box, " + shadowColor + ", 30, 0, 0, 15); "
                    + "-fx-border-color: " + color + "; -fx-border-radius: 25px; -fx-border-width: 2; "
                    + "-fx-cursor: hand;");
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 25px; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.08), 20, 0, 0, 10); "
                    + "-fx-border-color: transparent; -fx-cursor: hand;");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
    }

    @FXML
    void goToMedicaments(MouseEvent event) {
        loadView("/AfficherMedicament.fxml");
    }

    @FXML
    void goToOrdonnances(MouseEvent event) {
        loadView("/AfficherOrdonnance.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Find the contentPane from the current scene
            StackPane contentPane = (StackPane) cardOrdonnances.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
