package tn.esprit.models;

import java.time.LocalDate;

public class Ordonnance {

    private int id;
    private LocalDate dateOrdonnance;
    private String instructions;
    private String dureeTraitement;
    private int idUtilisateur;
    private String nomUtilisateur;
    private boolean isSeen;

    public Ordonnance() {}

    public Ordonnance(LocalDate dateOrdonnance, String instructions, String dureeTraitement, int idUtilisateur, boolean isSeen) {
        this.dateOrdonnance = dateOrdonnance;
        this.instructions = instructions;
        this.dureeTraitement = dureeTraitement;
        this.idUtilisateur = idUtilisateur;
        this.isSeen = isSeen;
    }

    public Ordonnance(LocalDate dateOrdonnance, String instructions, String dureeTraitement, int idUtilisateur, String nomUtilisateur, boolean isSeen) {
        this.dateOrdonnance = dateOrdonnance;
        this.instructions = instructions;
        this.dureeTraitement = dureeTraitement;
        this.idUtilisateur = idUtilisateur;
        this.nomUtilisateur = nomUtilisateur;
        this.isSeen = isSeen;
    }

    public Ordonnance(int id, LocalDate dateOrdonnance, String instructions, String dureeTraitement, int idUtilisateur, String nomUtilisateur, boolean isSeen) {
        this.id              = id;
        this.dateOrdonnance  = dateOrdonnance;
        this.instructions    = instructions;
        this.dureeTraitement = dureeTraitement;
        this.idUtilisateur   = idUtilisateur;
        this.nomUtilisateur  = nomUtilisateur;
        this.isSeen          = isSeen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateOrdonnance() {
        return dateOrdonnance;
    }

    public void setDateOrdonnance(LocalDate dateOrdonnance) {
        this.dateOrdonnance = dateOrdonnance;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDureeTraitement() {
        return dureeTraitement;
    }

    public void setDureeTraitement(String dureeTraitement) {
        this.dureeTraitement = dureeTraitement;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", dateOrdonnance=" + dateOrdonnance +
                ", instructions='" + instructions + '\'' +
                ", dureeTraitement='" + dureeTraitement + '\'' +
                ", idUtilisateur=" + idUtilisateur +
                ", isSeen=" + isSeen +
                '}';
    }
}