package tn.esprit.models;

import java.util.Date;

public class Utilisateur {

    private int idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private int idRole;
    private String specialite;
    private Date dateCreation;
    private boolean isActive;
    private double latitude;
    private double longitude;

    // Constructeur vide
    public Utilisateur() {}

    // Constructeurs compatibilité
    public Utilisateur(String nom) {
        this.nom = nom;
    }

    public Utilisateur(int idUtilisateur, String nom) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
    }

    // Constructeur sans id
    public Utilisateur(String nom, String prenom, String email, String motDePasse,
                       String telephone, int idRole, String specialite,
                       Date dateCreation, boolean isActive) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.idRole = idRole;
        this.specialite = specialite;
        this.dateCreation = dateCreation;
        this.isActive = isActive;
    }

    // Constructeur avec id
    public Utilisateur(int idUtilisateur, String nom, String prenom, String email, String motDePasse,
                       String telephone, int idRole, String specialite,
                       Date dateCreation, boolean isActive) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.idRole = idRole;
        this.specialite = specialite;
        this.dateCreation = dateCreation;
        this.isActive = isActive;
    }

    // Getters & Setters

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getIdRole() {
        return idRole;
    }

    public void setIdRole(int idRole) {
        this.idRole = idRole;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", idRole=" + idRole +
                ", specialite='" + specialite + '\'' +
                ", dateCreation=" + dateCreation +
                ", isActive=" + isActive +
                '}';
    }
}