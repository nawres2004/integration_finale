package tn.esprit.controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.models.Projet;
import tn.esprit.services.ProjetService;
import tn.esprit.services.ProjetSimilariteService;
import tn.esprit.services.SessionService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FrontProjetsController {

    @FXML private FlowPane         flowProjets;
    @FXML private TextField        tfRecherche;
    @FXML private ComboBox<String> cbTriPar;
    @FXML private ComboBox<String> cbOrdre;
    @FXML private Button           btnEspaceAdmin;
    @FXML private Label            topUserLabel;

    private final ProjetService ps = new ProjetService();
    private ArrayList<Projet> tousLesProjets = new ArrayList<>();

    public void initialize() {
        cbTriPar.setItems(FXCollections.observableArrayList("Titre", "Objectif", "Date debut"));
        cbOrdre.setItems(FXCollections.observableArrayList("Croissant", "Decroissant"));
        chargerDonnees();

        // Masquer le bouton Espace Admin si l'utilisateur n'est pas admin (rôle 4)
        if (btnEspaceAdmin != null) {
            tn.esprit.models.Utilisateur u = SessionService.getInstance().getCurrentUser();
            boolean isAdmin = u != null && u.getIdRole() == 4;
            btnEspaceAdmin.setVisible(isAdmin);
            btnEspaceAdmin.setManaged(isAdmin);
        }

        // Nom dans le header
        if (topUserLabel != null) {
            tn.esprit.models.Utilisateur u = SessionService.getInstance().getCurrentUser();
            if (u != null) {
                String full = (u.getPrenom() != null ? u.getPrenom() : "") + " " +
                              (u.getNom() != null ? u.getNom() : "");
                topUserLabel.setText("💰 " + full.trim());
            }
        }
    }

    @FXML
    public void openProfile() {
        tn.esprit.utils.ProfileHelper.open(flowProjets.getScene().getWindow());
    }

    @FXML
    public void openChangePassword() {
        tn.esprit.utils.ProfileHelper.openChangePassword(flowProjets.getScene().getWindow());
    }

    private void chargerDonnees() {
        try {
            tousLesProjets = ps.afficherAll();
            afficherCartes(tousLesProjets);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void afficherCartes(ArrayList<Projet> liste) {
        flowProjets.getChildren().clear();
        for (Projet p : liste) {
            flowProjets.getChildren().add(creerCarte(p));
        }
    }

    private VBox creerCarte(Projet p) {
        VBox carte = new VBox(10);
        carte.setPrefWidth(270);
        carte.setMaxWidth(270);
        carte.setPadding(new Insets(16));
        carte.setStyle(
            "-fx-background-color:white; -fx-background-radius:12;" +
            "-fx-border-radius:12; -fx-border-color:#e0e0e0; -fx-border-width:1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        // Titre
        Label lblTitre = new Label(p.getTitreProjet());
        lblTitre.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1a1a2e; -fx-wrap-text:true;");
        lblTitre.setMaxWidth(240);
        lblTitre.setWrapText(true);

        // Dates
        Label lblDates = new Label("📅 " + p.getDateDebut() + "  →  " + p.getDateFin());
        lblDates.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#1a73e8;");

        // Description
        String desc = p.getDescription();
        if (desc != null && desc.length() > 80) desc = desc.substring(0, 80) + "...";
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size:12px; -fx-text-fill:#555; -fx-wrap-text:true;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(240);

        // Montants
        double objectif  = p.getObjectifFinancier();
        double collecte  = p.getMontantCollecte();
        double pct       = objectif > 0 ? Math.min(collecte / objectif, 1.0) : 0;

        HBox hMontants = new HBox();
        hMontants.setStyle("-fx-alignment:center-left;");
        Label lblCollecte = new Label(String.format("%.2f TND", collecte));
        lblCollecte.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1a73e8;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblObjectif = new Label(String.format("/ %.2f TND", objectif));
        lblObjectif.setStyle("-fx-font-size:12px; -fx-text-fill:#888;");
        hMontants.getChildren().addAll(lblCollecte, spacer, lblObjectif);

        // Barre de progression
        ProgressBar pb = new ProgressBar(pct);
        pb.setPrefWidth(240);
        pb.setPrefHeight(8);
        pb.setStyle("-fx-accent:#1a73e8; -fx-background-radius:10; -fx-background-color:#e0e0e0;");

        Label lblPct = new Label(String.format("Progression : %.0f%%", pct * 100));
        lblPct.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");

        // Boutons
        Button btnDon = new Button("Faire un don");
        btnDon.setPrefWidth(240);
        btnDon.setStyle(
            "-fx-background-color:#1a73e8; -fx-text-fill:white; -fx-font-weight:bold;" +
            "-fx-background-radius:20; -fx-cursor:hand; -fx-font-size:12px; -fx-padding:8 0 8 0;"
        );

        Button btnDetails = new Button("Voir détails");
        btnDetails.setPrefWidth(240);
        btnDetails.setStyle(
            "-fx-background-color:transparent; -fx-text-fill:#1a73e8; -fx-font-weight:bold;" +
            "-fx-background-radius:20; -fx-cursor:hand; -fx-font-size:12px; -fx-padding:6 0 6 0;" +
            "-fx-border-color:#1a73e8; -fx-border-radius:20; -fx-border-width:1;"
        );
        btnDetails.setOnAction(e -> afficherDetails(p));

        boolean objectifAtteint = objectif > 0 && collecte >= objectif;
        if (objectifAtteint) {
            btnDon.setText("Objectif atteint ✓");
            btnDon.setStyle(
                "-fx-background-color:#d1fae5; -fx-text-fill:#065f46; -fx-font-weight:bold;" +
                "-fx-background-radius:20; -fx-font-size:12px; -fx-padding:8 0 8 0;"
            );
            btnDon.setDisable(true);
        } else {
            btnDon.setOnAction(e -> ouvrirDon(p));
        }

        carte.getChildren().addAll(lblTitre, lblDates, sep, lblDesc, hMontants, pb, lblPct, btnDetails, btnDon);
        return carte;
    }

    @FXML
    public void appliquerTri() {
        try {
            ArrayList<Projet> liste = ps.afficherAll();
            String tri   = cbTriPar.getValue();
            String ordre = cbOrdre.getValue();
            if (tri != null) {
                liste.sort((a, b) -> {
                    int cmp = switch (tri) {
                        case "Titre"      -> a.getTitreProjet().compareToIgnoreCase(b.getTitreProjet());
                        case "Objectif"   -> Double.compare(a.getObjectifFinancier(), b.getObjectifFinancier());
                        case "Date debut" -> a.getDateDebut().compareTo(b.getDateDebut());
                        default -> 0;
                    };
                    return "Decroissant".equals(ordre) ? -cmp : cmp;
                });
            }
            afficherCartes(liste);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void reinitialiser() {
        tfRecherche.clear();
        cbTriPar.setValue(null);
        cbOrdre.setValue(null);
        chargerDonnees();
    }

    @FXML
    public void rechercher() {
        String terme = tfRecherche.getText().trim().toLowerCase();
        try {
            ArrayList<Projet> tous = ps.afficherAll();
            if (terme.isEmpty()) {
                afficherCartes(tous);
            } else {
                ArrayList<Projet> filtres = new ArrayList<>();
                for (Projet p : tous) {
                    if (p.getTitreProjet().toLowerCase().contains(terme))
                        filtres.add(p);
                }
                afficherCartes(filtres);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void afficherDetails(Projet p) {
        double pct = p.getObjectifFinancier() > 0
                ? Math.min(p.getMontantCollecte() / p.getObjectifFinancier() * 100, 100)
                : 0;

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du projet");
        alert.setHeaderText(p.getTitreProjet());
        alert.setContentText(
                "📅 Début : " + p.getDateDebut() + "\n" +
                "📅 Fin   : " + p.getDateFin()   + "\n\n" +
                "📝 Description :\n" + p.getDescription() + "\n\n" +
                "🎯 Objectif    : " + String.format("%.2f TND", p.getObjectifFinancier()) + "\n" +
                "💰 Collecté    : " + String.format("%.2f TND", p.getMontantCollecte())  + "\n" +
                "📊 Progression : " + String.format("%.1f%%", pct)
        );
        alert.showAndWait();
    }

    private void ouvrirDon(Projet p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/AjouterDon.fxml"));
            Parent root = loader.load();
            AjouterDonController ctrl = loader.getController();
            ctrl.setProjet(p);
            flowProjets.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void goToAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/backoffice/AdminDashboard.fxml"));
            flowProjets.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void goToStats() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/frontoffice/StatistiquesFront.fxml"));
            flowProjets.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void logout() {
        SessionService.getInstance().clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            flowProjets.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
