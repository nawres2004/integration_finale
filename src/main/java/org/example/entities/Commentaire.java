package org.example.entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int idCommentaire;
    private String contenu;
    private LocalDateTime dateCreation;
    private int idArticle;
    private int idUtilisateur;

    public Commentaire() {}

    public Commentaire(int idCommentaire, String contenu, LocalDateTime dateCreation, int idArticle, int idUtilisateur) {
        this.idCommentaire = idCommentaire;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.idArticle = idArticle;
        this.idUtilisateur = idUtilisateur;
    }

    public Commentaire(String contenu, int idArticle, int idUtilisateur) {
        this.contenu = contenu;
        this.idArticle = idArticle;
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdCommentaire() { return idCommentaire; }
    public void setIdCommentaire(int idCommentaire) { this.idCommentaire = idCommentaire; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public int getIdArticle() { return idArticle; }
    public void setIdArticle(int idArticle) { this.idArticle = idArticle; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
}
