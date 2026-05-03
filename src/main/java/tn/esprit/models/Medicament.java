package tn.esprit.models;

import java.time.LocalDate;

public class Medicament {

    private int id;
    private String nom;
    private String dosage;
    private String forme;
    private String frequence;
    private String contreIndications;
    private LocalDate dateExpiration;
    private int ordonnanceId;

    public Medicament() {}

    public Medicament(String nom, String dosage, String forme, String frequence, String contreIndications, LocalDate dateExpiration, int ordonnanceId) {
        this.nom               = nom;
        this.dosage            = dosage;
        this.forme             = forme;
        this.frequence         = frequence;
        this.contreIndications = contreIndications;
        this.dateExpiration    = dateExpiration;
        this.ordonnanceId      = ordonnanceId;
    }

    public Medicament(int id, String nom, String dosage, String forme, String frequence, String contreIndications, LocalDate dateExpiration, int ordonnanceId) {
        this.id                = id;
        this.nom               = nom;
        this.dosage            = dosage;
        this.forme             = forme;
        this.frequence         = frequence;
        this.contreIndications = contreIndications;
        this.dateExpiration    = dateExpiration;
        this.ordonnanceId      = ordonnanceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    public String getFrequence() {
        return frequence;
    }

    public void setFrequence(String frequence) {
        this.frequence = frequence;
    }

    public String getContreIndications() {
        return contreIndications;
    }

    public void setContreIndications(String contreIndications) {
        this.contreIndications = contreIndications;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public int getOrdonnanceId() {
        return ordonnanceId;
    }

    public void setOrdonnanceId(int ordonnanceId) {
        this.ordonnanceId = ordonnanceId;
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", dosage='" + dosage + '\'' +
                ", forme='" + forme + '\'' +
                ", frequence='" + frequence + '\'' +
                ", contreIndications='" + contreIndications + '\'' +
                ", dateExpiration=" + dateExpiration +
                ", ordonnanceId=" + ordonnanceId +
                '}';
    }
}