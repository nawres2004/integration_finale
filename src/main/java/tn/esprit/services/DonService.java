package tn.esprit.services;

import tn.esprit.models.Don;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;

public class DonService {

    // Connexion à la base de données (partagée)
    private Connection cnx = MyDbConnexion.getInstance().getCnx();

    // ──────────────────────────────────────────
    // AJOUTER un don
    // ──────────────────────────────────────────
    public void ajouter(Don d) throws SQLException {

        // Vérifier si le projet a déjà atteint son objectif
        PreparedStatement psCheck = cnx.prepareStatement(
                "SELECT objectif_financier, montant_collecte FROM projet WHERE id=?"
        );
        psCheck.setInt(1, d.getProjetId());
        ResultSet rs = psCheck.executeQuery();

        if (rs.next()) {
            double objectif = rs.getDouble("objectif_financier");
            double collecte = rs.getDouble("montant_collecte");

            if (objectif > 0 && collecte >= objectif) {
                rs.close();
                psCheck.close();
                throw new SQLException("Ce projet a déjà atteint son objectif !");
            }
        }
        rs.close();
        psCheck.close();

        // Insérer le don
        String sql = "INSERT INTO don (montant, date_don, mode_paiement, email_donateur, " +
                "nom_donateur, prenom_donateur, flouci_payment_id, payment_status, " +
                "message_soutien, projet_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDouble(1, d.getMontant());
        ps.setDate(2,   d.getDateDon());   
        ps.setString(3, d.getModePaiement());
        ps.setString(4, d.getEmailDonateur());
        ps.setString(5, d.getNomDonateur());
        ps.setString(6, d.getPrenomDonateur());
        ps.setString(7, d.getFlouciPaymentId());
        ps.setString(8, d.getPaymentStatus());
        ps.setString(9, d.getMessageSoutien());
        ps.setInt(10,   d.getProjetId());

        ps.executeUpdate();
        ps.close();

        // 3. Mettre à jour le montant collecté du projet si le paiement est validé
        if ("paid".equalsIgnoreCase(d.getPaymentStatus())) {
            PreparedStatement psUp = cnx.prepareStatement(
                    "UPDATE projet SET montant_collecte = montant_collecte + ? WHERE id=?"
            );
            psUp.setDouble(1, d.getMontant());
            psUp.setInt(2,    d.getProjetId());
            psUp.executeUpdate();
            psUp.close();
        }
    }

    // ──────────────────────────────────────────
    // MODIFIER (non utilisé)
    // ──────────────────────────────────────────
    public void modifier(Don d) throws SQLException {
        // Non utilisé
    }

    // ──────────────────────────────────────────
    // SUPPRIMER un don
    // ──────────────────────────────────────────
    public void supprimer(int id) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement("DELETE FROM don WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    // ──────────────────────────────────────────
    // AFFICHER tous les dons
    // ──────────────────────────────────────────
    public ArrayList<Don> afficherAll() throws SQLException {
        ArrayList<Don> liste = new ArrayList<>();

        PreparedStatement ps = cnx.prepareStatement("SELECT * FROM don");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            liste.add(convertirEnDon(rs));
        }

        rs.close();
        ps.close();
        return liste;
    }

    // ──────────────────────────────────────────
    // COMPTER le nombre total de dons
    // ──────────────────────────────────────────
    public int compterDons() throws SQLException {
        PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM don");
        ResultSet rs = ps.executeQuery();

        int total = rs.next() ? rs.getInt(1) : 0;

        rs.close();
        ps.close();
        return total;
    }

    // ──────────────────────────────────────────
    // CALCULER le montant total des dons
    // ──────────────────────────────────────────
    public double getTotalDons() throws SQLException {
        PreparedStatement ps = cnx.prepareStatement("SELECT COALESCE(SUM(montant), 0) FROM don");
        ResultSet rs = ps.executeQuery();

        double total = rs.next() ? rs.getDouble(1) : 0;

        rs.close();
        ps.close();
        return total;
    }

    // ──────────────────────────────────────────
    // UTILITAIRE : transformer une ligne SQL → objet Don
    // ──────────────────────────────────────────
    private Don convertirEnDon(ResultSet rs) throws SQLException {
        return new Don(
                rs.getInt("id"),
                rs.getDouble("montant"),
                rs.getDate("date_don"),
                rs.getString("mode_paiement"),
                rs.getString("email_donateur"),
                rs.getString("message_soutien"),
                rs.getInt("projet_id"),
                rs.getString("nom_donateur"),
                rs.getString("prenom_donateur"),
                rs.getString("flouci_payment_id"),
                rs.getString("payment_status")
        );
    }
}