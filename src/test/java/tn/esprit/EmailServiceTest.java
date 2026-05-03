package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.models.Don;
import tn.esprit.models.Projet;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DonServiceTest {

    static DonService ds;
    static ProjetService ps;
    static int idProjetTemp;
    static int idDonTest;

    @BeforeAll
    static void setup() throws SQLException {
        ds = new DonService();
        ps = new ProjetService();

        // Créer un projet temporaire pour les tests de dons
        Projet p = new Projet(
                "Projet Don Test",
                "Projet utilisé pour tester les dons",
                10000.0, 0.0,
                Date.valueOf("2026-01-01"),
                Date.valueOf("2026-12-31")
        );
        ps.ajouter(p);

        // Récupérer son id
        ArrayList<Projet> liste = ps.afficherAll();
        idProjetTemp = liste.get(liste.size() - 1).getId();
    }

    // ✅ TEST 1 : Ajouter un don
    @Test
    @Order(1)
    void testAjouterDon() {
        Don d = new Don(
                200.0,
                Date.valueOf("2026-04-13"),
                "sur_place",
                "test@gmail.com",
                "Bon courage",
                idProjetTemp,
                "Test",
                "Unitaire",
                null,
                "paid"
        );
        assertDoesNotThrow(() -> ds.ajouter(d));
    }

    // ✅ TEST 2 : Afficher tous les dons
    @Test
    @Order(2)
    void testAfficherAll() throws SQLException {
        ArrayList<Don> liste = ds.afficherAll();
        assertNotNull(liste);
        assertFalse(liste.isEmpty(), "La liste de dons ne doit pas être vide");
    }

    // ✅ TEST 3 : Compter les dons
    @Test
    @Order(3)
    void testCompterDons() throws SQLException {
        int count = ds.compterDons();
        assertTrue(count > 0, "Il doit y avoir au moins un don");
    }

    // ✅ TEST 4 : Total des dons > 0
    @Test
    @Order(4)
    void testGetTotalDons() throws SQLException {
        double total = ds.getTotalDons();
        assertTrue(total > 0, "Le total des dons doit être > 0");
    }

    // ✅ TEST 5 : Supprimer un don
    @Test
    @Order(5)
    void testSupprimerDon() throws SQLException {
        ArrayList<Don> liste = ds.afficherAll();
        Don d = liste.get(liste.size() - 1);
        idDonTest = d.getId();

        assertDoesNotThrow(() -> ds.supprimer(idDonTest));

        ArrayList<Don> apres = ds.afficherAll();
        boolean existe = apres.stream().anyMatch(don -> don.getId() == idDonTest);
        assertFalse(existe, "Le don doit être supprimé");
    }

    // ✅ TEST 6 : Don sur projet avec objectif atteint → exception attendue
    @Test
    @Order(6)
    void testDonProjetObjectifAtteint() throws SQLException {
        // Créer un projet puis forcer montant_collecte = objectif via un don paid
        Projet p = new Projet(
                "Projet Complet",
                "Projet dont l objectif est atteint",
                100.0, 0.0,
                Date.valueOf("2026-01-01"),
                Date.valueOf("2026-12-31")
        );
        ps.ajouter(p);
        ArrayList<Projet> liste = ps.afficherAll();
        int idComplet = liste.get(liste.size() - 1).getId();

        // Ajouter un don paid de 100 → montant_collecte devient 100 = objectif
        Don premierDon = new Don(
                100.0, Date.valueOf("2026-04-13"), "sur_place",
                "test@gmail.com", "Message", idComplet,
                "Test", "User", null, "paid"
        );
        ds.ajouter(premierDon);

        // Maintenant tenter un 2ème don → doit lancer une exception
        Don d = new Don(
                50.0,
                Date.valueOf("2026-04-13"),
                "sur_place",
                "test@gmail.com",
                "Message",
                idComplet,
                "Test", "User", null, "paid"
        );

        // Doit lancer une exception car objectif atteint
        assertThrows(SQLException.class, () -> ds.ajouter(d));

        // Nettoyage
        // Supprimer d'abord le don lié avant de supprimer le projet
        ArrayList<Don> dons = ds.afficherAll();
        dons.stream()
            .filter(don -> don.getProjetId() == idComplet)
            .forEach(don -> { try { ds.supprimer(don.getId()); } catch (Exception ignored) {} });
        ps.supprimer(idComplet);
    }

    @AfterAll
    static void cleanup() throws SQLException {
        // Supprimer le projet temporaire créé pour les tests
        try { ps.supprimer(idProjetTemp); } catch (Exception ignored) {}
    }
}
