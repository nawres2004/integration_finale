package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.Ordonnance;
import tn.esprit.services.EmailService;
import tn.esprit.services.PdfService;
import tn.esprit.services.QrCodeService;
import tn.esprit.services.ServiceOrdonnance;
import tn.esprit.services.UtilisateurService;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AfficherOrdonnanceController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private Button btnNewOrdonnance;

    private ServiceOrdonnance so = new ServiceOrdonnance();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private PdfService pdfService = new PdfService();
    private QrCodeService qrCodeService = new QrCodeService();
    private EmailService emailService = new EmailService();
    private List<Ordonnance> allOrdonnances = new ArrayList<>();
    private boolean sortAscending = true;

    @FXML
    public void initialize() {
        // Sort combo setup
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList("Le plus récent", "Le plus ancien"));
            sortCombo.setValue("Le plus récent");
            sortCombo.setOnAction(e -> {
                sortAscending = "Le plus ancien".equals(sortCombo.getValue());
                filterAndDisplay();
            });
        }

        tn.esprit.models.Utilisateur logged = tn.esprit.services.SessionService.getInstance().getCurrentUser();
        if (logged == null || logged.getIdRole() != 3) {
            if (btnNewOrdonnance != null) {
                btnNewOrdonnance.setVisible(false);
                btnNewOrdonnance.setManaged(false);
            }
        }

        loadOrdonnances();
    }

    private void loadOrdonnances() {
        try {
            tn.esprit.models.Utilisateur logged = tn.esprit.services.SessionService.getInstance().getCurrentUser();
            if (logged != null && logged.getIdRole() == 1) {
                // Si c'est un patient, on ne charge que SES ordonnances
                allOrdonnances = so.findByPatient(logged.getIdUtilisateur());
            } else {
                // Sinon (Admin/Médecin), on charge tout
                allOrdonnances = so.findALL();
            }
            filterAndDisplay();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les ordonnances : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filterAndDisplay() {
        List<Ordonnance> filtered = allOrdonnances;

        // Sort
        Comparator<Ordonnance> comp = Comparator.comparing(
                Ordonnance::getDateOrdonnance,
                Comparator.nullsLast(Comparator.naturalOrder()));
        if (!sortAscending) {
            comp = comp.reversed();
        }
        filtered = filtered.stream().sorted(comp).collect(Collectors.toList());

        displayCards(filtered);
    }

    private void displayCards(List<Ordonnance> ordonnances) {
        cardsContainer.getChildren().clear();
        for (Ordonnance o : ordonnances) {
            cardsContainer.getChildren().add(createCard(o));
        }
    }

    private VBox createCard(Ordonnance o) {
        VBox card = new VBox(15);
        card.setPrefWidth(320);
        card.setPadding(new Insets(25, 20, 20, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 8); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1; "
                + "-fx-border-color: #0ea5e9 transparent transparent transparent; -fx-border-width: 4 0 0 0;");

        // Hover Effect Premium
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(14, 165, 233, 0.15), 20, 0, 0, 10); "
                + "-fx-border-color: #0EA5E9; -fx-border-radius: 20; -fx-border-width: 1.5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 15, 0, 0, 8); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 20; -fx-border-width: 1;"));

        // — Title (patient name) —
        String patientName = o.getNomUtilisateur() != null ? o.getNomUtilisateur() : "Patient inconnu";
        Label title = new Label(patientName);
        title.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        // — Description (instructions, truncated) —
        String instrText = o.getInstructions() != null ? o.getInstructions() : "";
        if (instrText.length() > 100) {
            instrText = instrText.substring(0, 100) + "...";
        }
        Label description = new Label(instrText);
        description.setStyle("-fx-font-size: 13; -fx-text-fill: #64748b;");
        description.setWrapText(true);
        description.setMaxHeight(50);

        // — Date —
        String dateStr = "N/A";
        if (o.getDateOrdonnance() != null) {
            dateStr = o.getDateOrdonnance().format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.FRENCH));
        }
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

        // — Info badges (duration + seen) —
        HBox infoRow = new HBox(12);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label durLabel = new Label("⏱ " + (o.getDureeTraitement() != null ? o.getDureeTraitement() : "N/A"));
        durLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #0ea5e9; -fx-padding: 6 12; "
                + "-fx-background-color: #f0f9ff; -fx-background-radius: 20; -fx-font-weight: bold;");

        Label seenLabel = new Label(o.isSeen() ? "✔ Vu" : "⌛ En attente");
        seenLabel.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-radius: 20; -fx-font-weight: bold; "
                + (o.isSeen()
                        ? "-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a;"
                        : "-fx-background-color: #fffbeb; -fx-text-fill: #d97706;"));

        infoRow.getChildren().addAll(durLabel, seenLabel);

        // — Spacer —
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // — Action buttons —
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button deleteBtn = createActionButton("🗑 ", "#ef4444");
        Button pdfBtn = createActionButton("📄 ", "#10b981");
        Button qrBtn = createActionButton("📱 ", "#f59e0b");
        Button editBtn = createActionButton("📝", "#0EA5E9");
        Button emailBtn = createActionButton("📧 ", "#0284c7");

        tn.esprit.models.Utilisateur logged = tn.esprit.services.SessionService.getInstance().getCurrentUser();
        if (logged == null || logged.getIdRole() != 3) {
            editBtn.setVisible(false); editBtn.setManaged(false);
            deleteBtn.setVisible(false); deleteBtn.setManaged(false);
            emailBtn.setVisible(false); emailBtn.setManaged(false); // Cacher mail pour les patients
        }

        editBtn.setOnAction(e -> handleEdit(o));
        deleteBtn.setOnAction(e -> handleDelete(o));
        pdfBtn.setOnAction(e -> handleExportPdf(o));
        qrBtn.setOnAction(e -> handleShowQr(o));
        emailBtn.setOnAction(e -> handleSendEmail(o));

        actions.getChildren().addAll(deleteBtn, pdfBtn, qrBtn, editBtn, emailBtn);

        card.getChildren().addAll(title, description, dateLabel, infoRow, spacer, actions);
        return card;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);

        // Style de base moderne
        String baseStyle = "-fx-background-color: " + color + "; "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 11px; "
                + "-fx-cursor: hand; "
                + "-fx-background-radius: 10; "
                + "-fx-padding: 8 14; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 3);";

        btn.setStyle(baseStyle);

        // Animation de zoom au survol
        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle + "-fx-background-color: derive(" + color + ", 15%);");
            btn.setScaleX(1.08);
            btn.setScaleY(1.08);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });

        return btn;
    }

    // ==================== HANDLERS ====================

    private void handleEdit(Ordonnance o) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierOrdonnance.fxml"));
            Parent root = loader.load();
            ModifierOrdonnanceController controller = loader.getController();
            controller.setData(o);

            StackPane contentPane = (StackPane) cardsContainer.getScene().lookup("#contentPane");
            if (contentPane != null)
                contentPane.getChildren().setAll(root);
            else
                cardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Ordonnance o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(
                "Supprimer l'ordonnance de " + (o.getNomUtilisateur() != null ? o.getNomUtilisateur() : "") + " ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                so.deleteOne(o.getId());
                loadOrdonnances();
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur suppression: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleExportPdf(Ordonnance o) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'ordonnance en PDF");
        fileChooser.setInitialFileName("Ordonnance_" + o.getNomUtilisateur() + "_" + o.getDateOrdonnance() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(cardsContainer.getScene().getWindow());
        if (file != null) {
            try {
                pdfService.generateOrdonnancePdf(o, file.getAbsolutePath());
                showAlert("Succès", "L'ordonnance a été exportée avec succès.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void handleSendEmail(Ordonnance o) {
        try {
            String mail = utilisateurService.getMailById(o.getIdUtilisateur());

            if (mail == null || mail.isBlank()) {
                showAlert("Erreur", "Aucun email trouvé pour ce patient.", Alert.AlertType.WARNING);
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation d'envoi");
            confirm.setHeaderText("Envoyer l'ordonnance à : " + mail + " ?");

            if (confirm.showAndWait().get() == ButtonType.OK) {
                // On utilise un Thread pour ne pas bloquer l'interface pendant l'envoi
                new Thread(() -> {
                    try {
                        emailService.envoyerOrdonnance(mail, o);
                        javafx.application.Platform.runLater(() -> showAlert("Succès",
                                "L'ordonnance a été envoyée par email !", Alert.AlertType.INFORMATION));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> showAlert("Erreur",
                                "Échec de l'envoi : " + ex.getMessage(), Alert.AlertType.ERROR));
                    }
                }).start();
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de récupérer l'email du patient.", Alert.AlertType.ERROR);
        }
    }

    private void handleShowQr(Ordonnance o) {

        try {
            String qrData = String.format("Patient: %s\nDate: %s\nInstructions: %s",
                    o.getNomUtilisateur() != null ? o.getNomUtilisateur() : "N/A",
                    o.getDateOrdonnance(),
                    o.getInstructions());

            Image qrImage = qrCodeService.generateQrCodeImage(qrData, 300, 300);

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("QR Code - " + o.getNomUtilisateur());

            ImageView imageView = new ImageView(qrImage);
            VBox root = new VBox(20, new Label("Scanner pour voir les détails"), imageView);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: white;");

            Scene scene = new Scene(root);
            popup.setScene(scene);
            popup.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de générer le QR code : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ==================== FXML ACTIONS ====================

    @FXML
    void trierParDate(ActionEvent event) {
        sortAscending = !sortAscending;
        filterAndDisplay();
    }

    @FXML
    void naviguerVersAjout(ActionEvent event) {
        loadView("/AjouterOrdonnance.fxml");
    }

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
    void showStatistiques(ActionEvent event) {
        try {
            Map<String, Integer> stats = so.countByMois();

            Stage stage = new Stage();
            stage.setTitle("Statistiques des Ordonnances par Mois");
            stage.initModality(Modality.APPLICATION_MODAL);

            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Mois");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Nombre d'ordonnances");

            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Évolution des Ordonnances");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ordonnances 2026");

            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            barChart.getData().add(series);

            VBox root = new VBox(barChart);
            root.setPadding(new Insets(15));
            Scene scene = new Scene(root, 800, 600);

            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les statistiques : " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
