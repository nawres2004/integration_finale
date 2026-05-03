package tn.esprit.models;

import java.sql.Date;

public class Projet {

    private int id;
    private String titreProjet;
    private String description;
    private double objectifFinancier;
    private double montantCollecte;
    private Date dateDebut;
    private Date dateFin;

    // Constructeur vide
    public Projet() {
    }


    public Projet(String titreProjet, String description,
                  double objectifFinancier,
                  Date dateDebut, Date dateFin) {
        this.titreProjet = titreProjet;
        this.description = description;
        this.objectifFinancier = objectifFinancier;
        this.montantCollecte = 0.0;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Constructeur sans id (INSERT) pour ajouter un projet car MySQL crée l’id automatiquement
    public Projet(String titreProjet, String description, double objectifFinancier,
                  double montantCollecte, Date dateDebut, Date dateFin) {

        this.titreProjet = titreProjet;
        this.description = description;
        this.objectifFinancier = objectifFinancier;
        this.montantCollecte = montantCollecte;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Constructeur avec id (SELECT) pour récupérer un projet (contient déja un id)
    public Projet(int id, String titreProjet, String description, double objectifFinancier,
                  double montantCollecte, Date dateDebut, Date dateFin) {

        this.id = id;
        this.titreProjet = titreProjet;
        this.description = description;
        this.objectifFinancier = objectifFinancier;
        this.montantCollecte = montantCollecte;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // GETTERS & SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreProjet() {
        return titreProjet;
    }

    public void setTitreProjet(String titreProjet) {
        this.titreProjet = titreProjet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getObjectifFinancier() {
        return objectifFinancier;
    }

    public void setObjectifFinancier(double objectifFinancier) {
        this.objectifFinancier = objectifFinancier;
    }

    public double getMontantCollecte() {
        return montantCollecte;
    }

    public void setMontantCollecte(double montantCollecte) {
        this.montantCollecte = montantCollecte;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    // toString
    @Override
    public String toString() {
        return "Projet{" +
                "id=" + id +
                ", titreProjet='" + titreProjet + '\'' +
                ", description='" + description + '\'' +
                ", objectifFinancier=" + objectifFinancier +
                ", montantCollecte=" + montantCollecte +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                '}';
    }
}