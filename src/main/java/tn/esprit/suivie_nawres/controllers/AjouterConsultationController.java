package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.services.ConsultationService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalTime;

public class AjouterConsultationController {
    private static final String NOM_PRENOM_REGEX = "^[\\p{L}][\\p{L} '\\-]{1,49}$";
    private static Consultation consultationAEditer;
    private static AfficherConsultationController parentConsultationController;

    @FXML private TextField txtUtilisateurId;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private DatePicker datePickerConsultation;
    @FXML private TextField txtHeureConsultation;
    @FXML private TextField txtModeConsultation;
    @FXML private TextField txtMaladie;
    @FXML private TextField txtDiagnostic;
    @FXML private TextField txtTraitement;
    @FXML private TextField txtExamensComplementaires;
    @FXML private TextArea txtNotesConsultation;
    @FXML private TextField txtCoutConsultation;
    @FXML private Label lblMessage;

    private final ConsultationService consultationService = new ConsultationService();

    public static void setConsultationAEditer(Consultation consultation) {
        consultationAEditer = consultation;
    }

    public static void setParentConsultationController(AfficherConsultationController controller) {
        parentConsultationController = controller;
    }

    @FXML
    private void initialize() {
        if (consultationAEditer != null) {
            remplirFormulaire(consultationAEditer);
            txtUtilisateurId.setDisable(true);
            consultationAEditer = null;
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
            Consultation consultation = new Consultation();
            consultation.setUtilisateurId(utilisateurId);
            consultation.setNom(txtNom.getText().trim());
            consultation.setPrenom(txtPrenom.getText().trim());
            consultation.setDateConsultation(datePickerConsultation.getValue());
            consultation.setHeureConsultation(parseHeure(txtHeureConsultation.getText().trim()));
            consultation.setModeConsultation(txtModeConsultation.getText().trim());
            consultation.setMaladie(txtMaladie.getText().trim());
            consultation.setDiagnostic(txtDiagnostic.getText().trim());
            consultation.setTraitement(txtTraitement.getText().trim());
            consultation.setExamensComplementaires(txtExamensComplementaires.getText().trim());
            consultation.setNotesConsultation(txtNotesConsultation.getText().trim());
            consultation.setCoutConsultation(parseCout(txtCoutConsultation.getText().trim()));

            if (consultationService.existe(consultation.getUtilisateurId())) {
                consultationService.modifierConsultation(consultation);
                afficherInfo("Consultation modifiee avec succes.");
            } else {
                consultationService.ajouterConsultation(consultation);
                afficherInfo("Consultation ajoutee avec succes.");
            }
            if (parentConsultationController != null) {
                parentConsultationController.rafraichirListe();
            }
            vider();
        } catch (IllegalArgumentException exception) {
            afficherErreur(exception.getMessage());
        } catch (SQLException exception) {
            afficherErreur("Erreur base de donnees. Verifie la colonne id dans la table.");
        } catch (Exception exception) {
            afficherErreur("Operation impossible. Verifie les champs saisis.");
        }
    }

    @FXML
    private void vider() {
        txtUtilisateurId.clear();
        txtUtilisateurId.setDisable(false);
        txtNom.clear();
        txtPrenom.clear();
        datePickerConsultation.setValue(null);
        txtHeureConsultation.clear();
        txtModeConsultation.clear();
        txtMaladie.clear();
        txtDiagnostic.clear();
        txtTraitement.clear();
        txtExamensComplementaires.clear();
        txtNotesConsultation.clear();
        txtCoutConsultation.clear();
        afficherInfo("Pret.");
    }

    private void remplirFormulaire(Consultation consultation) {
        txtUtilisateurId.setText(String.valueOf(consultation.getUtilisateurId()));
        txtNom.setText(consultation.getNom());
        txtPrenom.setText(consultation.getPrenom());
        datePickerConsultation.setValue(consultation.getDateConsultation());
        txtHeureConsultation.setText(consultation.getHeureConsultation() == null ? "" : consultation.getHeureConsultation().toString());
        txtModeConsultation.setText(consultation.getModeConsultation());
        txtMaladie.setText(consultation.getMaladie());
        txtDiagnostic.setText(consultation.getDiagnostic());
        txtTraitement.setText(consultation.getTraitement());
        txtExamensComplementaires.setText(consultation.getExamensComplementaires());
        txtNotesConsultation.setText(consultation.getNotesConsultation());
        txtCoutConsultation.setText(consultation.getCoutConsultation() == null ? "" : consultation.getCoutConsultation().toString());
        afficherInfo("Mode modification actif.");
    }

    private LocalTime parseHeure(String valeur) {
        String texte = valeur == null ? "" : valeur.trim();
        if (texte.length() == 5) {
            texte = texte + ":00";
        }
        return LocalTime.parse(texte);
    }

    private BigDecimal parseCout(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(valeur.trim());
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

    private String validerChampsObligatoires() {
        if (txtUtilisateurId.getText() == null || txtUtilisateurId.getText().trim().isEmpty()) {
            return "Le champ Utilisateur ID est obligatoire.";
        }
        if (!txtUtilisateurId.getText().trim().matches("\\d+")) {
            return "Utilisateur ID doit etre un nombre positif.";
        }
        if (datePickerConsultation.getValue() == null) {
            return "Le champ Date consultation est obligatoire.";
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
        if (txtHeureConsultation.getText() == null || txtHeureConsultation.getText().trim().isEmpty()) {
            return "Le champ Heure consultation est obligatoire.";
        }
        if (txtModeConsultation.getText() == null || txtModeConsultation.getText().trim().isEmpty()) {
            return "Le champ Mode consultation est obligatoire.";
        }
        if (txtMaladie.getText() == null || txtMaladie.getText().trim().isEmpty()) {
            return "Le champ Maladie est obligatoire.";
        }
        if (txtDiagnostic.getText() == null || txtDiagnostic.getText().trim().isEmpty()) {
            return "Le champ Diagnostic est obligatoire.";
        }
        if (txtTraitement.getText() == null || txtTraitement.getText().trim().isEmpty()) {
            return "Le champ Traitement est obligatoire.";
        }
        if (txtExamensComplementaires.getText() == null || txtExamensComplementaires.getText().trim().isEmpty()) {
            return "Le champ Examens complementaires est obligatoire.";
        }
        if (txtNotesConsultation.getText() == null || txtNotesConsultation.getText().trim().isEmpty()) {
            return "Le champ Notes est obligatoire.";
        }
        if (txtCoutConsultation.getText() == null || txtCoutConsultation.getText().trim().isEmpty()) {
            return "Le champ Cout est obligatoire.";
        }
        return null;
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

