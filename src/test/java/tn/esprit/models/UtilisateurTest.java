package tn.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du modèle Utilisateur")
class UtilisateurTest {

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur(
                1, "Dupont", "Jean", "jean@test.com",
                "hashedPass", "12345678", 3,
                "Cardiologie", new Date(), true
        );
    }

    @Test
    @DisplayName("Constructeur avec ID initialise correctement tous les champs")
    void testConstructeurAvecId() {
        assertEquals(1,             utilisateur.getIdUtilisateur());
        assertEquals("Dupont",      utilisateur.getNom());
        assertEquals("Jean",        utilisateur.getPrenom());
        assertEquals("jean@test.com", utilisateur.getEmail());
        assertEquals("hashedPass",  utilisateur.getMotDePasse());
        assertEquals("12345678",    utilisateur.getTelephone());
        assertEquals(3,             utilisateur.getIdRole());
        assertEquals("Cardiologie", utilisateur.getSpecialite());
        assertTrue(utilisateur.isActive());
    }

    @Test
    @DisplayName("Constructeur vide crée un objet non null")
    void testConstructeurVide() {
        Utilisateur u = new Utilisateur();
        assertNotNull(u);
    }

    @Test
    @DisplayName("Setters modifient correctement les valeurs")
    void testSetters() {
        utilisateur.setNom("Martin");
        utilisateur.setPrenom("Sophie");
        utilisateur.setEmail("sophie@test.com");
        utilisateur.setTelephone("87654321");
        utilisateur.setIdRole(1);
        utilisateur.setActive(false);

        assertEquals("Martin",          utilisateur.getNom());
        assertEquals("Sophie",          utilisateur.getPrenom());
        assertEquals("sophie@test.com", utilisateur.getEmail());
        assertEquals("87654321",        utilisateur.getTelephone());
        assertEquals(1,                 utilisateur.getIdRole());
        assertFalse(utilisateur.isActive());
    }

    @Test
    @DisplayName("isActive retourne false après désactivation")
    void testDesactivation() {
        utilisateur.setActive(false);
        assertFalse(utilisateur.isActive());
    }

    @Test
    @DisplayName("toString ne retourne pas null")
    void testToString() {
        assertNotNull(utilisateur.toString());
        assertTrue(utilisateur.toString().contains("Dupont"));
    }

    @Test
    @DisplayName("Constructeur sans ID initialise correctement")
    void testConstructeurSansId() {
        Utilisateur u = new Utilisateur(
                "Nom", "Prenom", "email@test.com",
                "pass", "00000000", 1,
                null, new Date(), true
        );
        assertEquals("Nom",   u.getNom());
        assertEquals(1,       u.getIdRole());
        assertEquals(0,       u.getIdUtilisateur()); // pas encore en DB
    }
}
