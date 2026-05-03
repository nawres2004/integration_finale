package tn.esprit.services;

import tn.esprit.models.Utilisateur;

public class SessionService {
    
    private static SessionService instance;
    private Utilisateur currentUser;
    
    private SessionService() {}
    
    public static SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }
    
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }
    
    public Utilisateur getCurrentUser() {
        return currentUser;
    }
    
    public void clearSession() {
        this.currentUser = null;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
