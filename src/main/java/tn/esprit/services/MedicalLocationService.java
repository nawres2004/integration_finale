package tn.esprit.services;

import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDbConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MedicalLocationService {

    private final Connection cnx;

    public MedicalLocationService() {
        this.cnx = MyDbConnexion.getInstance().getCnx();
    }

    // ── Formule Haversine ───────────────────────────────────
    /**
     * Calcule la distance en kilomètres entre deux points GPS.
     * Formule Haversine — précision ~0.5%
     */
    public static double haversineDistance(double lat1, double lon1,
                                           double lat2, double lon2) {
        final double R = 6371.0; // rayon Terre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ── Récupérer tous les médecins avec localisation ───────
    public List<Utilisateur> getAllMedecinsWithLocation() {
        List<Utilisateur> list = new ArrayList<>();
        if (cnx == null) return list;

        String sql = "SELECT idUtilisateur, nom, prenom, email, telephone, specialite, " +
                     "latitude, longitude, isActive FROM utilisateur " +
                     "WHERE idRole = 3 AND isActive = 1 " +
                     "AND latitude IS NOT NULL AND longitude IS NOT NULL";
        try {
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setIdUtilisateur(rs.getInt("idUtilisateur"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setSpecialite(rs.getString("specialite"));
                u.setLatitude(rs.getDouble("latitude"));
                u.setLongitude(rs.getDouble("longitude"));
                u.setActive(rs.getBoolean("isActive"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[MedicalLocationService] getAllMedecins: " + e.getMessage());
        }
        return list;
    }

    // ── Médecins triés par proximité ────────────────────────
    /**
     * Retourne les médecins triés du plus proche au plus loin.
     * @param patientLat latitude du patient
     * @param patientLon longitude du patient
     */
    public List<MedecinDistance> getNearestDoctors(double patientLat, double patientLon) {
        List<Utilisateur> medecins = getAllMedecinsWithLocation();
        List<MedecinDistance> result = new ArrayList<>();

        for (Utilisateur m : medecins) {
            double dist = haversineDistance(patientLat, patientLon,
                                            m.getLatitude(), m.getLongitude());
            result.add(new MedecinDistance(m, dist));
        }

        result.sort(Comparator.comparingDouble(md -> md.distanceKm));
        return result;
    }

    // ── Sauvegarder localisation utilisateur ────────────────
    public boolean saveLocation(int idUtilisateur, double lat, double lon) {
        if (cnx == null) return false;
        String sql = "UPDATE utilisateur SET latitude = ?, longitude = ? WHERE idUtilisateur = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setDouble(1, lat);
            ps.setDouble(2, lon);
            ps.setInt(3, idUtilisateur);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MedicalLocationService] saveLocation: " + e.getMessage());
            return false;
        }
    }

    // ── Classe résultat avec distance ───────────────────────
    public static class MedecinDistance {
        public final Utilisateur medecin;
        public final double distanceKm;

        public MedecinDistance(Utilisateur medecin, double distanceKm) {
            this.medecin = medecin;
            this.distanceKm = distanceKm;
        }

        public String getDistanceLabel() {
            if (distanceKm < 1.0)
                return String.format("%.0f m", distanceKm * 1000);
            return String.format("%.1f km", distanceKm);
        }

        @Override
        public String toString() {
            return String.format("Dr. %s %s (%s) — %s",
                medecin.getPrenom(), medecin.getNom(),
                medecin.getSpecialite() != null ? medecin.getSpecialite() : "Généraliste",
                getDistanceLabel());
        }
    }
}
