package org.example.entities;

import java.time.LocalDateTime;

public class Article {
    private int idArticle;
    private String titre;
    private String contenu;
    private LocalDateTime dateCreation;
    private int idUtilisateur;
    private int idCategorie;

    public Article() {}

    public Article(int idArticle, String titre, String contenu, LocalDateTime dateCreation, int idUtilisateur, int idCategorie) {
        this.idArticle = idArticle;
        this.titre = titre;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.idUtilisateur = idUtilisateur;
        this.idCategorie = idCategorie;
    }

    public Article(String titre, String contenu, int idUtilisateur, int idCategorie) {
        this.titre = titre;
        this.contenu = contenu;
        this.idUtilisateur = idUtilisateur;
        this.idCategorie = idCategorie;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(int idCategorie) {
        this.idCategorie = idCategorie;
    }
}
