package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.models.Role;
import tn.esprit.services.RoleService;

import java.util.Date;
import java.util.List;

public class RoleController {

    // 🔹 FORM
    @FXML private TextField nomRole;
    @FXML private TextArea description;
    @FXML private CheckBox statut;

    // 🔹 TABLE
    @FXML private TableView<Role> tableRole;
    @FXML private TableColumn<Role, Integer> colId;
    @FXML private TableColumn<Role, String> colNom;
    @FXML private TableColumn<Role, String> colDescription;
    @FXML private TableColumn<Role, Boolean> colStatut;

    private RoleService service = new RoleService();
    private ObservableList<Role> list = FXCollections.observableArrayList();

    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {

        // 🔥 TABLE BINDING
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getIdRole()
                ).asObject()
        );

        colNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNomRole()
                )
        );

        colDescription.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDescription()
                )
        );

        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleBooleanProperty(
                        data.getValue().isStatut()
                )
        );

        loadTable();

        // 🔥 CLICK ROW → FILL FORM
        tableRole.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        nomRole.setText(selected.getNomRole());
                        description.setText(selected.getDescription());
                        statut.setSelected(selected.isStatut());
                    }
                });
    }

    // =========================
    // LOAD TABLE
    // =========================
    private void loadTable() {
        list.clear();
        list.addAll(service.afficher());
        tableRole.setItems(list);
    }

    // =========================
    // ADD ROLE
    // =========================
    @FXML
    public void ajouter() {

        if (nomRole.getText().isEmpty()) {
            showAlert("⚠️ Nom role obligatoire");
            return;
        }

        Role r = new Role(
                nomRole.getText(),
                description.getText(),
                new Date(),
                statut.isSelected()
        );

        service.ajouter(r);
        loadTable();
        clearForm();
    }

    // =========================
    // UPDATE ROLE
    // =========================
    @FXML
    public void modifier() {

        Role selected = tableRole.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("⚠️ Sélectionner un rôle");
            return;
        }

        selected.setNomRole(nomRole.getText());
        selected.setDescription(description.getText());
        selected.setStatut(statut.isSelected());

        service.modifier(selected);
        loadTable();
        clearForm();
    }

    // =========================
    // DELETE ROLE
    // =========================
    @FXML
    public void supprimer() {

        Role selected = tableRole.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("⚠️ Sélectionner un rôle");
            return;
        }

        service.supprimer(selected.getIdRole());
        loadTable();
        clearForm();
    }

    // =========================
    // CLEAR FORM
    // =========================
    private void clearForm() {
        nomRole.clear();
        description.clear();
        statut.setSelected(false);
    }

    // =========================
    // ALERT
    // =========================
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}