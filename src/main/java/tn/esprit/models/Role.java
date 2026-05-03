package tn.esprit.models;

import java.util.Date;

public class Role {

    private int idRole;
    private String nomRole;
    private String description;
    private Date dateCreation;
    private boolean statut;

    // Constructeur vide
    public Role() {}

    // Constructeur sans id
    public Role(String nomRole, String description, Date dateCreation, boolean statut) {
        this.nomRole = nomRole;
        this.description = description;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // Constructeur avec id
    public Role(int idRole, String nomRole, String description, Date dateCreation, boolean statut) {
        this.idRole = idRole;
        this.nomRole = nomRole;
        this.description = description;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // Getters & Setters

    public int getIdRole() {
        return idRole;
    }

    public void setIdRole(int idRole) {
        this.idRole = idRole;
    }

    public String getNomRole() {
        return nomRole;
    }

    public void setNomRole(String nomRole) {
        this.nomRole = nomRole;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isStatut() {
        return statut;
    }

    public void setStatut(boolean statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return  nomRole ;


    }


}