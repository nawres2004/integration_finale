package tn.esprit.suivie_nawres.services;

import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsultationService {
    private static final String TABLE_NAME = "consultation";

    public void ajouterConsultation(Consultation consultation) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (utilisateur_id, date_consultation, heure_consultation, mode_consultation, maladie, diagnostic, traitement, examens_complementaires, notes_consultation, cout_consultation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, consultation);
            statement.executeUpdate();
        }
    }

    public void modifierConsultation(Consultation consultation) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET date_consultation = ?, heure_consultation = ?, mode_consultation = ?, maladie = ?, diagnostic = ?, traitement = ?, examens_complementaires = ?, notes_consultation = ?, cout_consultation = ? WHERE utilisateur_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(consultation.getDateConsultation()));
            statement.setTime(2, Time.valueOf(consultation.getHeureConsultation()));
            statement.setString(3, consultation.getModeConsultation());
            statement.setString(4, consultation.getMaladie());
            statement.setString(5, consultation.getDiagnostic());
            statement.setString(6, consultation.getTraitement());
            statement.setString(7, consultation.getExamensComplementaires());
            statement.setString(8, consultation.getNotesConsultation());
            statement.setBigDecimal(9, consultation.getCoutConsultation());
            statement.setInt(10, consultation.getUtilisateurId());
            statement.executeUpdate();
        }
    }

    public void supprimerConsultation(int utilisateurId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE utilisateur_id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, utilisateurId);
            statement.executeUpdate();
        }
    }

    public List<Consultation> afficherConsultations() throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY date_consultation DESC, heure_consultation DESC";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        List<Consultation> consultations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                consultations.add(mapRow(resultSet));
            }
        }
        return consultations;
    }

    public Optional<Consultation> chercherParId(int utilisateurId) throws SQLException {
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

    public boolean existe(int utilisateurId) throws SQLException {
        return chercherParId(utilisateurId).isPresent();
    }

    private void bind(PreparedStatement statement, Consultation consultation) throws SQLException {
        statement.setInt(1, consultation.getUtilisateurId());
        statement.setDate(2, Date.valueOf(consultation.getDateConsultation()));
        statement.setTime(3, Time.valueOf(consultation.getHeureConsultation()));
        statement.setString(4, consultation.getModeConsultation());
        statement.setString(5, consultation.getMaladie());
        statement.setString(6, consultation.getDiagnostic());
        statement.setString(7, consultation.getTraitement());
        statement.setString(8, consultation.getExamensComplementaires());
        statement.setString(9, consultation.getNotesConsultation());
        statement.setBigDecimal(10, consultation.getCoutConsultation() == null ? BigDecimal.ZERO : consultation.getCoutConsultation());
    }

    private Consultation mapRow(ResultSet resultSet) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        Date date = resultSet.getDate("date_consultation");
        Time time = resultSet.getTime("heure_consultation");
        consultation.setDateConsultation(date == null ? null : date.toLocalDate());
        consultation.setHeureConsultation(time == null ? null : time.toLocalTime());
        consultation.setModeConsultation(resultSet.getString("mode_consultation"));
        consultation.setMaladie(resultSet.getString("maladie"));
        consultation.setDiagnostic(resultSet.getString("diagnostic"));
        consultation.setTraitement(resultSet.getString("traitement"));
        consultation.setExamensComplementaires(resultSet.getString("examens_complementaires"));
        consultation.setNotesConsultation(resultSet.getString("notes_consultation"));
        consultation.setCoutConsultation(resultSet.getBigDecimal("cout_consultation"));
        return consultation;
    }
}

