package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.UtilisateurService;

import java.util.Date;

public class UtilisateurController {

    @FXML private TextField nom, prenom, email, telephone, specialite;
    @FXML private PasswordField motDePasse;
    @FXML private ComboBox<Integer> role;
    @FXML private CheckBox isActive;

    @FXML private FlowPane cardsContainer;
    private Utilisateur selectedUser = null;

    private UtilisateurService service = new UtilisateurService();
    private ObservableList<Utilisateur> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 🔹 Load roles (simple version)
        role.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
        
        loadCards();
    }

    // ✅ AFFICHER CARDS
    @FXML
    public void loadCards() {
        if (cardsContainer == null) return;
        cardsContainer.getChildren().clear();
        
        list.clear();
        list.addAll(service.afficher());
        
        for (Utilisateur u : list) {
            cardsContainer.getChildren().add(createCard(u));
        }
    }

    private VBox createCard(Utilisateur u) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 5); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 15; -fx-border-width: 1;");

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(14, 165, 233, 0.1), 15, 0, 0, 8); "
                + "-fx-border-color: #0ea5e9; -fx-border-radius: 15; -fx-border-width: 1;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 5); "
                + "-fx-border-color: #f1f5f9; -fx-border-radius: 15; -fx-border-width: 1;"));

        // Header: Name
        Label nameLabel = new Label(u.getPrenom() + " " + u.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Email
        Label emailLabel = new Label("✉ " + u.getEmail());
        emailLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        
        // Phone
        Label phoneLabel = new Label("📞 " + (u.getTelephone() != null ? u.getTelephone() : "N/A"));
        phoneLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");

        // Badges Row
        HBox badges = new HBox(8);
        
        // Role Badge
        Label roleBadge = new Label();
        switch (u.getIdRole()) {
            case 1: roleBadge.setText("👤 Patient"); roleBadge.setStyle("-fx-background-color: #f0f9ff; -fx-text-fill: #0ea5e9;"); break;
            case 2: roleBadge.setText("💰 Donateur"); roleBadge.setStyle("-fx-background-color: #f5f3ff; -fx-text-fill: #7c3aed;"); break;
            case 3: roleBadge.setText("🩺 Médecin"); roleBadge.setStyle("-fx-background-color: #ecfeff; -fx-text-fill: #0891b2;"); break;
            case 4: roleBadge.setText("🔑 Admin"); roleBadge.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626;"); break;
            default: roleBadge.setText("Rôle " + u.getIdRole()); break;
        }
        roleBadge.setStyle(roleBadge.getStyle() + "-fx-font-size: 10; -fx-padding: 4 8; -fx-background-radius: 10; -fx-font-weight: bold;");

        // Status Badge
        Label statusBadge = new Label(u.isActive() ? "● Actif" : "○ Inactif");
        statusBadge.setStyle(u.isActive() 
            ? "-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-font-size: 10; -fx-padding: 4 8; -fx-background-radius: 10; -fx-font-weight: bold;"
            : "-fx-background-color: #fff1f2; -fx-text-fill: #e11d48; -fx-font-size: 10; -fx-padding: 4 8; -fx-background-radius: 10; -fx-font-weight: bold;");

        badges.getChildren().addAll(roleBadge, statusBadge);

        // Actions Row
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("📝");
        editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand; -fx-background-radius: 5;");
        editBtn.setOnAction(e -> fillForm(u));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-cursor: hand; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> {
            service.supprimer(u.getIdUtilisateur());
            loadCards();
        });

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(nameLabel, emailLabel, phoneLabel, badges, actions);
        return card;
    }

    private void fillForm(Utilisateur u) {
        selectedUser = u;
        nom.setText(u.getNom());
        prenom.setText(u.getPrenom());
        email.setText(u.getEmail());
        telephone.setText(u.getTelephone());
        specialite.setText(u.getSpecialite());
        role.setValue(u.getIdRole());
        isActive.setSelected(u.isActive());
        motDePasse.setText(u.getMotDePasse());
    }

    // ✅ AJOUTER
    @FXML
    public void ajouter() {
        Utilisateur u = new Utilisateur(
                nom.getText(),
                prenom.getText(),
                email.getText(),
                motDePasse.getText(),
                telephone.getText(),
                role.getValue(),
                specialite.getText(),
                new Date(),
                isActive.isSelected()
        );

        service.ajouter(u);
        loadCards();
        clearForm();
    }

    // ✅ MODIFIER
    @FXML
    public void modifier() {
        if (selectedUser != null) {
            selectedUser.setNom(nom.getText());
            selectedUser.setPrenom(prenom.getText());
            selectedUser.setEmail(email.getText());
            selectedUser.setTelephone(telephone.getText());
            selectedUser.setMotDePasse(motDePasse.getText());
            selectedUser.setSpecialite(specialite.getText());
            selectedUser.setIdRole(role.getValue());
            selectedUser.setActive(isActive.isSelected());

            service.modifier(selectedUser);
            loadCards();
            clearForm();
        }
    }

    // ✅ SUPPRIMER
    @FXML
    public void supprimer() {
        if (selectedUser != null) {
            service.supprimer(selectedUser.getIdUtilisateur());
            loadCards();
            clearForm();
        }
    }

    // 🔹 CLEAR FORM
    @FXML
    public void clearForm() {
        selectedUser = null;
        nom.clear();
        prenom.clear();
        email.clear();
        telephone.clear();
        specialite.clear();
        motDePasse.clear();
        role.setValue(null);
        isActive.setSelected(false);
    }
}