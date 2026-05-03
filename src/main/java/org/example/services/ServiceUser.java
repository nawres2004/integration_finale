package org.example.services;

import org.example.entities.User;
import org.example.utils.MyDatabase;

import java.sql.*;

public class ServiceUser {
    private Connection connection;

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public User authenticate(String email, String password) {
        String query = "SELECT * FROM utilisateur WHERE email = ? AND motDePasse = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("motDePasse"),
                    rs.getInt("idRole")
                );
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    public void seedDatabase() {
        try {
            // 1. Seed Roles if needed
            seedRole(1, "ADMIN");
            seedRole(2, "PATIENT");
            seedRole(3, "DOCTOR");

            // 2. Seed Users if needed
            seedUser("Admin", "User", "admin@vitaplus.com", "admin123", 1);
            seedUser("Patient", "User", "patient@vitaplus.com", "patient123", 2);
            seedUser("Doctor", "User", "doctor@vitaplus.com", "doctor123", 3);
            
            System.out.println("Database seeded successfully!");
        } catch (SQLException e) {
            System.out.println("Seeding error: " + e.getMessage());
        }
    }

    private void seedRole(int id, String name) throws SQLException {
        String check = "SELECT idRole FROM role WHERE idRole = ?";
        PreparedStatement cpst = connection.prepareStatement(check);
        cpst.setInt(1, id);
        if (!cpst.executeQuery().next()) {
            String insert = "INSERT INTO role (idRole, nomRole) VALUES (?, ?)";
            PreparedStatement ipst = connection.prepareStatement(insert);
            ipst.setInt(1, id);
            ipst.setString(2, name);
            ipst.executeUpdate();
        }
    }

    private void seedUser(String nom, String prenom, String email, String pass, int roleId) throws SQLException {
        String check = "SELECT idUtilisateur FROM utilisateur WHERE email = ?";
        PreparedStatement cpst = connection.prepareStatement(check);
        cpst.setString(1, email);
        if (!cpst.executeQuery().next()) {
            String insert = "INSERT INTO utilisateur (nom, prenom, email, motDePasse, idRole) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ipst = connection.prepareStatement(insert);
            ipst.setString(1, nom);
            ipst.setString(2, prenom);
            ipst.setString(3, email);
            ipst.setString(4, pass);
            ipst.setInt(5, roleId);
            ipst.executeUpdate();
        }
    }

    public User getUserById(int id) {
        String query = "SELECT * FROM utilisateur WHERE idUtilisateur = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("motDePasse"),
                    rs.getInt("idRole")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
}
