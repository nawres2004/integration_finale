package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.SceneManager;
import tn.esprit.suivie_nawres.utils.UserRole;

public class ChoixRoleController {

    @FXML
    private void ouvrirAdmin() {
        RoleContext.setCurrentRole(UserRole.ADMIN);
        SceneManager.show("/views/DashboardAdmin.fxml", "VitaPlus Medical - Admin");
    }

    @FXML
    private void ouvrirPatient() {
        RoleContext.setCurrentRole(UserRole.PATIENT);
        SceneManager.show("/views/DashboardPatient.fxml", "VitaPlus Medical - Patient");
    }

    @FXML
    private void ouvrirMedecin() {
        RoleContext.setCurrentRole(UserRole.MEDECIN);
        SceneManager.show("/views/DashboardMedecin.fxml", "VitaPlus Medical - Médecin");
    }
}

