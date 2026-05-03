package org.example.entities;

public class Categorie {
    private int idCategorie;
    private String nom;
    private String description;
    private boolean allowPatientsToPost;
    private boolean allowDoctorsToPost;

    public Categorie() {}

    public Categorie(int idCategorie, String nom, String description, boolean allowPatientsToPost, boolean allowDoctorsToPost) {
        this.idCategorie = idCategorie;
        this.nom = nom;
        this.description = description;
        this.allowPatientsToPost = allowPatientsToPost;
        this.allowDoctorsToPost = allowDoctorsToPost;
    }

    public Categorie(String nom, String description, boolean allowPatientsToPost, boolean allowDoctorsToPost) {
        this.nom = nom;
        this.description = description;
        this.allowPatientsToPost = allowPatientsToPost;
        this.allowDoctorsToPost = allowDoctorsToPost;
    }

    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAllowPatientsToPost() {
        return allowPatientsToPost;
    }

    public void setAllowPatientsToPost(boolean allowPatientsToPost) {
        this.allowPatientsToPost = allowPatientsToPost;
    }

    public boolean isAllowDoctorsToPost() {
        return allowDoctorsToPost;
    }

    public void setAllowDoctorsToPost(boolean allowDoctorsToPost) {
        this.allowDoctorsToPost = allowDoctorsToPost;
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "idCategorie=" + idCategorie +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", allowPatientsToPost=" + allowPatientsToPost +
                ", allowDoctorsToPost=" + allowDoctorsToPost +
                '}';
    }
}
