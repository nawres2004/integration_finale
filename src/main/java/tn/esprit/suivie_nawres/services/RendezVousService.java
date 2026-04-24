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

    /**
     * 💾 AJOUTER UN RENDEZ-VOUS (avec ID auto-généré)
     * 
     * L'ID est maintenant AUTO-INCRÉMENTÉ par MySQL
     * On n'a plus besoin de le fournir !
     */
    public int ajouterRendezVous(RendezVous rendezVous) throws SQLException {
        // ✅ NOUVELLE REQUÊTE : Sans l'ID (MySQL le génère automatiquement)
        String sql = "INSERT INTO " + TABLE_NAME + " (nom, prenom, date_rendez_vous, heure_rendez_vous, priorite, mode_consultation, statut_rendez_vous, notes_rendez_vous, pays, telephone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection connection = DatabaseConnection.getInstance().getConnection();
        
        // RETURN_GENERATED_KEYS permet de récupérer l'ID généré
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // Remplit les paramètres (sans l'ID)
            bindSansId(statement, rendezVous);
            statement.executeUpdate();
            
            // Récupère l'ID généré par MySQL
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenere = generatedKeys.getInt(1);
                    rendezVous.setUtilisateurId(idGenere);  // Met à jour l'objet avec l'ID généré
                    return idGenere;  // Retourne l'ID généré
                } else {
                    throw new SQLException("Échec de la création du rendez-vous, aucun ID généré.");
                }
            }
        }
    }

    public void modifierRendezVous(RendezVous rendezVous) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET nom = ?, prenom = ?, date_rendez_vous = ?, heure_rendez_vous = ?, priorite = ?, mode_consultation = ?, statut_rendez_vous = ?, notes_rendez_vous = ?, pays = ?, telephone = ? WHERE id = ?";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rendezVous.getNom());
            statement.setString(2, rendezVous.getPrenom());
            statement.setDate(3, Date.valueOf(rendezVous.getDateRendezVous()));
            statement.setTime(4, Time.valueOf(rendezVous.getHeureRendezVous()));
            statement.setString(5, rendezVous.getPriorite());
            statement.setString(6, rendezVous.getModeConsultation());
            statement.setString(7, resolveStatut(rendezVous.getStatutRendezVous()));
            statement.setString(8, rendezVous.getNotesRendezVous());
            statement.setString(9, rendezVous.getPays());
            statement.setString(10, rendezVous.getTelephone());
            statement.setInt(11, rendezVous.getUtilisateurId());
            statement.executeUpdate();
        }
    }

    public void supprimerRendezVous(int utilisateurId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
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

    public void changerStatutRendezVous(int utilisateurId, StatutRendezVous statut) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET statut_rendez_vous = ? WHERE id = ?";
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
        statement.setString(2, rendezVous.getNom());
        statement.setString(3, rendezVous.getPrenom());
        statement.setDate(4, Date.valueOf(rendezVous.getDateRendezVous()));
        statement.setTime(5, Time.valueOf(rendezVous.getHeureRendezVous()));
        statement.setString(6, rendezVous.getPriorite());
        statement.setString(7, rendezVous.getModeConsultation());
        statement.setString(8, resolveStatut(rendezVous.getStatutRendezVous()));
        statement.setString(9, rendezVous.getNotesRendezVous());
        statement.setString(10, rendezVous.getPays());
        statement.setString(11, rendezVous.getTelephone());
    }

    /**
     * 🆕 NOUVELLE MÉTHODE : Remplit le PreparedStatement SANS l'ID
     * Utilisée pour l'ajout (l'ID est auto-généré)
     */
    private void bindSansId(PreparedStatement statement, RendezVous rendezVous) throws SQLException {
        statement.setString(1, rendezVous.getNom());
        statement.setString(2, rendezVous.getPrenom());
        statement.setDate(3, Date.valueOf(rendezVous.getDateRendezVous()));
        statement.setTime(4, Time.valueOf(rendezVous.getHeureRendezVous()));
        statement.setString(5, rendezVous.getPriorite());
        statement.setString(6, rendezVous.getModeConsultation());
        statement.setString(7, resolveStatut(rendezVous.getStatutRendezVous()));
        statement.setString(8, rendezVous.getNotesRendezVous());
        statement.setString(9, rendezVous.getPays());
        statement.setString(10, rendezVous.getTelephone());
    }

    private RendezVous mapRow(ResultSet resultSet) throws SQLException {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setUtilisateurId(resultSet.getInt("id"));
        rendezVous.setNom(resultSet.getString("nom"));
        rendezVous.setPrenom(resultSet.getString("prenom"));
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

