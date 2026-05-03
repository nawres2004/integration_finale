package org.example.entities;

public class ArticleVote {
    private int idVote;
    private int idArticle;
    private int idUtilisateur;
    private int voteType; // 1 for Like, -1 for Dislike, 0 for None (optional)

    public ArticleVote() {}

    public ArticleVote(int idVote, int idArticle, int idUtilisateur, int voteType) {
        this.idVote = idVote;
        this.idArticle = idArticle;
        this.idUtilisateur = idUtilisateur;
        this.voteType = voteType;
    }

    public ArticleVote(int idArticle, int idUtilisateur, int voteType) {
        this.idArticle = idArticle;
        this.idUtilisateur = idUtilisateur;
        this.voteType = voteType;
    }

    public int getIdVote() {
        return idVote;
    }

    public void setIdVote(int idVote) {
        this.idVote = idVote;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getVoteType() {
        return voteType;
    }

    public void setVoteType(int voteType) {
        this.voteType = voteType;
    }
}
