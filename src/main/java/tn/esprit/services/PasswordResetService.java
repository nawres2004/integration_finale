package tn.esprit.services;

import tn.esprit.dao.PasswordResetDAO;

import java.security.SecureRandom;

/**
 * Service de réinitialisation de mot de passe.
 * Orchestre DAO + EmailService avec logique sécurisée.
 */
public class PasswordResetService {

    private final PasswordResetDAO dao = new PasswordResetDAO();
    private final EmailService emailSvc = new EmailService();

    // ── Étape 1 : Demande de réinitialisation ───────────────
    /**
     * Génère et envoie un code. Toujours retourne un message générique
     * pour ne pas révéler si l'email existe.
     * 
     * @return true (toujours, pour ne pas énumérer les emails)
     */
    public boolean requestReset(String email) {
        // Message générique — ne jamais révéler si l'email existe
        if (!dao.emailExistsAndActive(email)) {
            System.out.println("[PasswordResetService] Email inexistant ou inactif (non révélé): " + email);
            return true; // On retourne true quand même (sécurité)
        }

        // Générer code 6 chiffres cryptographiquement sûr
        String code = generateSecureCode();

        // Sauvegarder le code hashé en DB
        boolean saved = dao.saveResetCode(email, code);
        if (!saved) {
            System.err.println("[PasswordResetService] Échec sauvegarde code pour: " + email);
            return false;
        }

        // Envoyer l'email
        boolean sent = emailSvc.sendResetCode(email, code);
        if (!sent) {
            System.err.println("[PasswordResetService] Échec envoi email pour: " + email);
            dao.clearResetData(email); // Nettoyer si email échoue
            return false;
        }

        System.out.println("[PasswordResetService] Code envoyé avec succès à: " + email);
        return true;
    }

    /**
     * @return message utilisateur (générique pour la sécurité)
     */
    public VerifyResult verifyCode(String email, String code) {
        String result = dao.verifyResetCode(email, code);
        return switch (result) {
            case "OK" -> VerifyResult.SUCCESS;
            case "EXPIRED" -> VerifyResult.EXPIRED;
            case "MAX_ATTEMPTS" -> VerifyResult.MAX_ATTEMPTS;
            case "INVALID" -> VerifyResult.INVALID;
            default -> VerifyResult.ERROR;
        };
    }

    // ── Étape 3 : Réinitialisation du mot de passe ──────────
    /**
     * @return true si succès
     */
    public boolean resetPassword(String email, String newPassword) {
        boolean ok = dao.updatePassword(email, newPassword);
        if (ok) {
            System.out.println("[PasswordResetService] Mot de passe réinitialisé pour: " + email);
        }
        return ok;
    }

    // ── Renvoi de code ──────────────────────────────────────
    public boolean resendCode(String email) {
        dao.clearResetData(email);
        return requestReset(email);
    }

    // ── Génération sécurisée ────────────────────────────────
    private String generateSecureCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    // ── Enum résultat vérification ──────────────────────────
    public enum VerifyResult {
        SUCCESS,
        EXPIRED,
        MAX_ATTEMPTS,
        INVALID,
        ERROR;

        /** Message générique affiché à l'utilisateur */
        public String getMessage() {
            return switch (this) {
                case SUCCESS -> "OK";
                case EXPIRED -> "Code expiré. Demandez un nouveau code.";
                case MAX_ATTEMPTS -> "Trop de tentatives. Demandez un nouveau code.";
                case INVALID -> "Code incorrect.";
                case ERROR -> "Une erreur est survenue. Réessayez.";
            };
        }
    }
}
