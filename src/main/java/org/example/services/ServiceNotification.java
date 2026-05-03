package org.example.services;

import org.example.entities.Notification;
import org.example.interfaces.IService;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification implements IService<Notification> {
    private Connection connection;

    public ServiceNotification() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Notification notification) {
        String query = "INSERT INTO notification (idUtilisateur, titre, message, type, link, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, notification.getIdUtilisateur());
            pst.setString(2, notification.getTitre());
            pst.setString(3, notification.getMessage());
            pst.setString(4, notification.getType());
            pst.setString(5, notification.getLink());
            pst.setBoolean(6, notification.isRead());
            pst.setTimestamp(7, Timestamp.valueOf(notification.getCreatedAt()));
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding notification: " + e.getMessage());
        }
    }

    @Override
    public void update(Notification notification) {
        String query = "UPDATE notification SET is_read = ?, read_at = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setBoolean(1, notification.isRead());
            if (notification.getReadAt() != null) {
                pst.setTimestamp(2, Timestamp.valueOf(notification.getReadAt()));
            } else {
                pst.setNull(2, Types.TIMESTAMP);
            }
            pst.setInt(3, notification.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating notification: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM notification WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting notification: " + e.getMessage());
        }
    }

    @Override
    public List<Notification> getAll() {
        return new ArrayList<>(); 
    }

    @Override
    public Notification getOne(int id) {
        String query = "SELECT * FROM notification WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching notification: " + e.getMessage());
        }
        return null;
    }

    public List<Notification> getByUser(int idUtilisateur) {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notification WHERE idUtilisateur = ? ORDER BY created_at DESC";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching notifications for user: " + e.getMessage());
        }
        return list;
    }

    public int getUnreadCount(int idUtilisateur) {
        String query = "SELECT COUNT(*) FROM notification WHERE idUtilisateur = ? AND is_read = 0";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching unread notifications count: " + e.getMessage());
        }
        return 0;
    }

    public void markAllAsRead(int idUtilisateur) {
        String query = "UPDATE notification SET is_read = 1, read_at = CURRENT_TIMESTAMP WHERE idUtilisateur = ? AND is_read = 0";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idUtilisateur);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error marking notifications as read: " + e.getMessage());
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp readTs = rs.getTimestamp("read_at");
        return new Notification(
                rs.getInt("id"),
                rs.getInt("idUtilisateur"),
                rs.getString("titre"),
                rs.getString("message"),
                rs.getString("type"),
                rs.getString("link"),
                rs.getBoolean("is_read"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                readTs != null ? readTs.toLocalDateTime() : null
        );
    }
}
