package tn.esprit.services;

import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUtilisateur implements CRUD<Utilisateur> {

    private Connection cnx;

    public ServiceUtilisateur() {
        cnx = MyDbConnexion.getInstance().getCnx();
    }

    // INSERT (kif Person)
    @Override
    public void insertOne(Utilisateur u) throws SQLException {
        String req = "INSERT INTO utilisateur (nom) VALUES ('" + u.getNom() + "')";
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    // INSERT avec PreparedStatement (meilleur)
    public void insertOnePS(Utilisateur u) throws SQLException {
        String req = "INSERT INTO utilisateur (nom) VALUES (?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, u.getNom());
        ps.executeUpdate();
    }

    @Override
    public void updateOne(Utilisateur u) throws SQLException {
        String req = "UPDATE utilisateur SET nom='" + u.getNom() + "' WHERE idUtilisateur=" + u.getIdUtilisateur();
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    // DELETE
    @Override
    public void deleteOne(int id) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE idUtilisateur=" + id;
        Statement st = cnx.createStatement();
        st.executeUpdate(req);
    }

    // SELECT ALL
    @Override
    public List<Utilisateur> findALL() throws SQLException {

        List<Utilisateur> list = new ArrayList<>();

        String req = "SELECT * FROM utilisateur";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {

            Utilisateur u = new Utilisateur(
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"));

            list.add(u);
        }

        return list;
    }

    public String getMailById(int id) throws SQLException {
        String req = "SELECT email FROM utilisateur WHERE idUtilisateur = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("email");
        }
        return null;
    }

}