package tn.esprit.models;

import java.sql.Date;

public class Don {

    private int id;
    private double montant;
    private Date dateDon;
    private String modePaiement;
    private String emailDonateur;
    private String messageSoutien;
    private int projetId;
    private String nomDonateur;
    private String prenomDonateur;
    private String flouciPaymentId;
    private String paymentStatus;

    public Don() {
    }
    // Constructeur sans id (INSERT) pour ajouter un projet car MySQL crée l’id automatiquement
    public Don(double montant, Date dateDon, String modePaiement, String emailDonateur,
               String messageSoutien, int projetId, String nomDonateur,
               String prenomDonateur, String flouciPaymentId, String paymentStatus) {

        this.montant = montant;
        this.dateDon = dateDon;
        this.modePaiement = modePaiement;
        this.emailDonateur = emailDonateur;
        this.messageSoutien = messageSoutien;
        this.projetId = projetId;
        this.nomDonateur = nomDonateur;
        this.prenomDonateur = prenomDonateur;
        this.flouciPaymentId = flouciPaymentId;
        this.paymentStatus = paymentStatus;
    }
    // Constructeur avec id (SELECT) pour récupérer un projet (contient déja un id)
    public Don(int id, double montant, Date dateDon, String modePaiement, String emailDonateur,
               String messageSoutien, int projetId, String nomDonateur,
               String prenomDonateur, String flouciPaymentId, String paymentStatus) {

        this.id = id;
        this.montant = montant;
        this.dateDon = dateDon;
        this.modePaiement = modePaiement;
        this.emailDonateur = emailDonateur;
        this.messageSoutien = messageSoutien;
        this.projetId = projetId;
        this.nomDonateur = nomDonateur;
        this.prenomDonateur = prenomDonateur;
        this.flouciPaymentId = flouciPaymentId;
        this.paymentStatus = paymentStatus;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Date getDateDon() {
        return dateDon;
    }

    public void setDateDon(Date dateDon) {
        this.dateDon = dateDon;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getEmailDonateur() {
        return emailDonateur;
    }

    public void setEmailDonateur(String emailDonateur) {
        this.emailDonateur = emailDonateur;
    }

    public String getMessageSoutien() {
        return messageSoutien;
    }

    public void setMessageSoutien(String messageSoutien) {
        this.messageSoutien = messageSoutien;
    }

    public int getProjetId() {
        return projetId;
    }

    public void setProjetId(int projetId) {
        this.projetId = projetId;
    }

    public String getNomDonateur() {
        return nomDonateur;
    }

    public void setNomDonateur(String nomDonateur) {
        this.nomDonateur = nomDonateur;
    }

    public String getPrenomDonateur() {
        return prenomDonateur;
    }

    public void setPrenomDonateur(String prenomDonateur) {
        this.prenomDonateur = prenomDonateur;
    }

    public String getFlouciPaymentId() {
        return flouciPaymentId;
    }

    public void setFlouciPaymentId(String flouciPaymentId) {
        this.flouciPaymentId = flouciPaymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "Don{" +
                "id=" + id +
                ", montant=" + montant +
                ", dateDon=" + dateDon +
                ", modePaiement='" + modePaiement + '\'' +
                ", emailDonateur='" + emailDonateur + '\'' +
                ", messageSoutien='" + messageSoutien + '\'' +
                ", projetId=" + projetId +
                ", nomDonateur='" + nomDonateur + '\'' +
                ", prenomDonateur='" + prenomDonateur + '\'' +
                ", flouciPaymentId='" + flouciPaymentId + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                '}';
    }
}