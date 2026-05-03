package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.AuthService;
import tn.esprit.services.SessionService;
import tn.esprit.services.RoleService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController {

    // =========================
    // FXML FIELDS
    // =========================
    @FXML private ImageView profileImage;
    @FXML private Label userNameLabel;
    @FXML private Label roleBadge;
    @FXML private Label pageTitleLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label totalDonationsLabel;
    @FXML private Label activeSessionsLabel;
    @FXML private Pane barChartContainer;
    @FXML private Pane pieChartContainer;
    @FXML private TableView<ActivityRecord> activityTable;
    @FXML private TableColumn<ActivityRecord, String> dateColumn;
    @FXML private TableColumn<ActivityRecord, String> userColumn;
    @FXML private TableColumn<ActivityRecord, String> actionColumn;
    @FXML private TableColumn<ActivityRecord, String> statusColumn;
    @FXML private TableColumn<ActivityRecord, String> detailsColumn;
    @FXML private VBox menuContainer;

    // =========================
    // SERVICES
    // =========================
    private AuthService authService = new AuthService();
    private RoleService roleService = new RoleService();
    private BarChart<String, Number> barChart;
    private PieChart pieChart;

    // =========================
    // MENU DATA MODELS
    // =========================
    private static class MenuItem {
        String icon;
        String text;
        String action;
        
        MenuItem(String icon, String text, String action) {
            this.icon = icon;
            this.text = text;
            this.action = action;
        }
    }

    // =========================
    // ACTIVITY RECORD MODEL
    // =========================
    public static class ActivityRecord {
        private String date;
        private String user;
        private String action;
        private String status;
        private String details;

        public ActivityRecord(String date, String user, String action, String status, String details) {
            this.date = date;
            this.user = user;
            this.action = action;
            this.status = status;
            this.details = details;
        }

        // Getters
        public String getDate() { return date; }
        public String getUser() { return user; }
        public String getAction() { return action; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
    }

    // =========================
    // INITIALIZATION
    // =========================
    @FXML
    public void initialize() {
        System.out.println("=== DASHBOARD CONTROLLER INITIALISÉ ===");
        
        loadUserProfile();
        loadMenuByRole();
        loadStatistics();
        loadCharts();
        loadRecentActivity();
    }

    private void loadUserProfile() {
        Utilisateur currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Charger le profil utilisateur
            String fullName = (currentUser.getNom() != null ? currentUser.getNom() : "") + " " + 
                          (currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            userNameLabel.setText(fullName);
            
            // Charger le badge de rôle
            String roleName = roleService.getRoleNameById(currentUser.getIdRole());
            roleBadge.setText(roleName != null ? roleName : "Utilisateur");
            
            // Charger l'image de profil par défaut
            try {
                URL imageUrl = getClass().getResource("/images/default-profile.png");
                if (imageUrl != null) {
                    profileImage.setImage(new Image(imageUrl.toExternalForm()));
                }
            } catch (Exception e) {
                System.out.println("Impossible de charger l'image de profil: " + e.getMessage());
            }
        }
    }

    private void loadMenuByRole() {
        Utilisateur currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser == null) return;

        List<MenuItem> menuItems = getMenuItemsByRole(currentUser.getIdRole());
        createMenuItems(menuItems);
    }

    private List<MenuItem> getMenuItemsByRole(int roleId) {
        List<MenuItem> items = new ArrayList<>();
        
        switch (roleId) {
            case 4: // ADMIN
                items.add(new MenuItem("📊", "Tableau de Bord", "dashboard"));
                items.add(new MenuItem("👥", "Gérer les Utilisateurs", "manageUsers"));
                items.add(new MenuItem("👨‍⚕️", "Gérer les Médecins", "manageDoctors"));
                items.add(new MenuItem("🩸", "Gérer les Dons", "manageDonations"));
                items.add(new MenuItem("📈", "Statistiques", "statistics"));
                items.add(new MenuItem("⚙️", "Paramètres", "settings"));
                break;
                
            case 3: // MEDECIN
                items.add(new MenuItem("📊", "Tableau de Bord", "dashboard"));
                items.add(new MenuItem("👥", "Mes Patients", "myPatients"));
                items.add(new MenuItem("📅", "Rendez-vous", "appointments"));
                items.add(new MenuItem("📋", "Rapports", "reports"));
                items.add(new MenuItem("👤", "Profil", "profile"));
                items.add(new MenuItem("⚙️", "Paramètres", "settings"));
                break;
                
            case 1: // CLIENT
                items.add(new MenuItem("📊", "Tableau de Bord", "dashboard"));
                items.add(new MenuItem("📅", "Mes Rendez-vous", "myAppointments"));
                items.add(new MenuItem("🔍", "Chercher un Médecin", "searchDoctor"));
                items.add(new MenuItem("👤", "Profil", "profile"));
                items.add(new MenuItem("⚙️", "Paramètres", "settings"));
                break;
                
            case 2: // DONATEUR
                items.add(new MenuItem("📊", "Tableau de Bord", "dashboard"));
                items.add(new MenuItem("🩸", "Faire un Don", "makeDonation"));
                items.add(new MenuItem("📜", "Historique des Dons", "donationHistory"));
                items.add(new MenuItem("🎯", "Campagnes", "campaigns"));
                items.add(new MenuItem("👤", "Profil", "profile"));
                items.add(new MenuItem("⚙️", "Paramètres", "settings"));
                break;
                
            default:
                items.add(new MenuItem("📊", "Tableau de Bord", "dashboard"));
                items.add(new MenuItem("👤", "Profil", "profile"));
                items.add(new MenuItem("⚙️", "Paramètres", "settings"));
                break;
        }
        
        return items;
    }

    private void createMenuItems(List<MenuItem> menuItems) {
        menuContainer.getChildren().clear();
        
        for (MenuItem item : menuItems) {
            HBox menuItemBox = new HBox(10);
            menuItemBox.getStyleClass().add("menu-item");
            
            Label iconLabel = new Label(item.icon);
            iconLabel.getStyleClass().add("menu-icon");
            
            Label textLabel = new Label(item.text);
            textLabel.getStyleClass().add("menu-text");
            
            menuItemBox.getChildren().addAll(iconLabel, textLabel);
            
            // Ajouter l'événement de clic
            menuItemBox.setOnMouseClicked(event -> handleMenuAction(item.action));
            
            menuContainer.getChildren().add(menuItemBox);
            
            // Ajouter un séparateur sauf pour le dernier élément
            if (menuItems.indexOf(item) < menuItems.size() - 1) {
                Label separator = new Label();
                separator.getStyleClass().add("menu-separator");
                menuContainer.getChildren().add(separator);
            }
        }
    }

    private void handleMenuAction(String action) {
        System.out.println("Action menu cliquée: " + action);
        
        switch (action) {
            case "dashboard":
                pageTitleLabel.setText("Tableau de Bord");
                refreshDashboard();
                break;
            case "manageUsers":
                pageTitleLabel.setText("Gestion des Utilisateurs");
                // TODO: Naviguer vers la page de gestion des utilisateurs
                break;
            case "manageDoctors":
                pageTitleLabel.setText("Gestion des Médecins");
                // TODO: Naviguer vers la page de gestion des médecins
                break;
            case "manageDonations":
                pageTitleLabel.setText("Gestion des Dons");
                // TODO: Naviguer vers la page de gestion des dons
                break;
            case "statistics":
                pageTitleLabel.setText("Statistiques");
                // TODO: Naviguer vers la page de statistiques
                break;
            case "myPatients":
                pageTitleLabel.setText("Mes Patients");
                // TODO: Naviguer vers la page des patients
                break;
            case "appointments":
                pageTitleLabel.setText("Rendez-vous");
                // TODO: Naviguer vers la page des rendez-vous
                break;
            case "reports":
                pageTitleLabel.setText("Rapports");
                // TODO: Naviguer vers la page des rapports
                break;
            case "searchDoctor":
                pageTitleLabel.setText("Chercher un Médecin");
                // TODO: Naviguer vers la page de recherche
                break;
            case "makeDonation":
                pageTitleLabel.setText("Faire un Don");
                // TODO: Naviguer vers la page de don
                break;
            case "donationHistory":
                pageTitleLabel.setText("Historique des Dons");
                // TODO: Naviguer vers la page d'historique
                break;
            case "campaigns":
                pageTitleLabel.setText("Campagnes");
                // TODO: Naviguer vers la page des campagnes
                break;
            case "profile":
                pageTitleLabel.setText("Profil");
                // TODO: Naviguer vers la page de profil
                break;
            case "settings":
                pageTitleLabel.setText("Paramètres");
                // TODO: Naviguer vers la page des paramètres
                break;
        }
    }

    private void loadStatistics() {
        // Simuler des données statistiques
        totalUsersLabel.setText("1,234");
        totalAppointmentsLabel.setText("456");
        totalDonationsLabel.setText("78,900");
        activeSessionsLabel.setText("89");
    }

    private void loadCharts() {
        // Créer le graphique en barres (activité mensuelle)
        createBarChart();
        
        // Créer le graphique circulaire (répartition par rôle)
        createPieChart();
    }

    private void createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.getCategories().addAll("Jan", "Fév", "Mar", "Avr", "Mai", "Jun");
        xAxis.setLabel("Mois");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'Utilisateurs");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux Utilisateurs");
        
        series.getData().addAll(
            new XYChart.Data<>("Jan", 120),
            new XYChart.Data<>("Fév", 150),
            new XYChart.Data<>("Mar", 180),
            new XYChart.Data<>("Avr", 220),
            new XYChart.Data<>("Mai", 280),
            new XYChart.Data<>("Jun", 320)
        );
        
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Nouveaux Utilisateurs par Mois");
        barChart.setLegendVisible(false);
        barChart.getData().add(series);
        
        barChartContainer.getChildren().clear();
        barChartContainer.getChildren().add(barChart);
    }

    private void createPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Clients", 45),
            new PieChart.Data("Donateurs", 25),
            new PieChart.Data("Médecins", 20),
            new PieChart.Data("Admins", 10)
        );
        
        pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Répartition des Utilisateurs");
        pieChart.setLegendVisible(false);
        
        pieChartContainer.getChildren().clear();
        pieChartContainer.getChildren().add(pieChart);
    }

    private void loadRecentActivity() {
        ObservableList<ActivityRecord> activityData = FXCollections.observableArrayList();
        
        // Simuler des données d'activité récente
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        activityData.addAll(
            new ActivityRecord(today, "Jean Dupont", "Nouveau patient ajouté", "✅ Succès", "Patient #1234 ajouté au système"),
            new ActivityRecord(today, "Dr. Martin", "Rendez-vous terminé", "✅ Succès", "Consultation avec Patient #5678"),
            new ActivityRecord(today, "Marie Laurent", "Don enregistré", "✅ Succès", "Don de sang de 450ml"),
            new ActivityRecord(today, "System", "Nouvel utilisateur", "✅ Succès", "Admin account créé"),
            new ActivityRecord(today, "Paul Bernard", "Profil mis à jour", "✅ Succès", "Informations personnelles modifiées")
        );
        
        activityTable.setItems(activityData);
        
        // Configurer les colonnes
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
    }

    // =========================
    // BUTTON ACTIONS
    // =========================
    
    @FXML
    public void handleRefresh() {
        System.out.println("=== BOUTON ACTUALISER CLIQUÉ ===");
        refreshDashboard();
    }

    @FXML
    public void handleNotifications() {
        System.out.println("=== BOUTON NOTIFICATIONS CLIQUÉ ===");
        // TODO: Ouvrir le panneau des notifications
        showAlert("🔔 Notifications", "Vous avez 3 nouvelles notifications");
    }

    @FXML
    public void handleSettings() {
        System.out.println("=== BOUTON PARAMÈTRES CLIQUÉ ===");
        // TODO: Naviguer vers la page des paramètres
        showAlert("⚙️ Paramètres", "Page des paramètres en cours de développement");
    }

    @FXML
    public void handleLogout() {
        System.out.println("=== BOUTON DÉCONNEXION CLIQUÉ ===");
        
        // Vider la session
        SessionService.getInstance().clearSession();
        
        try {
            // Retourner à la page de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            stage.setTitle("VitaPlus - Connexion");
            stage.centerOnScreen();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleViewAll() {
        System.out.println("=== BOUTON VOIR TOUT CLIQUÉ ===");
        // TODO: Naviguer vers la page complète d'activité
        showAlert("📋 Activité Complète", "Page d'activité détaillée en cours de développement");
    }

    private void refreshDashboard() {
        loadStatistics();
        loadCharts();
        loadRecentActivity();
        System.out.println("=== TABLEAU DE BORD ACTUALISÉ ===");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
