package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.models.Utilisateur;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests SessionService")
class SessionServiceTest {

    private SessionService session;

    @BeforeEach
    void setUp() {
        session = SessionService.getInstance();
        session.clearSession(); // état propre avant chaque test
    }

    @Test
    @DisplayName("getInstance() retourne toujours la même instance (singleton)")
    void testSingleton() {
        SessionService s1 = SessionService.getInstance();
        SessionService s2 = SessionService.getInstance();
        assertSame(s1, s2);
    }

    @Test
    @DisplayName("getCurrentUser() retourne null après clearSession()")
    void testClearSession() {
        Utilisateur u = new Utilisateur(1, "A", "B", "a@b.com",
                "pass", "123", 1, null, new Date(), true);
        session.setCurrentUser(u);
        session.clearSession();

        assertNull(session.getCurrentUser());
        assertFalse(session.isLoggedIn());
    }

    @Test
    @DisplayName("setCurrentUser() stocke l'utilisateur correctement")
    void testSetCurrentUser() {
        Utilisateur u = new Utilisateur(2, "Dupont", "Jean", "jean@test.com",
                "hash", "12345678", 3, "Cardio", new Date(), true);
        session.setCurrentUser(u);

        assertNotNull(session.getCurrentUser());
        assertEquals(2,       session.getCurrentUser().getIdUtilisateur());
        assertEquals("Dupont", session.getCurrentUser().getNom());
        assertTrue(session.isLoggedIn());
    }

    @Test
    @DisplayName("isLoggedIn() retourne false si aucun utilisateur en session")
    void testIsLoggedIn_false() {
        assertFalse(session.isLoggedIn());
    }

    @Test
    @DisplayName("isLoggedIn() retourne true après connexion")
    void testIsLoggedIn_true() {
        session.setCurrentUser(new Utilisateur());
        assertTrue(session.isLoggedIn());
    }

    @Test
    @DisplayName("setCurrentUser() remplace l'utilisateur existant")
    void testRemplacement() {
        Utilisateur u1 = new Utilisateur();
        u1.setIdUtilisateur(1);
        u1.setNom("Premier");

        Utilisateur u2 = new Utilisateur();
        u2.setIdUtilisateur(2);
        u2.setNom("Deuxième");

        session.setCurrentUser(u1);
        session.setCurrentUser(u2);

        assertEquals(2,          session.getCurrentUser().getIdUtilisateur());
        assertEquals("Deuxième", session.getCurrentUser().getNom());
    }
}
