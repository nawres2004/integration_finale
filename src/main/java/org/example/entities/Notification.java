package org.example.entities;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int idUtilisateur; // ID of the user who receives the notification
    private String titre;
    private String message;
    private String type;
    private String link;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification() {
    }

    public Notification(String titre, String message, int idUtilisateur, String type, String link) {
        this.titre = titre;
        this.message = message;
        this.idUtilisateur = idUtilisateur;
        this.type = type;
        this.link = link;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(int id, int idUtilisateur, String titre, String message, String type, String link, boolean isRead, LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.idUtilisateur = idUtilisateur;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.link = link;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
