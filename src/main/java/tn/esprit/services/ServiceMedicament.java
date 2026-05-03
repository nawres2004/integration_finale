package tn.esprit.services;

import tn.esprit.models.Medicament;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMedicament implements CRUD<Medicament> {

    private Connection cnx;

    public ServiceMedicament() {
        cnx = MyDbConnexion.getInstance().getCnx();
    }

    // ─── INSERT avec Statement (concaténation) ────────────────────────────────
    @Override
    public void insertOne(Medicament m) throws SQLException {
        String req = "INSERT INTO `medicament`(`nom`, `dosage`, `forme`, `frequence`, `contre_indications`, `date_expiration`, `ordonnance_id`) " +
                "VALUES ('" + m.getNom() + "', '" + m.getDosage() + "', '" + m.getForme() + "', '" + m.getFrequence() + "', '" + m.getContreIndications() + "', '" + m.getDateExpiration() + "', " + m.getOrdonnanceId() + ")";
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    // ─── INSERT avec PreparedStatement (sécurisé) ─────────────────────────────
    public void insertOnePS(Medicament m) throws SQLException {
        String req = "INSERT INTO `medicament`(`nom`, `dosage`, `forme`, `frequence`, `contre_indications`, `date_expiration`, `ordonnance_id`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement st = cnx.prepareStatement(req);
        st.setString(1, m.getNom());
        st.setString(2, m.getDosage());
        st.setString(3, m.getForme());
        st.setString(4, m.getFrequence());
        st.setString(5, m.getContreIndications());
        st.setDate(6, Date.valueOf(m.getDateExpiration()));
        
        if (m.getOrdonnanceId() == 0) {
            st.setNull(7, java.sql.Types.INTEGER);
        } else {
            st.setInt(7, m.getOrdonnanceId());
        }
        
        st.executeUpdate();
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Override
    public void updateOne(Medicament m) throws SQLException {
        String req = "UPDATE `medicament` SET " +
                "`nom`=?, `dosage`=?, `forme`=?, `frequence`=?, `contre_indications`=?, `date_expiration`=?, `ordonnance_id`=? " +
                "WHERE `id`=?";
        PreparedStatement st = cnx.prepareStatement(req);
        st.setString(1, m.getNom());
        st.setString(2, m.getDosage());
        st.setString(3, m.getForme());
        st.setString(4, m.getFrequence());
        st.setString(5, m.getContreIndications());
        st.setDate(6, Date.valueOf(m.getDateExpiration()));
        
        if (m.getOrdonnanceId() == 0) {
            st.setNull(7, java.sql.Types.INTEGER);
        } else {
            st.setInt(7, m.getOrdonnanceId());
        }
        
        st.setInt(8, m.getId());
        st.executeUpdate();
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Override
    public void deleteOne(int id) throws SQLException {
        String req = "DELETE FROM `medicament` WHERE `id`=?";
        PreparedStatement st = cnx.prepareStatement(req);
        st.setInt(1, id);
        st.executeUpdate();
    }

    // ─── SELECT ALL ───────────────────────────────────────────────────────────
    @Override
    public List<Medicament> findALL() throws SQLException {
        List<Medicament> list = new ArrayList<>();
        String req = "SELECT * FROM `medicament`";
        Statement st = cnx.createStatement();

        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Medicament m = new Medicament(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("dosage"),
                    rs.getString("forme"),
                    rs.getString("frequence"),
                    rs.getString("contre_indications"),
                    rs.getDate("date_expiration").toLocalDate(),
                    rs.getInt("ordonnance_id")
            );
            list.add(m);
        }
        return list;
    }

    // ─── UNIQUENESS CHECK ─────────────────────────────────────────────────────
    public boolean exists(String nom, String dosage) throws SQLException {
        String req = "SELECT COUNT(*) FROM `medicament` WHERE `nom` = ? AND `dosage` = ?";
        PreparedStatement st = cnx.prepareStatement(req);
        st.setString(1, nom);
        st.setString(2, dosage);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    public boolean existsForUpdate(String nom, String dosage, int currentId) throws SQLException {
        String req = "SELECT COUNT(*) FROM `medicament` WHERE `nom` = ? AND `dosage` = ? AND `id` != ?";
        PreparedStatement st = cnx.prepareStatement(req);
        st.setString(1, nom);
        st.setString(2, dosage);
        st.setInt(3, currentId);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}