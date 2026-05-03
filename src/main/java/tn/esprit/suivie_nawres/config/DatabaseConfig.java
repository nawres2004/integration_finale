package tn.esprit.suivie_nawres.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record DatabaseConfig(
        String host,
        int port,
        String databaseName,
        String username,
        String password,
        String parameters
) {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DEFAULT_DATABASE = "vitaplus";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_PARAMETERS = "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static DatabaseConfig load() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de lire la configuration de base de données", exception);
        }

        return new DatabaseConfig(
                resolve("db.host", "DB_HOST", properties, DEFAULT_HOST),
                parsePort(resolve("db.port", "DB_PORT", properties, String.valueOf(DEFAULT_PORT))),
                resolve("db.name", "DB_NAME", properties, DEFAULT_DATABASE),
                resolve("db.username", "DB_USERNAME", properties, DEFAULT_USERNAME),
                resolve("db.password", "DB_PASSWORD", properties, DEFAULT_PASSWORD),
                resolve("db.parameters", "DB_PARAMETERS", properties, DEFAULT_PARAMETERS)
        );
    }

    public String jdbcUrl() {
        if (parameters == null || parameters.isBlank()) {
            return "jdbc:mysql://%s:%d/%s".formatted(host, port, databaseName);
        }
        return "jdbc:mysql://%s:%d/%s?%s".formatted(host, port, databaseName, parameters);
    }

    private static String resolve(String propertyKey, String environmentKey, Properties properties, String defaultValue) {
        String systemProperty = System.getProperty(propertyKey);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        String configuredValue = properties.getProperty(propertyKey);
        if (configuredValue != null && !configuredValue.isBlank()) {
            return configuredValue;
        }

        return defaultValue;
    }

    private static int parsePort(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Le port MySQL doit être un nombre valide: " + value, exception);
        }
    }
}

