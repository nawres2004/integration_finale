package tn.esprit.services;

import tn.esprit.models.Projet;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;

public class ProjetService {

    private Connection cnx;

    public ProjetService() {
        cnx = MyDbConnexion.getInstance().getCnx();
    }

    //CREATE
    public void ajouter(Projet p) throws SQLException {
        String req = "INSERT INTO projet (titre_projet, description, " +
                "objectif_financier, montant_collecte, date_debut, date_fin) VALUES (?, ?, ?, 0, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);

        ps.setString(1, p.getTitreProjet());
        ps.setString(2, p.getDescription());
        ps.setDouble(3, p.getObjectifFinancier());
        ps.setDate(4, p.getDateDebut());
        ps.setDate(5, p.getDateFin());

        ps.executeUpdate();
        ps.close();
    }

    // UPDATE
    public void modifier(Projet p) throws SQLException {
        String req = "UPDATE projet SET titre_projet=?, description=?, " +
                "objectif_financier=?, date_debut=?, date_fin=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);

        ps.setString(1, p.getTitreProjet());
        ps.setString(2, p.getDescription());
        ps.setDouble(3, p.getObjectifFinancier());
        ps.setDate(4, p.getDateDebut()); 
        ps.setDate(5, p.getDateFin());
        ps.setInt(6, p.getId());

        ps.executeUpdate();
        ps.close();
    }

    // DELETE
    public void supprimer(int id) throws SQLException {
        String check = "SELECT COUNT(*) FROM don WHERE projet_id=?";
        PreparedStatement psCheck = cnx.prepareStatement(check);
        psCheck.setInt(1, id);

        ResultSet rs = psCheck.executeQuery();
        rs.next();
        int nb = rs.getInt(1);

        rs.close();
        psCheck.close();

        if (nb > 0) {
            throw new SQLException("Impossible de supprimer : ce projet contient " + nb + " don(s).");
        }

        String req = "DELETE FROM projet WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    // READ 
    public ArrayList<Projet> afficherAll() throws SQLException {
        ArrayList<Projet> list = new ArrayList<>();

        String req = "SELECT * FROM projet";
        PreparedStatement ps = cnx.prepareStatement(req);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapper(rs));
        }

        rs.close();
        ps.close();
        return list;
    }
    public int compterProjets() throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM projet");
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;


        
        rs.close();
        ps.close();
        return count;
    }


    // MAPPER (Elle joue le rôle de traducteur entre le monde SQL (lignes/colonnes) et le monde Java (objets))
    private Projet mapper(ResultSet rs) throws SQLException {
        return new Projet(
                rs.getInt("id"),
                rs.getString("titre_projet"),
                rs.getString("description"),
                rs.getDouble("objectif_financier"),
                rs.getDouble("montant_collecte"),
                rs.getDate("date_debut"),
                rs.getDate("date_fin")
        );
    }
}