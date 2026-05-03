package org.example.services;

import org.example.entities.Article;
import org.example.interfaces.IService;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceArticle implements IService<Article> {
    private Connection connection;

    public ServiceArticle() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Article article) {
        String query = "INSERT INTO article (titre, contenu, idUtilisateur, idCategorie) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, article.getTitre());
            pst.setString(2, article.getContenu());
            pst.setInt(3, article.getIdUtilisateur());
            pst.setInt(4, article.getIdCategorie());
            pst.executeUpdate();
            System.out.println("Article added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding article: " + e.getMessage());
        }
    }

    @Override
    public void update(Article article) {
        String query = "UPDATE article SET titre = ?, contenu = ?, idCategorie = ? WHERE idArticle = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, article.getTitre());
            pst.setString(2, article.getContenu());
            pst.setInt(3, article.getIdCategorie());
            pst.setInt(4, article.getIdArticle());
            pst.executeUpdate();
            System.out.println("Article updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating article: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM article WHERE idArticle = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Article deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting article: " + e.getMessage());
        }
    }

    @Override
    public List<Article> getAll() {
        List<Article> list = new ArrayList<>();
        String query = "SELECT * FROM article";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Article(
                        rs.getInt("idArticle"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getTimestamp("dateCreation").toLocalDateTime(),
                        rs.getInt("idUtilisateur"),
                        rs.getInt("idCategorie")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching articles: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Article getOne(int id) {
        String query = "SELECT * FROM article WHERE idArticle = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Article(
                        rs.getInt("idArticle"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getTimestamp("dateCreation").toLocalDateTime(),
                        rs.getInt("idUtilisateur"),
                        rs.getInt("idCategorie")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching article: " + e.getMessage());
        }
        return null;
    }
}
