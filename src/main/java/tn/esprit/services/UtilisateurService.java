package tn.esprit.services;

import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private Connection cnx;

    public UtilisateurService() {
        this.cnx = MyDbConnexion.getInstance().getCnx();
    }

    // ✅ CREATE
    public void ajouter(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, motDePasse, telephone, idRole, specialite, dateCreation, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getTelephone());
            ps.setInt(6, u.getIdRole());
            ps.setString(7, u.getSpecialite());
            ps.setDate(8, new java.sql.Date(u.getDateCreation().getTime()));
            ps.setBoolean(9, u.isActive());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ALL
    public List<Utilisateur> afficher() {
        List<Utilisateur> list = new ArrayList<>();
        if (cnx == null) {
            System.out.println("❌ afficher(): connexion NULL");
            return list;
        }
        String sql = "SELECT * FROM utilisateur";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
                Utilisateur u = mapRow(rs);
                list.add(u);
                System.out.println("✅ User #" + count + ": " + u.getNom() + " " + u.getPrenom()
                        + " | role=" + u.getIdRole() + " | active=" + u.isActive());
            }
            System.out.println("✅ afficher() total: " + count + " utilisateurs");
        } catch (SQLException e) {
            System.out.println("❌ afficher() erreur: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // ✅ GET MEDECINS EN ATTENTE (isActive = false, idRole = 3)
    public List<Utilisateur> getMedecinsEnAttente() {
        List<Utilisateur> list = new ArrayList<>();
        if (cnx == null)
            return list;
        String sql = "SELECT * FROM utilisateur WHERE idRole = 3 AND isActive = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getMedecinsEnAttente: " + e.getMessage());
        }
        return list;
    }

    // ✅ GET ALL MEDECINS
    public List<Utilisateur> getAllMedecins() {
        List<Utilisateur> list = new ArrayList<>();
        if (cnx == null)
            return list;
        String sql = "SELECT * FROM utilisateur WHERE idRole = 3";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAllMedecins: " + e.getMessage());
        }
        return list;
    }

    // ✅ ACTIVER / DESACTIVER UN UTILISATEUR
    public boolean setActiveStatus(int idUtilisateur, boolean active) {
        if (cnx == null)
            return false;
        String sql = "UPDATE utilisateur SET isActive = ? WHERE idUtilisateur = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setBoolean(1, active);
            ps.setInt(2, idUtilisateur);
            int rows = ps.executeUpdate();
            System.out.println("✅ setActiveStatus: id=" + idUtilisateur + " active=" + active + " rows=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ setActiveStatus: " + e.getMessage());
            return false;
        }
    }

    // ✅ UPDATE PROFILE (nom, prenom, telephone)
    public boolean updateProfile(Utilisateur u) {
        if (cnx == null)
            return false;
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, telephone=? WHERE idUtilisateur=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getTelephone());
            ps.setInt(4, u.getIdUtilisateur());
            int rows = ps.executeUpdate();
            System.out.println("✅ updateProfile: " + rows + " ligne(s)");
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ updateProfile: " + e.getMessage());
            return false;
        }
    }

    // ✅ UPDATE
    public void modifier(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, motDePasse=?, telephone=?, idRole=?, specialite=?, isActive=? WHERE idUtilisateur=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getTelephone());
            ps.setInt(6, u.getIdRole());
            ps.setString(7, u.getSpecialite());
            ps.setBoolean(8, u.isActive());
            ps.setInt(9, u.getIdUtilisateur());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    public void supprimer(int id) {
        if (cnx == null)
            return;
        String sql = "DELETE FROM utilisateur WHERE idUtilisateur=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Helper: mapper un ResultSet vers Utilisateur
    // ✅ GET MAIL BY ID
    public String getMailById(int id) {
        if (cnx == null) return null;
        String sql = "SELECT email FROM utilisateur WHERE idUtilisateur = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.out.println("Erreur getMailById: " + e.getMessage());
        }
        return null;
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getInt("idUtilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setIdRole(rs.getInt("idRole"));
        u.setSpecialite(rs.getString("specialite"));
        u.setDateCreation(rs.getDate("dateCreation"));

        // mot de passe — essayer les deux conventions de nommage
        String pwd = null;
        try {
            pwd = rs.getString("motDePasse");
        } catch (SQLException e) {
            try {
                pwd = rs.getString("mot_de_passe");
            } catch (SQLException e2) {
                System.out.println("⚠ mapRow: Colonne mot de passe introuvable");
            }
        }
        u.setMotDePasse(pwd);

        // isActive — parcourir les colonnes du ResultSet pour trouver le bon nom
        boolean active = false;
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String colName = meta.getColumnName(i);
            if (colName.equalsIgnoreCase("isActive") || colName.equalsIgnoreCase("is_active")) {
                active = rs.getBoolean(i);
                break;
            }
        }
        u.setActive(active);

        return u;
    }
}
