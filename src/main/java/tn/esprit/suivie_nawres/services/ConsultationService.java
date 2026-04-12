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
        String sql = "INSERT INTO " + TABLE_NAME + " (id, nom, prenom, date_consultation, heure_consultation, mode_consultation, maladie, diagnostic, traitement, examens_complementaires, notes_consultation, cout_consultation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, consultation);
            statement.executeUpdate();
        }
    }

    public void modifierConsultation(Consultation consultation) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET nom = ?, prenom = ?, date_consultation = ?, heure_consultation = ?, mode_consultation = ?, maladie = ?, diagnostic = ?, traitement = ?, examens_complementaires = ?, notes_consultation = ?, cout_consultation = ? WHERE id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, consultation.getNom());
            statement.setString(2, consultation.getPrenom());
            statement.setDate(3, Date.valueOf(consultation.getDateConsultation()));
            statement.setTime(4, Time.valueOf(consultation.getHeureConsultation()));
            statement.setString(5, consultation.getModeConsultation());
            statement.setString(6, consultation.getMaladie());
            statement.setString(7, consultation.getDiagnostic());
            statement.setString(8, consultation.getTraitement());
            statement.setString(9, consultation.getExamensComplementaires());
            statement.setString(10, consultation.getNotesConsultation());
            statement.setBigDecimal(11, consultation.getCoutConsultation());
            statement.setInt(12, consultation.getUtilisateurId());
            statement.executeUpdate();
        }
    }

    public void supprimerConsultation(int utilisateurId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
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
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
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
        statement.setString(2, consultation.getNom());
        statement.setString(3, consultation.getPrenom());
        statement.setDate(4, Date.valueOf(consultation.getDateConsultation()));
        statement.setTime(5, Time.valueOf(consultation.getHeureConsultation()));
        statement.setString(6, consultation.getModeConsultation());
        statement.setString(7, consultation.getMaladie());
        statement.setString(8, consultation.getDiagnostic());
        statement.setString(9, consultation.getTraitement());
        statement.setString(10, consultation.getExamensComplementaires());
        statement.setString(11, consultation.getNotesConsultation());
        statement.setBigDecimal(12, consultation.getCoutConsultation() == null ? BigDecimal.ZERO : consultation.getCoutConsultation());
    }

    private Consultation mapRow(ResultSet resultSet) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setUtilisateurId(resultSet.getInt("id"));
        consultation.setNom(resultSet.getString("nom"));
        consultation.setPrenom(resultSet.getString("prenom"));
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

