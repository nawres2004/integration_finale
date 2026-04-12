package tn.esprit.suivie_nawres.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {
    private Integer utilisateurId;
    private String nom;
    private String prenom;
    private LocalDate dateRendezVous;
    private LocalTime heureRendezVous;
    private String priorite;
    private String modeConsultation;
    private StatutRendezVous statutRendezVous;
    private String notesRendezVous;
    private String pays;
    private String telephone;

    public RendezVous() {
    }

    public RendezVous(Integer utilisateurId, String nom, String prenom, LocalDate dateRendezVous, LocalTime heureRendezVous, String priorite,
                      String modeConsultation, StatutRendezVous statutRendezVous, String notesRendezVous,
                      String pays, String telephone) {
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.prenom = prenom;
        this.dateRendezVous = dateRendezVous;
        this.heureRendezVous = heureRendezVous;
        this.priorite = priorite;
        this.modeConsultation = modeConsultation;
        this.statutRendezVous = statutRendezVous;
        this.notesRendezVous = notesRendezVous;
        this.pays = pays;
        this.telephone = telephone;
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

    public LocalDate getDateRendezVous() {
        return dateRendezVous;
    }

    public void setDateRendezVous(LocalDate dateRendezVous) {
        this.dateRendezVous = dateRendezVous;
    }

    public LocalTime getHeureRendezVous() {
        return heureRendezVous;
    }

    public void setHeureRendezVous(LocalTime heureRendezVous) {
        this.heureRendezVous = heureRendezVous;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getModeConsultation() {
        return modeConsultation;
    }

    public void setModeConsultation(String modeConsultation) {
        this.modeConsultation = modeConsultation;
    }

    public StatutRendezVous getStatutRendezVous() {
        return statutRendezVous;
    }

    public void setStatutRendezVous(StatutRendezVous statutRendezVous) {
        this.statutRendezVous = statutRendezVous;
    }

    public String getNotesRendezVous() {
        return notesRendezVous;
    }

    public void setNotesRendezVous(String notesRendezVous) {
        this.notesRendezVous = notesRendezVous;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}

