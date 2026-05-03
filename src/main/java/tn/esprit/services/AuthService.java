package tn.esprit.services;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.Date;

public class AuthService {

    private Connection cnx;

    public AuthService() {
        cnx = MyDbConnexion.getInstance().getCnx();
        ensureActiveColumnExists();
        ensurePasswordColumnLength();
        checkMissingColumns();
    }

    private void checkMissingColumns() {
        if (cnx == null) return;
        try {
            DatabaseMetaData dbm = cnx.getMetaData();
            ResultSet rs = dbm.getColumns(null, null, "utilisateur", null);
            System.out.println("--- DB Columns Check (utilisateur) ---");
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                String nullable = rs.getString("IS_NULLABLE");
                String def = rs.getString("COLUMN_DEF");
                if ("NO".equals(nullable) && def == null) {
                    System.out.println("  ⚠ " + col + ": NOT NULL, NO DEFAULT");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ checkMissingColumns: " + e.getMessage());
        }
    }

    // Ajoute isActive si elle n'existe pas
    private void ensureActiveColumnExists() {
        if (cnx == null) return;
        try {
            ResultSet cols = cnx.getMetaData().getColumns(null, null, "utilisateur", "isActive");
            if (!cols.next()) {
                cnx.createStatement().executeUpdate(
                    "ALTER TABLE utilisateur ADD COLUMN isActive TINYINT(1) NOT NULL DEFAULT 1");
                System.out.println("✅ Colonne isActive créée");
            }
        } catch (Exception e) {
            System.out.println("⚠ ensureActiveColumn: " + e.getMessage());
        }
    }

    // Agrandit motDePasse si besoin (60+ chars pour BCrypt)
    private void ensurePasswordColumnLength() {
        if (cnx == null) return;
        try {
            // 1. S'assurer que motDePasse existe et est assez long
            cnx.createStatement().executeUpdate(
                "ALTER TABLE utilisateur MODIFY COLUMN motDePasse VARCHAR(255)");
            
            // 2. Si la colonne snake_case 'mot_de_passe' existe, elle peut causer des erreurs 
            // de type "Field 'mot_de_passe' doesn't have a default value" lors de l'INSERT
            // si elle est NOT NULL. On la rend NULLABLE pour éviter ce conflit.
            try {
                cnx.createStatement().executeUpdate(
                    "ALTER TABLE utilisateur MODIFY COLUMN mot_de_passe VARCHAR(255) NULL");
                System.out.println("✅ Colonne mot_de_passe rendue facultative (nullable)");
            } catch (Exception e) {
                // La colonne n'existe probablement pas, ce qui est très bien.
            }

            System.out.println("✅ Colonne motDePasse vérifiée/agrandie");
        } catch (Exception e) {
            System.out.println("⚠ ensurePasswordLength: " + e.getMessage());
        }
    }

    // =========================
    // LOGIN
    // =========================
    public Utilisateur login(String email, String motDePasse) {
        if (cnx == null) {
            System.out.println("❌ LOGIN: connexion null");
            return null;
        }

        String sql = "SELECT * FROM utilisateur WHERE email = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Détecter le nom de la colonne du mot de passe
                String hashStored = null;
                try {
                    hashStored = rs.getString("motDePasse");
                } catch (Exception e) {
                    try { hashStored = rs.getString("mot_de_passe"); } 
                    catch (Exception e2) {
                        System.out.println("❌ LOGIN: Impossible de trouver la colonne mot de passe");
                        return null;
                    }
                }

                boolean passwordOk = false;

                // Vérifier si le hash est BCrypt valide ($2a$, $2b$, $2y$ et longueur >= 59)
                if (hashStored != null && hashStored.length() >= 59 && 
                    (hashStored.startsWith("$2a$") || hashStored.startsWith("$2b$") || hashStored.startsWith("$2y$"))) {
                    try {
                        // jBCrypt ne supporte que $2a$ — convertir $2y$ et $2b$ si nécessaire
                        String hashToCheck = hashStored;
                        if (hashStored.startsWith("$2y$") || hashStored.startsWith("$2b$")) {
                            hashToCheck = "$2a$" + hashStored.substring(4);
                        }
                        passwordOk = BCrypt.checkpw(motDePasse.trim(), hashToCheck);
                    } catch (Exception e) {
                        System.out.println("⚠ BCrypt check failed: " + e.getMessage());
                        System.out.println("DEBUG: Stored Hash info -> Length: " + hashStored.length() + " | Prefix: " + (hashStored.length() > 4 ? hashStored.substring(0, 4) : hashStored));
                        passwordOk = false;
                    }
                } else {
                    // Comparaison en clair (pour migration ou anciens comptes)
                    System.out.println("ℹ LOGIN: Hash non-BCrypt détecté, tentative fallback plain text");
                    passwordOk = (hashStored != null && motDePasse.trim().equals(hashStored));
                }

                if (!passwordOk) {
                    System.out.println("❌ LOGIN: mot de passe incorrect");
                    return null;
                }

                Utilisateur user = new Utilisateur();
                user.setIdUtilisateur(rs.getInt("idUtilisateur"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(hashStored);
                user.setTelephone(rs.getString("telephone"));
                user.setIdRole(rs.getInt("idRole"));
                user.setSpecialite(rs.getString("specialite"));
                user.setDateCreation(rs.getDate("dateCreation"));

                // Lire isActive
                try { user.setActive(rs.getBoolean("isActive")); }
                catch (Exception e) { user.setActive(true); }

                System.out.println("✅ LOGIN OK - " + user.getNom() + " | role=" + user.getIdRole() + " | active=" + user.isActive());
                return user;

            } else {
                System.out.println("❌ LOGIN: email introuvable");
                return null;
            }

        } catch (Exception e) {
            System.out.println("❌ LOGIN ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // =========================
    // REGISTER
    // =========================
    public boolean register(Utilisateur u) {
        System.out.println("=== REGISTER START: " + u.getNom() + " " + u.getPrenom() + " | role=" + u.getIdRole());

        if (cnx == null) {
            cnx = MyDbConnexion.getInstance().getCnx();
            if (cnx == null) {
                System.out.println("❌ REGISTER: connexion impossible");
                return false;
            }
        }

        try {
            // Hasher le mot de passe
            String hashedPassword = BCrypt.hashpw(u.getMotDePasse(), BCrypt.gensalt(12));

            // Détecter dynamiquement les colonnes présentes dans la table (uniquement pour la DB actuelle)
            DatabaseMetaData dbm = cnx.getMetaData();
            String catalog = cnx.getCatalog();
            ResultSet rsCols = dbm.getColumns(catalog, null, "utilisateur", null);
            java.util.Set<String> dbColumns = new java.util.HashSet<>();
            while (rsCols.next()) {
                dbColumns.add(rsCols.getString("COLUMN_NAME").toLowerCase().trim());
            }

            // Construire la requête dynamiquement
            StringBuilder columns = new StringBuilder("nom, prenom, email, telephone, idRole, specialite, dateCreation");
            StringBuilder values = new StringBuilder("?, ?, ?, ?, ?, ?, ?");
            java.util.List<Object> params = new java.util.ArrayList<>(java.util.Arrays.asList(
                u.getNom(), u.getPrenom(), u.getEmail(), u.getTelephone(), u.getIdRole(), u.getSpecialite(), new Timestamp(u.getDateCreation().getTime())
            ));

            // Gérer les doublons de colonnes mot de passe
            if (dbColumns.contains("motdepasse")) {
                columns.append(", motDePasse");
                values.append(", ?");
                params.add(hashedPassword);
            }
            if (dbColumns.contains("mot_de_passe")) {
                columns.append(", mot_de_passe");
                values.append(", ?");
                params.add(hashedPassword);
            }

            // Gérer les doublons de colonnes isActive
            if (dbColumns.contains("isactive")) {
                columns.append(", isActive");
                values.append(", ?");
                params.add(u.isActive());
            }
            if (dbColumns.contains("is_active")) {
                columns.append(", is_active");
                values.append(", ?");
                params.add(u.isActive());
            }

            // Gérer les colonnes de géolocalisation si elles existent
            if (dbColumns.contains("latitude")) {
                columns.append(", latitude");
                values.append(", ?");
                params.add(0.0); // Valeur par défaut
            }
            if (dbColumns.contains("longitude")) {
                columns.append(", longitude");
                values.append(", ?");
                params.add(0.0); // Valeur par défaut
            }

            String sql = "INSERT INTO utilisateur (" + columns.toString() + ") VALUES (" + values.toString() + ")";
            
            System.out.println("DEBUG: Executing Dynamic SQL: " + sql);
            PreparedStatement ps = cnx.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ REGISTER SUCCESS: " + rows + " row(s) inserted.");
                return true;
            } else {
                System.out.println("❌ REGISTER FAILED: No rows affected.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("❌ REGISTER ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // UPDATE PASSWORD
    // =========================
    public boolean updatePassword(int idUtilisateur, String nouveauMotDePasse) {
        if (cnx == null) return false;
        String hashed = BCrypt.hashpw(nouveauMotDePasse, BCrypt.gensalt(12));
        String sql = "UPDATE utilisateur SET motDePasse = ? WHERE idUtilisateur = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, hashed);
            ps.setInt(2, idUtilisateur);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("❌ UPDATE PASSWORD ERROR: " + e.getMessage());
            return false;
        }
    }
}
