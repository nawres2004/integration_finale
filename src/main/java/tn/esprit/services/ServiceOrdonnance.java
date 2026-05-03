package tn.esprit.services;

import tn.esprit.models.Ordonnance;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrdonnance implements CRUD<Ordonnance> {

    private Connection cnx;

    public ServiceOrdonnance() {
        cnx = MyDbConnexion.getInstance().getCnx();
    }

    @Override
    public void insertOne(Ordonnance o) throws SQLException {
        String req = "INSERT INTO `ordonnance`(`date_ordonnance`, `instructions`, `duree_traitement`, `idUtilisateur`, `is_seen`) "
                +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setDate(1, Date.valueOf(o.getDateOrdonnance()));
        ps.setString(2, o.getInstructions());
        ps.setString(3, o.getDureeTraitement());
        ps.setInt(4, o.getIdUtilisateur());
        ps.setBoolean(5, o.isSeen());
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Ordonnance o) throws SQLException {
        String req = "UPDATE `ordonnance` SET `date_ordonnance`=?, `instructions`=?, `duree_traitement`=?, `idUtilisateur`=?, `is_seen`=? "
                +
                "WHERE `id`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setDate(1, Date.valueOf(o.getDateOrdonnance()));
        ps.setString(2, o.getInstructions());
        ps.setString(3, o.getDureeTraitement());
        ps.setInt(4, o.getIdUtilisateur());
        ps.setBoolean(5, o.isSeen());
        ps.setInt(6, o.getId());
        ps.executeUpdate();
    }

    @Override
    public void deleteOne(int id) throws SQLException {
        String req = "DELETE FROM `ordonnance` WHERE `id`=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Ordonnance> findALL() throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String req = "SELECT o.*, u.nom FROM `ordonnance` o " +
                "JOIN `utilisateur` u ON o.idUtilisateur = u.idUtilisateur";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Ordonnance o = new Ordonnance(
                    rs.getInt("id"),
                    rs.getDate("date_ordonnance").toLocalDate(),
                    rs.getString("instructions"),
                    rs.getString("duree_traitement"),
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getBoolean("is_seen"));
            list.add(o);
        }
        return list;
    }

    public java.util.Map<String, Integer> countByMois() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String[] moisFr = { "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" };

        String req = "SELECT MONTH(date_ordonnance) as mois, COUNT(*) as total " +
                "FROM ordonnance " +
                "GROUP BY MONTH(date_ordonnance) " +
                "ORDER BY MONTH(date_ordonnance)";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            int m = rs.getInt("mois");
            stats.put(moisFr[m - 1], rs.getInt("total"));
        }
        return stats;
    }

    public List<Ordonnance> findByPatient(int idPatient) throws SQLException {
        List<Ordonnance> list = new ArrayList<>();
        String req = "SELECT o.*, u.nom FROM `ordonnance` o " +
                     "JOIN `utilisateur` u ON o.idUtilisateur = u.idUtilisateur " +
                     "WHERE o.idUtilisateur = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idPatient);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Ordonnance o = new Ordonnance(
                    rs.getInt("id"),
                    rs.getDate("date_ordonnance").toLocalDate(),
                    rs.getString("instructions"),
                    rs.getString("duree_traitement"),
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getBoolean("is_seen")
            );
            list.add(o);
        }
        return list;
    }
}
