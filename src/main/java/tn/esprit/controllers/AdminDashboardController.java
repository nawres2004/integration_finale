package tn.esprit.controllers;

import javafx.event.Event;
import javafx.scene.Node;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.List;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.AuthService;
import tn.esprit.services.RoleService;
import tn.esprit.services.SessionService;
import tn.esprit.services.UtilisateurService;

public class AdminDashboardController {

    // ── Profil ──────────────────────────────────────────────
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label roleLabel;
    @FXML private Label topUserLabel;

    // ── Tableau utilisateurs (Remplacé par Cards) ──────────
    @FXML private StackPane contentPane;
    @FXML private Label tableStatusLabel;
    @FXML private TextField searchField;
    @FXML private ScrollPane mainContent;
    @FXML private VBox usersSection;
    @FXML private FlowPane cardsContainer;

    private ObservableList<Utilisateur> masterList = FXCollections.observableArrayList();
    private FilteredList<Utilisateur>  filteredList;
    private Utilisateur loggedAdmin;
    private Node dashboardNode; // Sauvegarder la vue du dashboard

    private final AuthService         authService         = new AuthService();
    private final RoleService         roleService         = new RoleService();
    private final UtilisateurService  utilisateurService  = new UtilisateurService();

    // ════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        loggedAdmin = SessionService.getInstance().getCurrentUser();
        
        // Sauvegarder la vue initiale (le dashboard)
        if (contentPane != null && !contentPane.getChildren().isEmpty()) {
            dashboardNode = contentPane.getChildren().get(0);
        }
        sharedContentPane = contentPane;
        
        loadUserProfile();
        setupTable();
        loadAllUtilisateurs();
        setupSearch();
        tn.esprit.utils.SessionBridge.sync();
    }

    // ── Profil admin ────────────────────────────────────────
    private void loadUserProfile() {
        Utilisateur u = SessionService.getInstance().getCurrentUser();
        if (u == null) return;
        String full = s(u.getPrenom()) + " " + s(u.getNom());
        if (topUserLabel != null) topUserLabel.setText("👤 " + full.trim());
        nomLabel.setText(s(u.getNom()));
        prenomLabel.setText(s(u.getPrenom()));
        emailLabel.setText(s(u.getEmail()));
        telephoneLabel.setText(s(u.getTelephone()));
        String rn = roleService.getRoleNameById(u.getIdRole());
        roleLabel.setText(rn != null ? rn : "Admin");
    }

    private String s(String v) { return v != null ? v : ""; }

    // ── Recherche avec FilteredList ─────────────────────────
    private void setupSearch() {
        if (searchField == null) return;
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredList.setPredicate(u -> {
                if (val == null || val.isBlank()) return true;
                String q = val.toLowerCase();
                return s(u.getNom()).toLowerCase().contains(q)
                    || s(u.getPrenom()).toLowerCase().contains(q)
                    || s(u.getEmail()).toLowerCase().contains(q);
            });
            displayCards(filteredList);
        });
    }

    private void setupTable() {
        // La table est remplacée par le système de cards
    }

    private void loadAllUtilisateurs() {
        masterList.setAll(utilisateurService.afficher());
        filteredList = new FilteredList<>(masterList, u -> true);
        displayCards(filteredList);
    }

    // ── Système de Cards ────────────────────────────────────
    private void displayCards(List<Utilisateur> users) {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();
        for (Utilisateur u : users) {
            cardsContainer.getChildren().add(createCard(u));
        }
        updateStatusLabel(users.size());
    }

    private VBox createCard(Utilisateur u) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 5); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 15; -fx-border-width: 1;");

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(14, 165, 233, 0.1), 15, 0, 0, 8); "
                + "-fx-border-color: #0ea5e9; -fx-border-radius: 15; -fx-border-width: 1.5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 5); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 15; -fx-border-width: 1;"));

        Label nameLabel = new Label(u.getPrenom() + " " + u.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label emailLabel = new Label("✉ " + u.getEmail());
        emailLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");

        HBox badges = new HBox(8);
        Label roleBadge = new Label();
        switch (u.getIdRole()) {
            case 1: roleBadge.setText("👤 Patient"); roleBadge.setStyle("-fx-background-color: #f0f9ff; -fx-text-fill: #0ea5e9;"); break;
            case 2: roleBadge.setText("💰 Donateur"); roleBadge.setStyle("-fx-background-color: #f5f3ff; -fx-text-fill: #7c3aed;"); break;
            case 3: roleBadge.setText("🩺 Médecin"); roleBadge.setStyle("-fx-background-color: #ecfeff; -fx-text-fill: #0891b2;"); break;
            case 4: roleBadge.setText("🔑 Admin"); roleBadge.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626;"); break;
            default: roleBadge.setText("Rôle " + u.getIdRole()); break;
        }
        roleBadge.setStyle(roleBadge.getStyle() + "-fx-font-size: 10; -fx-padding: 4 8; -fx-background-radius: 10; -fx-font-weight: bold;");

        Label statusBadge = new Label(u.isActive() ? "● Actif" : "○ Inactif");
        statusBadge.setStyle(u.isActive() 
            ? "-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-font-size: 10; -fx-padding: 4 8; -fx-background-radius: 10; -fx-font-weight: bold;"
            : "-fx-background-color: #fff1f2; -fx-text-fill: #e11d48; -fx-font-size: 10; -fx-padding: 4 8; -fx-background-radius: 10; -fx-font-weight: bold;");

        badges.getChildren().addAll(roleBadge, statusBadge);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button toggleBtn = new Button(u.isActive() ? "Désactiver" : "Activer");
        toggleBtn.setStyle("-fx-background-color: " + (u.isActive() ? "#ef4444" : "#10b981") + "; -fx-text-fill: white; "
                + "-fx-font-size: 10; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 10;");
        toggleBtn.setOnAction(e -> handleToggleStatus(u));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-size: 10; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 10;");
        deleteBtn.setOnAction(e -> handleDeleteUser(u));

        actions.getChildren().addAll(toggleBtn, deleteBtn);

        card.getChildren().addAll(nameLabel, emailLabel, badges, spacer, actions);
        return card;
    }

    private void handleToggleStatus(Utilisateur u) {
        boolean newState = !u.isActive();
        if (confirm("Changer le statut de " + u.getNom() + " ?")) {
            if (utilisateurService.setActiveStatus(u.getIdUtilisateur(), newState)) {
                u.setActive(newState);
                displayCards(filteredList);
            }
        }
    }

    private void handleDeleteUser(Utilisateur u) {
        if (u.getIdUtilisateur() == loggedAdmin.getIdUtilisateur()) {
            showAlert("Erreur", "Vous ne pouvez pas supprimer votre propre compte.", Alert.AlertType.ERROR);
            return;
        }
        if (confirm("Supprimer l'utilisateur " + u.getNom() + " ?")) {
            utilisateurService.supprimer(u.getIdUtilisateur());
            masterList.remove(u);
            displayCards(filteredList);
        }
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.OK;
    }

    private void updateStatusLabel(int count) {
        if (tableStatusLabel != null) {
            tableStatusLabel.setText(count + " utilisateur(s) affiché(s)");
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showStatus(String msg, boolean success) {
        if (tableStatusLabel == null) return;
        tableStatusLabel.setText(msg);
        tableStatusLabel.setStyle(success
            ? "-fx-text-fill:#22C55E;-fx-font-size:13px;-fx-font-weight:bold;"
            : "-fx-text-fill:#EF4444;-fx-font-size:13px;-fx-font-weight:bold;");
    }

    // ── Rafraîchir ──────────────────────────────────────────
    @FXML
    public void refreshTable() {
        loadAllUtilisateurs();
        showStatus("🔄 Liste mise à jour.", true);
    }

    // ── Profil modal ────────────────────────────────────────
    @FXML
    public void openChangePassword(Event event) {
        tn.esprit.utils.ProfileHelper.openChangePassword(((Node)event.getSource()).getScene().getWindow());
    }

    @FXML
    public void openProfile(Event event) {
        tn.esprit.utils.ProfileHelper.open(((Node)event.getSource()).getScene().getWindow());
        loadUserProfile();
    }

    // ── Logout ──────────────────────────────────────────────
    @FXML
    public void logout(Event event) {
        SessionService.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(loader.load(), 900, 550);
            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - VitaPlus");
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openOrdonnances() {
        loadView("/AfficherOrdonnance.fxml");
    }

    @FXML
    public void openMedicaments() {
        loadView("/AfficherMedicament.fxml");
    }

    @FXML
    public void openDonsProjets() {
        loadView("/backoffice/AdminDashboard.fxml");
    }

    // ── Accès statique au contentPane pour les sous-vues ───
    private static StackPane sharedContentPane;

    public static void chargerDansContentPane(String fxmlPath) {
        if (sharedContentPane == null) return;
        try {
            javafx.scene.Node node = (javafx.scene.Node)
                new javafx.fxml.FXMLLoader(
                    AdminDashboardController.class.getResource(fxmlPath)).load();
            sharedContentPane.getChildren().clear();
            sharedContentPane.getChildren().add(node);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openBlogEtInformations() {
        try {
            tn.esprit.utils.SessionBridge.sync(); // Sync session first
            org.example.App.integrationPane = contentPane;
            contentPane.getChildren().setAll(
                (javafx.scene.Node) new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/article_list.fxml")).load()
            );
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (contentPane != null) {
                contentPane.getChildren().setAll(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Navigation Interne ──────────────────────────────────
    @FXML
    public void showDashboard() {
        if (contentPane != null && dashboardNode != null) {
            contentPane.getChildren().setAll(dashboardNode);
        }
    }

    @FXML
    public void scrollToUsers() {
        showDashboard(); // Retour au dashboard si on était ailleurs
        
        if (mainContent != null && usersSection != null) {
            // Force layout pass to ensure bounds are up to date
            mainContent.layout();
            
            // Calculer la position relative de la section par rapport au contenu total
            Node content = mainContent.getContent();
            if (content instanceof VBox) {
                VBox container = (VBox) content;
                double targetY = usersSection.getBoundsInParent().getMinY();
                double totalHeight = container.getHeight();
                double viewportHeight = mainContent.getViewportBounds().getHeight();
                
                // La valeur VValue est entre 0.0 et 1.0
                if (totalHeight > viewportHeight) {
                    double vValue = targetY / (totalHeight - viewportHeight);
                    mainContent.setVvalue(Math.min(1.0, Math.max(0.0, vValue)));
                }
            }
        }
    }
}
