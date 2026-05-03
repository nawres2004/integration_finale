package org.example.services;

import org.example.entities.Commentaire;
import org.example.interfaces.IService;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements IService<Commentaire> {
    private Connection connection;

    public ServiceCommentaire() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Commentaire commentaire) {
        String query = "INSERT INTO commentaire (contenu, idArticle, idUtilisateur) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, commentaire.getContenu());
            pst.setInt(2, commentaire.getIdArticle());
            pst.setInt(3, commentaire.getIdUtilisateur());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding comment: " + e.getMessage());
        }
    }

    @Override
    public void update(Commentaire commentaire) {
        String query = "UPDATE commentaire SET contenu = ? WHERE idCommentaire = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, commentaire.getContenu());
            pst.setInt(2, commentaire.getIdCommentaire());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating comment: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM commentaire WHERE idCommentaire = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting comment: " + e.getMessage());
        }
    }

    @Override
    public List<Commentaire> getAll() {
        List<Commentaire> list = new ArrayList<>();
        String query = "SELECT * FROM commentaire ORDER BY dateCreation DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching comments: " + e.getMessage());
        }
        return list;
    }

    public List<Commentaire> getByArticle(int idArticle) {
        List<Commentaire> list = new ArrayList<>();
        String query = "SELECT * FROM commentaire WHERE idArticle = ? ORDER BY dateCreation ASC";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idArticle);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching comments by article: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Commentaire getOne(int id) {
        String query = "SELECT * FROM commentaire WHERE idCommentaire = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("Error fetching comment: " + e.getMessage());
        }
        return null;
    }

    private Commentaire mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("dateCreation");
        return new Commentaire(
                rs.getInt("idCommentaire"),
                rs.getString("contenu"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getInt("idArticle"),
                rs.getInt("idUtilisateur")
        );
    }

    public int getCommentsCounter(int idArticle) {
        String query = "SELECT COUNT(*) FROM commentaire WHERE idArticle = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idArticle);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching comments count: " + e.getMessage());
        }
        return 0;
    }
}
