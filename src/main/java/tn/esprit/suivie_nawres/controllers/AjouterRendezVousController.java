package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

import java.time.LocalTime;

public class AjouterRendezVousController {
    private static RendezVous rendezVousAEditer;
    private static AfficherRendezVousMedecinController parentMedecinController;
    private static boolean fermerApresEnregistrement;

    @FXML private TextField txtUtilisateurId;
    @FXML private DatePicker datePickerRendezVous;
    @FXML private TextField txtHeureRendezVous;
    @FXML private ComboBox<String> comboPriorite;
    @FXML private ComboBox<String> comboModeConsultation;
    @FXML private TextArea txtNotesRendezVous;
    @FXML private TextField txtPays;
    @FXML private TextField txtTelephone;
    @FXML private Label lblMessage;

    private final RendezVousService rendezVousService = new RendezVousService();
    private StatutRendezVous statutEdition = StatutRendezVous.EN_ATTENTE;
    private boolean modeEdition;

    public static void setRendezVousAEditer(RendezVous rendezVous) {
        rendezVousAEditer = rendezVous;
    }

    public static void setParentMedecinController(AfficherRendezVousMedecinController controller) {
        parentMedecinController = controller;
        fermerApresEnregistrement = true;
    }

    public static void setFermerApresEnregistrement(boolean fermer) {
        fermerApresEnregistrement = fermer;
    }

    @FXML
    private void initialize() {
        comboPriorite.setItems(FXCollections.observableArrayList("BASSE", "NORMALE", "HAUTE", "URGENTE"));
        comboModeConsultation.setItems(FXCollections.observableArrayList("A_DISTANCE", "PRESENTIEL", "TELECONSULTATION"));
        comboPriorite.getSelectionModel().selectFirst();
        comboModeConsultation.getSelectionModel().selectFirst();
        modeEdition = rendezVousAEditer != null;
        if (rendezVousAEditer != null) {
            remplirFormulaire(rendezVousAEditer);
            txtUtilisateurId.setDisable(true);
            rendezVousAEditer = null;
        }
    }

    @FXML
    private void enregistrer() {
        try {
            RendezVous rendezVous = new RendezVous();
            rendezVous.setUtilisateurId(Integer.parseInt(txtUtilisateurId.getText().trim()));
            rendezVous.setDateRendezVous(datePickerRendezVous.getValue());
            rendezVous.setHeureRendezVous(parseHeure(txtHeureRendezVous.getText()));
            rendezVous.setPriorite(comboPriorite.getValue());
            rendezVous.setModeConsultation(comboModeConsultation.getValue());
            rendezVous.setStatutRendezVous(resolveStatutPourSauvegarde());
            rendezVous.setNotesRendezVous(txtNotesRendezVous.getText());
            rendezVous.setPays(txtPays.getText());
            rendezVous.setTelephone(txtTelephone.getText());

            if (rendezVousService.existe(rendezVous.getUtilisateurId())) {
                rendezVousService.modifierRendezVous(rendezVous);
                afficherMessage("Rendez-vous modifie avec succes.");
            } else {
                rendezVousService.ajouterRendezVous(rendezVous);
                afficherMessage("Rendez-vous ajoute avec succes.");
            }
            if (RoleContext.getCurrentRole() == UserRole.PATIENT) {
                RoleContext.setCurrentUtilisateurId(rendezVous.getUtilisateurId());
            }
            if (parentMedecinController != null) {
                parentMedecinController.rafraichirListe();
            }
            if (fermerApresEnregistrement) {
                fermerModale();
            } else {
                viderChamps();
            }
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    private void fermerModale() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) txtUtilisateurId.getScene().getWindow();
            stage.close();
            fermerApresEnregistrement = false;
        } catch (Exception e) {
            System.err.println("Erreur fermeture modale : " + e.getMessage());
        }
    }

    @FXML
    private void vider() {
        viderChamps();
        afficherMessage("Prêt.");
    }

    private void viderChamps() {
        txtUtilisateurId.clear();
        txtUtilisateurId.setDisable(false);
        datePickerRendezVous.setValue(null);
        txtHeureRendezVous.clear();
        comboPriorite.getSelectionModel().selectFirst();
        comboModeConsultation.getSelectionModel().selectFirst();
        txtNotesRendezVous.clear();
        txtPays.clear();
        txtTelephone.clear();
        statutEdition = StatutRendezVous.EN_ATTENTE;
        modeEdition = false;
    }

    private StatutRendezVous resolveStatutPourSauvegarde() {
        if (modeEdition) {
            return statutEdition;
        }
        return RoleContext.getCurrentRole() == UserRole.MEDECIN
                ? StatutRendezVous.ACCEPTE
                : StatutRendezVous.EN_ATTENTE;
    }

    private void remplirFormulaire(RendezVous rendezVous) {
        txtUtilisateurId.setText(String.valueOf(rendezVous.getUtilisateurId()));
        datePickerRendezVous.setValue(rendezVous.getDateRendezVous());
        txtHeureRendezVous.setText(rendezVous.getHeureRendezVous() == null ? "" : rendezVous.getHeureRendezVous().toString());
        comboPriorite.setValue(rendezVous.getPriorite());
        comboModeConsultation.setValue(rendezVous.getModeConsultation());
        txtNotesRendezVous.setText(rendezVous.getNotesRendezVous());
        txtPays.setText(rendezVous.getPays());
        txtTelephone.setText(rendezVous.getTelephone());
        statutEdition = rendezVous.getStatutRendezVous() == null ? StatutRendezVous.EN_ATTENTE : rendezVous.getStatutRendezVous();
        afficherMessage("Mode modification actif.");
    }

    private LocalTime parseHeure(String valeur) {
        String texte = valeur == null ? "" : valeur.trim();
        if (texte.length() == 5) {
            texte = texte + ":00";
        }
        return LocalTime.parse(texte);
    }

    private void afficherMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }
}

