package tn.esprit.services;

import tn.esprit.models.Role;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleService {

    private Connection cnx;

    public RoleService() {
        this.cnx = MyDbConnexion.getInstance().getCnx(); // ✅ FIX PRINCIPAL
    }

    // ✅ CREATE
    public void ajouter(Role r) {
        String sql = "INSERT INTO role (nomRole, description, dateCreation, statut) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getNomRole());
            ps.setString(2, r.getDescription());
            ps.setDate(3, new java.sql.Date(r.getDateCreation().getTime()));
            ps.setBoolean(4, r.isStatut());

            ps.executeUpdate();
            System.out.println(" Role ajout!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 
    public List<Role> afficher() {

        List<Role> list = new ArrayList<>();

        System.out.println("Tentative de connexion à la base de données...");
        System.out.println("Connexion: " + (cnx != null ? "OK" : "NULL"));

        // Si la connexion échoue, utiliser les données factices
        if (cnx == null) {
            System.out.println("MODE DÉMO: Utilisation des données factices (base non accessible)");
            
            // Créer les rôles selon votre base de données
            Role role1 = new Role(1, "ROLE_PATIENT", "Patient", new java.util.Date(), true);
            Role role2 = new Role(2, "ROLE_DONNATEUR", "Donateur", new java.util.Date(), true);
            Role role3 = new Role(3, "ROLE_MEDECIN", "Médecin", new java.util.Date(), true);
            Role role4 = new Role(4, "ROLE_ADMINISTRATEUR", "Administrateur", new java.util.Date(), true);
            Role role5 = new Role(5, "ROLE_MEDECIN_PENDING", "Médecin en attente", new java.util.Date(), true);
            
            list.add(role1);
            list.add(role2);
            list.add(role3);
            list.add(role4);
            list.add(role5);
            
            System.out.println("Rôles factices créés : " + list.size());
            return list;
        }

        String sql = "SELECT * FROM role";
        System.out.println("SQL: " + sql);

        try {
            Statement st = cnx.createStatement();
            System.out.println("Statement créé avec succès");
            
            ResultSet rs = st.executeQuery(sql);
            System.out.println("Requête exécutée avec succès");

            int count = 0;
            while (rs.next()) {
                count++;
                Role r = new Role();
                r.setIdRole(rs.getInt("idRole"));
                r.setNomRole(rs.getString("nomRole"));
                r.setDescription(rs.getString("description"));
                r.setDateCreation(rs.getDate("dateCreation"));
                
                // Gérer le statut comme VARCHAR ("actif"/"inactif")
                String statutStr = rs.getString("statut");
                r.setStatut("actif".equalsIgnoreCase(statutStr));

                list.add(r);
                System.out.println("Rôle #" + count + " trouvé: " + r.getNomRole() + " (ID: " + r.getIdRole() + ", Statut: " + statutStr + ")");
            }
            
            System.out.println("Nombre de lignes parcourues: " + count);

        } catch (Exception e) {
            System.out.println("ERREUR lors de la récupération des rôles: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Total des rôles chargés: " + list.size());
        return list;
    }

    // 
    public void modifier(Role r) {
        String sql = "UPDATE role SET nomRole=?, description=?, dateCreation=?, statut=? WHERE idRole=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getNomRole());
            ps.setString(2, r.getDescription());
            ps.setDate(3, new java.sql.Date(r.getDateCreation().getTime()));
            ps.setBoolean(4, r.isStatut());
            ps.setInt(5, r.getIdRole());

            ps.executeUpdate();
            System.out.println("✏️ Role modifié !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ DELETE
    public void supprimer(int id) {
        String sql = "DELETE FROM role WHERE idRole=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println(" Role supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //  GET ROLE NAME BY ID
    public String getRoleNameById(int idRole) {
        String sql = "SELECT nomRole FROM role WHERE idRole = ?";
        
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, idRole);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nomRole");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du nom du rôle: " + e.getMessage());
        }
        
        return null;
    }
}