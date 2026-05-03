package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Medicament;
import tn.esprit.services.ServiceMedicament;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AfficherMedicamentController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnNewMedicament;

    private ServiceMedicament sm = new ServiceMedicament();
    private List<Medicament> allMedicaments = new ArrayList<>();

    @FXML
    public void initialize() {
        // Search setup
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        }

        tn.esprit.models.Utilisateur logged = tn.esprit.services.SessionService.getInstance().getCurrentUser();
        if (logged == null || logged.getIdRole() != 4) {
            if (btnNewMedicament != null) {
                btnNewMedicament.setVisible(false);
                btnNewMedicament.setManaged(false);
            }
        }

        loadMedicaments();
    }

    private void loadMedicaments() {
        try {
            allMedicaments = sm.findALL();
            filterAndDisplay();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les médicaments : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filterAndDisplay() {
        String searchText = txtSearch != null ? txtSearch.getText() : "";
        List<Medicament> filtered = allMedicaments;

        if (searchText != null && !searchText.isEmpty()) {
            String lower = searchText.toLowerCase();
            filtered = allMedicaments.stream()
                    .filter(m -> (m.getNom() != null && m.getNom().toLowerCase().contains(lower))
                            || (m.getDosage() != null && m.getDosage().toLowerCase().contains(lower))
                            || (m.getForme() != null && m.getForme().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }

        // Display
        displayCards(filtered);
    }

    private void displayCards(List<Medicament> medicaments) {
        cardsContainer.getChildren().clear();
        for (Medicament m : medicaments) {
            cardsContainer.getChildren().add(createCard(m));
        }
    }

    private VBox createCard(Medicament m) {
        VBox card = new VBox(15);
        card.setPrefWidth(320);
        card.setPadding(new Insets(25, 20, 20, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 8); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; "
                + "-fx-border-color: #8b5cf6 transparent transparent transparent; -fx-border-width: 4 0 0 0;");

        // Hover effect for consistency
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(139, 92, 246, 0.2), 20, 0, 0, 10); "
                + "-fx-border-color: #8b5cf6 transparent transparent transparent; -fx-border-width: 4 0 0 0;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 8); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; "
                + "-fx-border-color: #8b5cf6 transparent transparent transparent; -fx-border-width: 4 0 0 0;"));

        // — Title (medication name) —
        String name = m.getNom() != null ? m.getNom() : "Médicament inconnu";
        Label title = new Label(name);
        title.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        // — Description (contre-indications or dosage info) —
        String descText = "";
        if (m.getContreIndications() != null && !m.getContreIndications().isEmpty()) {
            descText = m.getContreIndications();
        } else {
            descText = "Dosage : " + (m.getDosage() != null ? m.getDosage() : "N/A")
                    + " | Forme : " + (m.getForme() != null ? m.getForme() : "N/A");
        }
        if (descText.length() > 100) {
            descText = descText.substring(0, 100) + "...";
        }
        Label description = new Label(descText);
        description.setStyle("-fx-font-size: 13; -fx-text-fill: #64748b;");
        description.setWrapText(true);
        description.setMaxHeight(50);

        // — Expiration date —
        String dateStr = "N/A";
        if (m.getDateExpiration() != null) {
            dateStr = m.getDateExpiration().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.FRENCH));
        }
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

        // — Info badges (dosage, forme, frequence) —
        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label dosageBadge = new Label("💊 " + (m.getDosage() != null ? m.getDosage() : "N/A"));
        dosageBadge.setStyle("-fx-font-size: 11; -fx-text-fill: #7c3aed; -fx-padding: 4 10; "
                + "-fx-background-color: #f3e8ff; -fx-background-radius: 10;");

        Label formeBadge = new Label("📦 " + (m.getForme() != null ? m.getForme() : "N/A"));
        formeBadge.setStyle("-fx-font-size: 11; -fx-text-fill: #0EA5E9; -fx-padding: 4 10; "
                + "-fx-background-color: #e0f2fe; -fx-background-radius: 10;");

        // Expiration warning
        boolean isExpired = m.getDateExpiration() != null && m.getDateExpiration().isBefore(LocalDate.now());
        Label expiryBadge = new Label(isExpired ? "⚠ Expiré" : "✓ Valide");
        expiryBadge.setStyle("-fx-font-size: 11; -fx-padding: 4 10; -fx-background-radius: 10; "
                + (isExpired
                        ? "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;"
                        : "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;"));

        infoRow.getChildren().addAll(dosageBadge, formeBadge, expiryBadge);

        // — Spacer —
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // — Action buttons —
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button deleteBtn = createActionButton("🗑 Delete", "#ef4444");
        Button editBtn = createActionButton("✏ Edit", "#A855F7");

        tn.esprit.models.Utilisateur logged = tn.esprit.services.SessionService.getInstance().getCurrentUser();
        if (logged == null || logged.getIdRole() != 4) {
            editBtn.setVisible(false); editBtn.setManaged(false);
            deleteBtn.setVisible(false); deleteBtn.setManaged(false);
        }

        editBtn.setOnAction(e -> handleEdit(m));
        deleteBtn.setOnAction(e -> handleDelete(m));

        actions.getChildren().addAll(deleteBtn, editBtn);

        card.getChildren().addAll(title, description, dateLabel, infoRow, spacer, actions);
        return card;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 7 16; "
                + "-fx-font-size: 12; -fx-font-weight: bold;");
        return btn;
    }

    // ==================== HANDLERS ====================

    private void handleEdit(Medicament m) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMedicament.fxml"));
            Parent root = loader.load();
            ModifierMedicamentController controller = loader.getController();
            controller.setData(m);

            StackPane contentPane = (StackPane) cardsContainer.getScene().lookup("#contentPane");
            if (contentPane != null)
                contentPane.getChildren().setAll(root);
            else
                cardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Medicament m) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + m.getNom() + " ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                sm.deleteOne(m.getId());
                loadMedicaments();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ==================== FXML ACTIONS ====================

    @FXML
    void backToDashboard(ActionEvent event) {
        tn.esprit.models.Utilisateur logged = tn.esprit.services.SessionService.getInstance().getCurrentUser();
        String path = "/dashboard.fxml";
        if (logged != null) {
            if (logged.getIdRole() == 4) path = "/admin_dashboard.fxml";
            else if (logged.getIdRole() == 3) path = "/medecin_dashboard.fxml";
            else if (logged.getIdRole() == 1) path = "/client_dashboard.fxml";
        }
        loadView(path);
    }

    @FXML
    void naviguerVersAjout(ActionEvent event) {
        loadView("/AjouterMedicament.fxml");
    }

    // ==================== UTILS ====================

    private void loadView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            StackPane contentPane = (StackPane) cardsContainer.getScene().lookup("#contentPane");
            if (contentPane != null)
                contentPane.getChildren().setAll(root);
            else
                cardsContainer.getScene().setRoot(root);
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
