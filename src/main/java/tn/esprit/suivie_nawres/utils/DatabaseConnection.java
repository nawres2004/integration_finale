package tn.esprit.suivie_nawres.utils;

import tn.esprit.suivie_nawres.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static DatabaseConnection instance;
    private final DatabaseConfig config;
    private Connection connection;

    private DatabaseConnection() {
        this.config = DatabaseConfig.load();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(config.jdbcUrl(), config.username(), config.password());
        }
        return connection;
    }

    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException exception) {
                throw new IllegalStateException("Impossible de fermer la connexion MySQL", exception);
            }
        }
    }

    public DatabaseConfig getConfig() {
        return config;
    }
}

