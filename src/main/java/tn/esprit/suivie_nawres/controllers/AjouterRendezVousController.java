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
import java.sql.SQLException;

public class AjouterRendezVousController {
    private static final String NOM_PRENOM_REGEX = "^[\\p{L}][\\p{L} '\\-]{1,49}$";
    private static RendezVous rendezVousAEditer;
    private static AfficherRendezVousMedecinController parentMedecinController;
    private static boolean fermerApresEnregistrement;

    @FXML private TextField txtUtilisateurId;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
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
            String erreurValidation = validerChampsObligatoires();
            if (erreurValidation != null) {
                afficherErreur(erreurValidation);
                return;
            }

            int utilisateurId = parseUtilisateurId();
            RendezVous rendezVous = new RendezVous();
            rendezVous.setUtilisateurId(utilisateurId);
            rendezVous.setNom(txtNom.getText().trim());
            rendezVous.setPrenom(txtPrenom.getText().trim());
            rendezVous.setDateRendezVous(datePickerRendezVous.getValue());
            rendezVous.setHeureRendezVous(parseHeure(txtHeureRendezVous.getText().trim()));
            rendezVous.setPriorite(comboPriorite.getValue());
            rendezVous.setModeConsultation(comboModeConsultation.getValue());
            rendezVous.setStatutRendezVous(resolveStatutPourSauvegarde());
            rendezVous.setNotesRendezVous(txtNotesRendezVous.getText().trim());
            rendezVous.setPays(txtPays.getText().trim());
            rendezVous.setTelephone(txtTelephone.getText().trim());

            if (rendezVousService.existe(rendezVous.getUtilisateurId())) {
                rendezVousService.modifierRendezVous(rendezVous);
                afficherInfo("Rendez-vous modifie avec succes.");
            } else {
                rendezVousService.ajouterRendezVous(rendezVous);
                afficherInfo("Rendez-vous ajoute avec succes.");
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
        } catch (IllegalArgumentException exception) {
            afficherErreur(exception.getMessage());
        } catch (SQLException exception) {
            afficherErreur("Erreur base de donnees. Verifie la colonne id dans la table.");
        } catch (Exception exception) {
            afficherErreur("Operation impossible. Verifie les champs saisis.");
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
        afficherInfo("Pret.");
    }

    private void viderChamps() {
        txtUtilisateurId.clear();
        txtUtilisateurId.setDisable(false);
        txtNom.clear();
        txtPrenom.clear();
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

    private String validerChampsObligatoires() {
        if (txtUtilisateurId.getText() == null || txtUtilisateurId.getText().trim().isEmpty()) {
            return "Le champ Utilisateur ID est obligatoire.";
        }
        if (!txtUtilisateurId.getText().trim().matches("\\d+")) {
            return "Utilisateur ID doit etre un nombre positif.";
        }
        if (datePickerRendezVous.getValue() == null) {
            return "Le champ Date rendez-vous est obligatoire.";
        }
        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            return "Le champ Nom est obligatoire.";
        }
        if (!txtNom.getText().trim().matches(NOM_PRENOM_REGEX)) {
            return "Nom invalide : utilise uniquement des lettres, espaces, tiret ou apostrophe (2 a 50 caracteres).";
        }
        if (txtPrenom.getText() == null || txtPrenom.getText().trim().isEmpty()) {
            return "Le champ Prenom est obligatoire.";
        }
        if (!txtPrenom.getText().trim().matches(NOM_PRENOM_REGEX)) {
            return "Prenom invalide : utilise uniquement des lettres, espaces, tiret ou apostrophe (2 a 50 caracteres).";
        }
        if (txtHeureRendezVous.getText() == null || txtHeureRendezVous.getText().trim().isEmpty()) {
            return "Le champ Heure rendez-vous est obligatoire.";
        }
        if (comboPriorite.getValue() == null || comboPriorite.getValue().trim().isEmpty()) {
            return "Le champ Priorite est obligatoire.";
        }
        if (comboModeConsultation.getValue() == null || comboModeConsultation.getValue().trim().isEmpty()) {
            return "Le champ Mode consultation est obligatoire.";
        }
        if (txtNotesRendezVous.getText() == null || txtNotesRendezVous.getText().trim().isEmpty()) {
            return "Le champ Notes est obligatoire.";
        }
        if (txtPays.getText() == null || txtPays.getText().trim().isEmpty()) {
            return "Le champ Pays est obligatoire.";
        }
        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            return "Le champ Telephone est obligatoire.";
        }
        return null;
    }

    private void remplirFormulaire(RendezVous rendezVous) {
        txtUtilisateurId.setText(String.valueOf(rendezVous.getUtilisateurId()));
        txtNom.setText(rendezVous.getNom());
        txtPrenom.setText(rendezVous.getPrenom());
        datePickerRendezVous.setValue(rendezVous.getDateRendezVous());
        txtHeureRendezVous.setText(rendezVous.getHeureRendezVous() == null ? "" : rendezVous.getHeureRendezVous().toString());
        comboPriorite.setValue(rendezVous.getPriorite());
        comboModeConsultation.setValue(rendezVous.getModeConsultation());
        txtNotesRendezVous.setText(rendezVous.getNotesRendezVous());
        txtPays.setText(rendezVous.getPays());
        txtTelephone.setText(rendezVous.getTelephone());
        statutEdition = rendezVous.getStatutRendezVous() == null ? StatutRendezVous.EN_ATTENTE : rendezVous.getStatutRendezVous();
        afficherInfo("Mode modification actif.");
    }

    private LocalTime parseHeure(String valeur) {
        String texte = valeur == null ? "" : valeur.trim();
        if (texte.length() == 5) {
            texte = texte + ":00";
        }
        return LocalTime.parse(texte);
    }

    private int parseUtilisateurId() {
        String texte = txtUtilisateurId.getText() == null ? "" : txtUtilisateurId.getText().trim();
        if (texte.isEmpty()) {
            throw new IllegalArgumentException("Le champ Utilisateur ID est obligatoire.");
        }
        if (!texte.matches("\\d+")) {
            throw new IllegalArgumentException("Utilisateur ID doit etre un nombre positif.");
        }
        return Integer.parseInt(texte);
    }

    private void afficherMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    private void afficherInfo(String message) {
        if (lblMessage != null) {
            lblMessage.setStyle("");
        }
        afficherMessage(message);
    }

    private void afficherErreur(String message) {
        if (lblMessage != null) {
            lblMessage.setStyle("-fx-text-fill: #d32f2f;");
        }
        afficherMessage(message);
    }
}

