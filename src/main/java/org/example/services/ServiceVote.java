package org.example.services;

import org.example.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServiceVote {
    private Connection connection;

    public ServiceVote() {
        connection = MyDatabase.getInstance().getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS article_vote (" +
                "idVote INT AUTO_INCREMENT PRIMARY KEY, " +
                "idArticle INT NOT NULL, " +
                "idUtilisateur INT NOT NULL, " +
                "voteType INT NOT NULL, " +
                "UNIQUE KEY unique_vote (idArticle, idUtilisateur), " +
                "FOREIGN KEY (idArticle) REFERENCES article(idArticle) ON DELETE CASCADE, " +
                "FOREIGN KEY (idUtilisateur) REFERENCES utilisateur(idUtilisateur) ON DELETE CASCADE" +
                ")";
        try (Statement st = connection.createStatement()) {
            st.execute(query);
            System.out.println("Table article_vote verified.");
        } catch (SQLException e) {
            System.out.println("Error verifying/creating article_vote table: " + e.getMessage());
        }
    }

    // Return the number of Likes (voteType = 1)
    public int getLikesCounter(int idArticle) {
        String query = "SELECT COUNT(*) as total FROM article_vote WHERE idArticle = ? AND voteType = 1";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idArticle);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error getting likes: " + e.getMessage());
        }
        return 0;
    }

    // Return the number of Dislikes (voteType = -1)
    public int getDislikesCounter(int idArticle) {
        String query = "SELECT COUNT(*) as total FROM article_vote WHERE idArticle = ? AND voteType = -1";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idArticle);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error getting dislikes: " + e.getMessage());
        }
        return 0;
    }

    // Return the current vote for a specific user on an article
    public int getUserVote(int idArticle, int idUtilisateur) {
        String query = "SELECT voteType FROM article_vote WHERE idArticle = ? AND idUtilisateur = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idArticle);
            pst.setInt(2, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("voteType"); // will be 1 or -1
            }
        } catch (SQLException e) {
            System.out.println("Error getting user vote: " + e.getMessage());
        }
        return 0; // means the user has not voted
    }

    // Cast or toggle a vote
    public void castVote(int idArticle, int idUtilisateur, int voteType) {
        int currentVote = getUserVote(idArticle, idUtilisateur);
        
        if (currentVote == 0) {
            // No vote exists, insert a new one
            String query = "INSERT INTO article_vote (idArticle, idUtilisateur, voteType) VALUES (?, ?, ?)";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, idArticle);
                pst.setInt(2, idUtilisateur);
                pst.setInt(3, voteType);
                pst.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error inserting vote: " + e.getMessage());
            }
        } else if (currentVote == voteType) {
            // User clicked the same vote button, so we remove the vote (toggle off)
            String query = "DELETE FROM article_vote WHERE idArticle = ? AND idUtilisateur = ?";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, idArticle);
                pst.setInt(2, idUtilisateur);
                pst.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error removing vote: " + e.getMessage());
            }
        } else {
            // User clicked a different vote button, update the existing vote
            String query = "UPDATE article_vote SET voteType = ? WHERE idArticle = ? AND idUtilisateur = ?";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, voteType);
                pst.setInt(2, idArticle);
                pst.setInt(3, idUtilisateur);
                pst.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error updating vote: " + e.getMessage());
            }
        }
    }
}
