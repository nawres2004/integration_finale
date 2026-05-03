package tn.esprit.dao;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO dédié à la réinitialisation de mot de passe.
 * Toutes les opérations DB sont isolées ici.
 */
public class PasswordResetDAO {

    private final Connection cnx;

    public PasswordResetDAO() {
        this.cnx = MyDbConnexion.getInstance().getCnx();
    }

    /**
     * Vérifie si un email existe ET que le compte est actif.
     * Ne retourne pas de détail pour éviter l'énumération d'emails.
     */
    public boolean emailExistsAndActive(String email) {
        if (cnx == null) return false;
        String sql = "SELECT idUtilisateur FROM utilisateur WHERE email = ? AND isActive = 1";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[PasswordResetDAO] emailExistsAndActive: " + e.getMessage());
            return false;
        }
    }

    /**
     * Enregistre le code en clair + expiration + reset des tentatives.
     */
    public boolean saveResetCode(String email, String rawCode) {
        if (cnx == null) return false;
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        String sql = "UPDATE utilisateur SET reset_code = ?, reset_code_expires_at = ?, " +
                     "reset_code_attempts = 0 WHERE email = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, rawCode);           // code 6 chiffres en clair
            ps.setTimestamp(2, Timestamp.valueOf(expiry));
            ps.setString(3, email.trim().toLowerCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PasswordResetDAO] saveResetCode: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie le code saisi contre le hash stocké.
     * Gère expiration et tentatives.
     * @return "OK" | "EXPIRED" | "MAX_ATTEMPTS" | "INVALID" | "ERROR"
     */
    public String verifyResetCode(String email, String rawCode) {
        if (cnx == null) return "ERROR";
        String sql = "SELECT reset_code, reset_code_expires_at, reset_code_attempts " +
                     "FROM utilisateur WHERE email = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return "ERROR";

            String storedHash = rs.getString("reset_code");
            Timestamp expiryTs = rs.getTimestamp("reset_code_expires_at");
            int attempts = rs.getInt("reset_code_attempts");

            if (storedHash == null || expiryTs == null) return "ERROR";

            // Trop de tentatives
            if (attempts >= 3) return "MAX_ATTEMPTS";

            // Code expiré
            if (expiryTs.toLocalDateTime().isBefore(LocalDateTime.now())) return "EXPIRED";

            // Vérification directe (code en clair)
            boolean match = rawCode.trim().equals(storedHash);
            if (!match) {
                incrementAttempts(email);
                return "INVALID";
            }

            return "OK";

        } catch (SQLException e) {
            System.err.println("[PasswordResetDAO] verifyResetCode: " + e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Incrémente le compteur de tentatives échouées.
     */
    public void incrementAttempts(String email) {
        if (cnx == null) return;
        String sql = "UPDATE utilisateur SET reset_code_attempts = reset_code_attempts + 1 WHERE email = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email.trim().toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PasswordResetDAO] incrementAttempts: " + e.getMessage());
        }
    }

    /**
     * Met à jour le mot de passe (hashé BCrypt) et efface toutes les données de reset.
     */
    public boolean updatePassword(String email, String newRawPassword) {
        if (cnx == null) return false;
        String hashed = BCrypt.hashpw(newRawPassword, BCrypt.gensalt(12));
        // Essayer d'abord motDePasse, puis mot_de_passe si échec
        String sql = "UPDATE utilisateur SET motDePasse = ?, " +
                     "reset_code = NULL, reset_code_expires_at = NULL, reset_code_attempts = 0 " +
                     "WHERE email = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, hashed);
            ps.setString(2, email.trim().toLowerCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Fallback sur mot_de_passe
            String sqlFallback = "UPDATE utilisateur SET mot_de_passe = ?, " +
                                 "reset_code = NULL, reset_code_expires_at = NULL, reset_code_attempts = 0 " +
                                 "WHERE email = ?";
            try {
                PreparedStatement ps = cnx.prepareStatement(sqlFallback);
                ps.setString(1, hashed);
                ps.setString(2, email.trim().toLowerCase());
                return ps.executeUpdate() > 0;
            } catch (SQLException e2) {
                System.err.println("[PasswordResetDAO] updatePassword: " + e2.getMessage());
                return false;
            }
        }
    }

    /**
     * Efface les données de reset (sécurité après succès ou abandon).
     */
    public void clearResetData(String email) {
        if (cnx == null) return;
        String sql = "UPDATE utilisateur SET reset_code = NULL, reset_code_expires_at = NULL, " +
                     "reset_code_attempts = 0 WHERE email = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email.trim().toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PasswordResetDAO] clearResetData: " + e.getMessage());
        }
    }
}
