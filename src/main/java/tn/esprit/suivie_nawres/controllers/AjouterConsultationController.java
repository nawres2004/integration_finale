package tn.esprit.suivie_nawres.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.services.ConsultationService;

import java.math.BigDecimal;
import java.time.LocalTime;

public class AjouterConsultationController {
    private static Consultation consultationAEditer;
    private static AfficherConsultationController parentConsultationController;

    @FXML private TextField txtUtilisateurId;
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
            Consultation consultation = new Consultation();
            consultation.setUtilisateurId(Integer.parseInt(txtUtilisateurId.getText().trim()));
            consultation.setDateConsultation(datePickerConsultation.getValue());
            consultation.setHeureConsultation(parseHeure(txtHeureConsultation.getText()));
            consultation.setModeConsultation(txtModeConsultation.getText());
            consultation.setMaladie(txtMaladie.getText());
            consultation.setDiagnostic(txtDiagnostic.getText());
            consultation.setTraitement(txtTraitement.getText());
            consultation.setExamensComplementaires(txtExamensComplementaires.getText());
            consultation.setNotesConsultation(txtNotesConsultation.getText());
            consultation.setCoutConsultation(parseCout(txtCoutConsultation.getText()));

            if (consultationService.existe(consultation.getUtilisateurId())) {
                consultationService.modifierConsultation(consultation);
                afficherMessage("Consultation modifiée avec succès.");
            } else {
                consultationService.ajouterConsultation(consultation);
                afficherMessage("Consultation ajoutée avec succès.");
            }
            if (parentConsultationController != null) {
                parentConsultationController.rafraichirListe();
            }
            vider();
        } catch (Exception exception) {
            afficherMessage("Erreur : " + exception.getMessage());
        }
    }

    @FXML
    private void vider() {
        txtUtilisateurId.clear();
        txtUtilisateurId.setDisable(false);
        datePickerConsultation.setValue(null);
        txtHeureConsultation.clear();
        txtModeConsultation.clear();
        txtMaladie.clear();
        txtDiagnostic.clear();
        txtTraitement.clear();
        txtExamensComplementaires.clear();
        txtNotesConsultation.clear();
        txtCoutConsultation.clear();
        afficherMessage("Prêt.");
    }

    private void remplirFormulaire(Consultation consultation) {
        txtUtilisateurId.setText(String.valueOf(consultation.getUtilisateurId()));
        datePickerConsultation.setValue(consultation.getDateConsultation());
        txtHeureConsultation.setText(consultation.getHeureConsultation() == null ? "" : consultation.getHeureConsultation().toString());
        txtModeConsultation.setText(consultation.getModeConsultation());
        txtMaladie.setText(consultation.getMaladie());
        txtDiagnostic.setText(consultation.getDiagnostic());
        txtTraitement.setText(consultation.getTraitement());
        txtExamensComplementaires.setText(consultation.getExamensComplementaires());
        txtNotesConsultation.setText(consultation.getNotesConsultation());
        txtCoutConsultation.setText(consultation.getCoutConsultation() == null ? "" : consultation.getCoutConsultation().toString());
        afficherMessage("Mode modification actif.");
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

    private void afficherMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }
}

