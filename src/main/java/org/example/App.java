package org.example;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.entities.User;
import org.example.services.ServiceArticle;
import org.example.services.ServiceCategorie;
import org.example.services.ServiceUser;
import org.example.utils.MyDatabase;
import org.example.utils.UserSession;

import javafx.fxml.Initializable;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class App extends Application implements Initializable {

    private static Scene scene;

    // ──── Dashboard sidebar & topbar bindings ────
    @FXML private Button manageCategoriesBtn;
    @FXML private Label  sidebarUserName;
    @FXML private Label  sidebarUserRole;
    @FXML private Label  topBarUser;
    @FXML private Label  topBarTitle;
    @FXML private Label  statArticles;
    @FXML private Label  statCategories;
    @FXML private Button notifBtn;

    private org.example.services.ServiceNotification serviceNotification = new org.example.services.ServiceNotification();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UserSession session = UserSession.getInstance();
        if (session == null) return;

        User user = session.getUser();

        // Populate sidebar user info
        if (sidebarUserName != null)
            sidebarUserName.setText(user.getNom() + " " + user.getPrenom());
        if (sidebarUserRole != null)
            sidebarUserRole.setText(getRoleName(user.getIdRole()));
        if (topBarUser != null)
            topBarUser.setText(user.getEmail());

        // Restrict categories button for non-admins
        if (manageCategoriesBtn != null && user.getIdRole() != 1) {
            manageCategoriesBtn.setVisible(false);
            manageCategoriesBtn.setManaged(false);
        }

        // Populate stat cards
        if (statArticles != null) {
            int count = new ServiceArticle().getAll().size();
            statArticles.setText(String.valueOf(count));
        }
        if (statCategories != null) {
            int count = new ServiceCategorie().getAll().size();
            statCategories.setText(String.valueOf(count));
        }

        updateNotificationCount();
    }

    private void updateNotificationCount() {
        if (UserSession.getInstance() != null && notifBtn != null) {
            int count = serviceNotification.getUnreadCount(UserSession.getInstance().getUser().getIdUtilisateur());
            if (count > 0) {
                notifBtn.setText("🔔 (" + count + ")");
                notifBtn.setStyle("-fx-background-radius: 20; -fx-padding: 8 15; -fx-background-color: #ff4757; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                notifBtn.setText("🔔 (0)");
                notifBtn.setStyle("-fx-background-radius: 20; -fx-padding: 8 15; -fx-background-color: #EBF5FB; -fx-text-fill: #0C5283;");
            }
        }
    }

    private String getRoleName(int roleId) {
        return switch (roleId) {
            case 1 -> "Administrator";
            case 2 -> "Patient";
            case 3 -> "Doctor";
            default -> "User";
        };
    }

    @Override
    public void start(Stage stage) throws IOException {
        MyDatabase.getInstance();

        // Seed DB on first run
        new ServiceUser().seedDatabase();

        scene = new Scene(loadFXML("signin"), 900, 600);
        stage.setScene(scene);
        stage.setTitle("VitaPlus");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static javafx.scene.layout.StackPane integrationPane;

    public static void setRoot(String fxml) throws IOException {
        if (integrationPane != null) {
            integrationPane.getChildren().setAll((javafx.scene.Node) loadFXML(fxml));
        } else {
            scene.setRoot(loadFXML(fxml));
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String fxmlPath = "/org/example/" + fxml + ".fxml";
        java.net.URL fxmlLocation = App.class.getResource(fxmlPath);
        if (fxmlLocation == null) {
            throw new IOException("Cannot find FXML file: " + fxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        return loader.load();
    }

    // ──── Navigation (called from primary.fxml) ────

    @FXML
    private void showDashboard() throws IOException {
        // Already on dashboard – no-op (we're the controller of primary.fxml)
    }

    @FXML
    private void switchToCategorie() throws IOException {
        if (UserSession.getInstance() != null
                && UserSession.getInstance().getUser().getIdRole() == 1) {
            App.setRoot("add_categorie");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("Only Administrators can manage categories.");
            alert.show();
        }
    }

    @FXML
    private void switchToArticleList() throws IOException {
        App.setRoot("article_list");
    }

    @FXML
    private void handleLogOut() throws IOException {
        UserSession.cleanUserSession();
        App.setRoot("signin");
    }

    @FXML
    private void handleShowNotifications(javafx.event.ActionEvent event) {
        if (UserSession.getInstance() == null) return;
        int userId = UserSession.getInstance().getUser().getIdUtilisateur();
        java.util.List<org.example.entities.Notification> notifs = serviceNotification.getByUser(userId);
        
        if (notifs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText(null);
            alert.setContentText("Vous n'avez aucune notification.");
            alert.show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (org.example.entities.Notification n : notifs) {
            String status = n.isRead() ? "" : "[NOUVEAU] ";
            sb.append(status).append(n.getTitre()).append(" - ").append(n.getMessage()).append("\n")
              .append(n.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vos Notifications");
        alert.setHeaderText("Dernières notifications");
        
        javafx.scene.control.TextArea area = new javafx.scene.control.TextArea(sb.toString());
        area.setWrapText(true);
        area.setEditable(false);
        area.setPrefWidth(400);
        area.setPrefHeight(300);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();

        // Mark all as read
        serviceNotification.markAllAsRead(userId);
        updateNotificationCount();
    }

    public static void main(String[] args) {
        launch();
    }
}
