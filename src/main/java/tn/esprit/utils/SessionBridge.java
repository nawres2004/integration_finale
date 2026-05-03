package tn.esprit.utils;

import tn.esprit.models.Utilisateur;
import tn.esprit.services.SessionService;
import org.example.entities.User;
import org.example.utils.UserSession;

public class SessionBridge {
    public static void sync() {
        Utilisateur current = SessionService.getInstance().getCurrentUser();
        if (current != null) {
            User user = new User();
            user.setIdUtilisateur(current.getIdUtilisateur());
            user.setNom(current.getNom());
            user.setPrenom(current.getPrenom());
            user.setEmail(current.getEmail());
            user.setMotDePasse(current.getMotDePasse());
            user.setIdRole(current.getIdRole());
            
            UserSession.getInstance(user);
        }
    }
}
