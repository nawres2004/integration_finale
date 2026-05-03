package tn.esprit.suivie_nawres.utils;

public final class RoleContext {
    private static UserRole currentRole;
    private static Integer currentUtilisateurId;

    private RoleContext() {
    }

    public static UserRole getCurrentRole() {
        return currentRole;
    }

    public static void setCurrentRole(UserRole role) {
        currentRole = role;
    }

    public static Integer getCurrentUtilisateurId() {
        return currentUtilisateurId;
    }

    public static void setCurrentUtilisateurId(Integer utilisateurId) {
        currentUtilisateurId = utilisateurId;
    }
}

