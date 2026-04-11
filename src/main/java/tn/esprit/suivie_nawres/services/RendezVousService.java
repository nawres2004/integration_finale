package tn.esprit.suivie_nawres.services;

import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class RendezVousService {
    private static final String TABLE_NAME = "rendez_vous";

    public void ajouterRendezVous(RendezVous rendezVous) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (utilisateur_id, date_rendez_vous, heure_rendez_vous, priorite, mode_consultation, statut_rendez_vous, notes_rendez_vous, pays, telephone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, rendezVous);
            statement.executeUpdate();
        }
    }

    public void modifierRendezVous(RendezVous rendezVous) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET date_rendez_vous = ?, heure_rendez_vous = ?, priorite = ?, mode_consultation = ?, statut_rendez_vous = ?, notes_rendez_vous = ?, pays = ?, telephone = ? WHERE utilisateur_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(rendezVous.getDateRendezVous()));
            statement.setTime(2, Time.valueOf(rendezVous.getHeureRendezVous()));
            statement.setString(3, rendezVous.getPriorite());
            statement.setString(4, rendezVous.getModeConsultation());
            statement.setString(5, resolveStatut(rendezVous.getStatutRendezVous()));
            statement.setString(6, rendezVous.getNotesRendezVous());
            statement.setString(7, rendezVous.getPays());
            statement.setString(8, rendezVous.getTelephone());
            statement.setInt(9, rendezVous.getUtilisateurId());
            statement.executeUpdate();
        }
    }

    public void supprimerRendezVous(int utilisateurId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE utilisateur_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            statement.executeUpdate();
        }
    }

    public List<RendezVous> afficherRendezVous() throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY date_rendez_vous DESC, heure_rendez_vous DESC";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        List<RendezVous> rendezVousList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rendezVousList.add(mapRow(resultSet));
            }
        }
        return rendezVousList;
    }

    public Optional<RendezVous> chercherParId(int utilisateurId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE utilisateur_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public void changerStatutRendezVous(int utilisateurId, StatutRendezVous statut) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET statut_rendez_vous = ? WHERE utilisateur_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resolveStatut(statut));
            statement.setInt(2, utilisateurId);
            statement.executeUpdate();
        }
    }

    public boolean existe(int utilisateurId) throws SQLException {
        return chercherParId(utilisateurId).isPresent();
    }

    private void bind(PreparedStatement statement, RendezVous rendezVous) throws SQLException {
        statement.setInt(1, rendezVous.getUtilisateurId());
        statement.setDate(2, Date.valueOf(rendezVous.getDateRendezVous()));
        statement.setTime(3, Time.valueOf(rendezVous.getHeureRendezVous()));
        statement.setString(4, rendezVous.getPriorite());
        statement.setString(5, rendezVous.getModeConsultation());
        statement.setString(6, resolveStatut(rendezVous.getStatutRendezVous()));
        statement.setString(7, rendezVous.getNotesRendezVous());
        statement.setString(8, rendezVous.getPays());
        statement.setString(9, rendezVous.getTelephone());
    }

    private RendezVous mapRow(ResultSet resultSet) throws SQLException {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        Date date = resultSet.getDate("date_rendez_vous");
        Time time = resultSet.getTime("heure_rendez_vous");
        rendezVous.setDateRendezVous(date == null ? null : date.toLocalDate());
        rendezVous.setHeureRendezVous(time == null ? null : time.toLocalTime());
        rendezVous.setPriorite(resultSet.getString("priorite"));
        rendezVous.setModeConsultation(resultSet.getString("mode_consultation"));
        rendezVous.setStatutRendezVous(parseStatut(resultSet.getString("statut_rendez_vous")));
        rendezVous.setNotesRendezVous(resultSet.getString("notes_rendez_vous"));
        rendezVous.setPays(resultSet.getString("pays"));
        rendezVous.setTelephone(resultSet.getString("telephone"));
        return rendezVous;
    }

    private String resolveStatut(StatutRendezVous statutRendezVous) {
        return Objects.requireNonNullElse(statutRendezVous, StatutRendezVous.EN_ATTENTE).name();
    }

    private StatutRendezVous parseStatut(String value) {
        try {
            return value == null ? StatutRendezVous.EN_ATTENTE : StatutRendezVous.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return StatutRendezVous.EN_ATTENTE;
        }
    }
}

