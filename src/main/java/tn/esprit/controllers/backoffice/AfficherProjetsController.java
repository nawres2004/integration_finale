package tn.esprit.controllers.backoffice;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tn.esprit.models.Projet;
import tn.esprit.services.ProjetService;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class AfficherProjetsController {

    @FXML private TableView<Projet>           tableProjets;
    @FXML private TableColumn<Projet, String> colTitre;
    @FXML private TableColumn<Projet, Double> colObjectif;
    @FXML private TableColumn<Projet, Double> colCollecte;
    @FXML private TableColumn<Projet, Date>   colDebut;
    @FXML private TableColumn<Projet, Date>   colFin;
    @FXML private TableColumn<Projet, Void>   colActions;

    private final ProjetService ps = new ProjetService();

    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreProjet"));
        colObjectif.setCellValueFactory(new PropertyValueFactory<>("objectifFinancier"));
        colCollecte.setCellValueFactory(new PropertyValueFactory<>("montantCollecte"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        tableProjets.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        configurerColonneActions();
        chargerDonnees();
    }

    private void configurerColonneActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox   box       = new HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setStyle("-fx-background-color:#1a73e8; -fx-text-fill:white; -fx-cursor:hand;" +
                        "-fx-background-radius:20; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-font-weight:bold;");
                btnDelete.setStyle("-fx-background-color:#e53935; -fx-text-fill:white; -fx-cursor:hand;" +
                        "-fx-background-radius:20; -fx-font-size:11px; -fx-padding:4 10 4 10; -fx-font-weight:bold;");
                box.setStyle("-fx-alignment:center;");
                btnEdit.setOnAction(e -> ouvrirPopupEdition(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> supprimerProjet(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void ouvrirPopupEdition(Projet p) {
        // Créer le Dialog
        Dialog<Projet> dialog = new Dialog<>();
        dialog.setTitle("Modifier le projet");
        dialog.setHeaderText("Modifier : " + p.getTitreProjet());

        // Boutons
        ButtonType btnEnregistrer = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler     = new ButtonType("Annuler",     ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEnregistrer, btnAnnuler);

        // Champs du formulaire
        TextField   tfTitre    = new TextField(p.getTitreProjet());
        TextArea    taDesc     = new TextArea(p.getDescription());
        TextField   tfObjectif = new TextField(String.valueOf(p.getObjectifFinancier()));
        DatePicker  dpDebut    = new DatePicker(p.getDateDebut().toLocalDate());
        DatePicker  dpFin      = new DatePicker(p.getDateFin().toLocalDate());

        taDesc.setPrefRowCount(3);
        taDesc.setWrapText(true);

        // Format date
        StringConverter<LocalDate> conv = new StringConverter<>() {
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate d)    { return d != null ? fmt.format(d) : ""; }
            @Override public LocalDate fromString(String s)  { return (s != null && !s.isEmpty()) ? LocalDate.parse(s, fmt) : null; }
        };
        dpDebut.setConverter(conv);
        dpFin.setConverter(conv);

        // Layout grille
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Titre :"),       0, 0); grid.add(tfTitre,    1, 0);
        grid.add(new Label("Objectif (DT):"),0, 1); grid.add(tfObjectif, 1, 1);
        grid.add(new Label("Début :"),       0, 2); grid.add(dpDebut,    1, 2);
        grid.add(new Label("Fin :"),         0, 3); grid.add(dpFin,      1, 3);
        grid.add(new Label("Description :"), 0, 4); grid.add(taDesc,     1, 4);

        tfTitre.setPrefWidth(280);
        tfObjectif.setPrefWidth(280);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(460);

        // Convertir le résultat
        dialog.setResultConverter(btn -> {
            if (btn == btnEnregistrer) {
                String titre = tfTitre.getText().trim();
                String desc  = taDesc.getText().trim();
                String obj   = tfObjectif.getText().trim();

                if (titre.length() < 3) {
                    showAlert(Alert.AlertType.WARNING, "Titre invalide", "Minimum 3 caractères."); return null;
                }
                if (desc.length() < 10) {
                    showAlert(Alert.AlertType.WARNING, "Description invalide", "Minimum 10 caractères."); return null;
                }
                if (obj.isEmpty() || dpDebut.getValue() == null || dpFin.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Champs vides", "Remplissez tous les champs."); return null;
                }
                if (dpFin.getValue().isBefore(dpDebut.getValue())) {
                    showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de fin doit être >= date de début."); return null;
                }
                try {
                    double objectif = Double.parseDouble(obj);
                    if (objectif <= 0) {
                        showAlert(Alert.AlertType.WARNING, "Objectif invalide", "L'objectif doit être > 0."); return null;
                    }
                    p.setTitreProjet(titre);
                    p.setDescription(desc);
                    p.setObjectifFinancier(objectif);
                    p.setDateDebut(Date.valueOf(dpDebut.getValue()));
                    p.setDateFin(Date.valueOf(dpFin.getValue()));
                    return p;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "L'objectif doit être un nombre."); return null;
                }
            }
            return null;
        });

        Optional<Projet> result = dialog.showAndWait();
        result.ifPresent(projet -> {
            try {
                ps.modifier(projet);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet modifié !");
                chargerDonnees();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            }
        });
    }

    private void chargerDonnees() {
        try {
            tableProjets.setItems(FXCollections.observableArrayList(ps.afficherAll()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void supprimerProjet(Projet p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + p.getTitreProjet() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    ps.supprimer(p.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet supprimé !");
                    chargerDonnees();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Suppression impossible", e.getMessage());
                }
            }
        });
    }

    @FXML public void goToAjouter()   { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AjouterProjet.fxml"); }
    @FXML public void goToDons()      { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AfficherDons.fxml"); }
    @FXML public void goToDashboard() { tn.esprit.controllers.AdminDashboardController.chargerDansContentPane("/backoffice/AdminDashboard.fxml"); }

    private void naviguer(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            tableProjets.getScene().setRoot(root);
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
