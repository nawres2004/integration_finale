package tn.esprit.suivie_nawres.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Consultation {
    private Integer utilisateurId;
    private String nom;
    private String prenom;
    private LocalDate dateConsultation;
    private LocalTime heureConsultation;
    private String modeConsultation;
    private String maladie;
    private String diagnostic;
    private String traitement;
    private String examensComplementaires;
    private String notesConsultation;
    private BigDecimal coutConsultation;

    public Consultation() {
    }

    public Consultation(Integer utilisateurId, String nom, String prenom, LocalDate dateConsultation, LocalTime heureConsultation,
                        String modeConsultation, String maladie, String diagnostic, String traitement,
                        String examensComplementaires, String notesConsultation, BigDecimal coutConsultation) {
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.prenom = prenom;
        this.dateConsultation = dateConsultation;
        this.heureConsultation = heureConsultation;
        this.modeConsultation = modeConsultation;
        this.maladie = maladie;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.examensComplementaires = examensComplementaires;
        this.notesConsultation = notesConsultation;
        this.coutConsultation = coutConsultation;
    }

    public Integer getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Integer utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateConsultation() {
        return dateConsultation;
    }

    public void setDateConsultation(LocalDate dateConsultation) {
        this.dateConsultation = dateConsultation;
    }

    public LocalTime getHeureConsultation() {
        return heureConsultation;
    }

    public void setHeureConsultation(LocalTime heureConsultation) {
        this.heureConsultation = heureConsultation;
    }

    public String getModeConsultation() {
        return modeConsultation;
    }

    public void setModeConsultation(String modeConsultation) {
        this.modeConsultation = modeConsultation;
    }

    public String getMaladie() {
        return maladie;
    }

    public void setMaladie(String maladie) {
        this.maladie = maladie;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getTraitement() {
        return traitement;
    }

    public void setTraitement(String traitement) {
        this.traitement = traitement;
    }

    public String getExamensComplementaires() {
        return examensComplementaires;
    }

    public void setExamensComplementaires(String examensComplementaires) {
        this.examensComplementaires = examensComplementaires;
    }

    public String getNotesConsultation() {
        return notesConsultation;
    }

    public void setNotesConsultation(String notesConsultation) {
        this.notesConsultation = notesConsultation;
    }

    public BigDecimal getCoutConsultation() {
        return coutConsultation;
    }

    public void setCoutConsultation(BigDecimal coutConsultation) {
        this.coutConsultation = coutConsultation;
    }
}

