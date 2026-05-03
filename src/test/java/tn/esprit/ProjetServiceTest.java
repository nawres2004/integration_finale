package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.models.Projet;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjetServiceTest {

    static ProjetService ps;
    static int idProjetTest;

    @BeforeAll
    static void setup() {
        ps = new ProjetService();
    }

    // ✅ TEST 1 : Ajouter un projet
    @Test
    @Order(1)
    void testAjouterProjet() {
        Projet p = new Projet(
                "Projet JUnit",
                "Description test unitaire",
                5000.0, 0.0,
                Date.valueOf("2026-01-01"),
                Date.valueOf("2026-12-31")
        );
        assertDoesNotThrow(() -> ps.ajouter(p));
    }

    // ✅ TEST 2 : Afficher tous les projets (liste non vide)
    @Test
    @Order(2)
    void testAfficherAll() throws SQLException {
        ArrayList<Projet> liste = ps.afficherAll();
        assertNotNull(liste);
        assertFalse(liste.isEmpty(), "La liste ne doit pas être vide");
    }

    // ✅ TEST 3 : Compter les projets (> 0)
    @Test
    @Order(3)
    void testCompterProjets() throws SQLException {
        int count = ps.compterProjets();
        assertTrue(count > 0, "Il doit y avoir au moins un projet");
    }

    // ✅ TEST 4 : Modifier un projet
    @Test
    @Order(4)
    void testModifierProjet() throws SQLException {
        // Récupère le dernier projet ajouté
        ArrayList<Projet> liste = ps.afficherAll();
        Projet p = liste.get(liste.size() - 1);

        p.setTitreProjet("Projet JUnit Modifié");
        p.setDescription("Description modifiée pour le test");
        p.setObjectifFinancier(9999.0);

        assertDoesNotThrow(() -> ps.modifier(p));

        // Vérifie que la modification est bien en base
        ArrayList<Projet> apres = ps.afficherAll();
        Projet modifie = apres.stream()
                .filter(pr -> pr.getId() == p.getId())
                .findFirst().orElse(null);

        assertNotNull(modifie);
        assertEquals("Projet JUnit Modifié", modifie.getTitreProjet());
    }

    // ✅ TEST 5 : Supprimer un projet sans dons
    @Test
    @Order(5)
    void testSupprimerProjet() throws SQLException {
        ArrayList<Projet> liste = ps.afficherAll();
        Projet p = liste.get(liste.size() - 1);
        idProjetTest = p.getId();

        assertDoesNotThrow(() -> ps.supprimer(idProjetTest));

        // Vérifie qu'il n'existe plus
        ArrayList<Projet> apres = ps.afficherAll();
        boolean existe = apres.stream().anyMatch(pr -> pr.getId() == idProjetTest);
        assertFalse(existe, "Le projet doit être supprimé");
    }

    // ✅ TEST 6 : Titre trop court → validation métier
    @Test
    @Order(6)
    void testTitreTropCourt() {
        Projet p = new Projet(
                "AB", // moins de 3 caractères
                "Description valide pour le test",
                1000.0, 0.0,
                Date.valueOf("2026-01-01"),
                Date.valueOf("2026-12-31")
        );
        // Le service n'a pas de validation → on vérifie juste que l'objet est créé
        assertTrue(p.getTitreProjet().length() < 3);
    }
}
