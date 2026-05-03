package org.example.services;

import org.example.entities.Categorie;
import org.example.interfaces.IService;
import org.example.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements IService<Categorie> {
    private Connection connection;

    public ServiceCategorie() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Categorie categorie) {
        String query = "INSERT INTO categorie (nom, description, allow_patients_to_post, allow_doctors_to_post) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, categorie.getNom());
            preparedStatement.setString(2, categorie.getDescription());
            preparedStatement.setBoolean(3, categorie.isAllowPatientsToPost());
            preparedStatement.setBoolean(4, categorie.isAllowDoctorsToPost());
            preparedStatement.executeUpdate();
            System.out.println("Categorie added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding Categorie: " + e.getMessage());
        }
    }

    @Override
    public void update(Categorie categorie) {
        // To be implemented if needed
    }

    @Override
    public void delete(int id) {
        // To be implemented if needed
    }

    @Override
    public List<Categorie> getAll() {
        List<Categorie> list = new ArrayList<>();
        String query = "SELECT * FROM categorie";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Categorie(
                        rs.getInt("idCategorie"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getBoolean("allow_patients_to_post"),
                        rs.getBoolean("allow_doctors_to_post")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching Categories: " + e.getMessage());
        }
        return list;
    }

    public List<Categorie> getAllByRole(int roleId) {
        if (roleId == 1) return getAll(); // Admin sees all

        List<Categorie> list = new ArrayList<>();
        String column = (roleId == 2) ? "allow_patients_to_post" : "allow_doctors_to_post";
        String query = "SELECT * FROM categorie WHERE " + column + " = 1";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Categorie(
                        rs.getInt("idCategorie"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getBoolean("allow_patients_to_post"),
                        rs.getBoolean("allow_doctors_to_post")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching filtered Categories: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Categorie getOne(int id) {
        String query = "SELECT * FROM categorie WHERE idCategorie = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Categorie(
                        rs.getInt("idCategorie"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getBoolean("allow_patients_to_post"),
                        rs.getBoolean("allow_doctors_to_post")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching category: " + e.getMessage());
        }
        return null;
    }
}
