package tn.esprit.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du modèle Role")
class RoleTest {

    @Test
    @DisplayName("Constructeur avec ID initialise correctement")
    void testConstructeurAvecId() {
        Role role = new Role(3, "ROLE_MEDECIN", "Médecin", new Date(), true);
        assertEquals(3,             role.getIdRole());
        assertEquals("ROLE_MEDECIN", role.getNomRole());
        assertEquals("Médecin",     role.getDescription());
        assertTrue(role.isStatut());
    }

    @Test
    @DisplayName("toString retourne le nomRole")
    void testToString() {
        Role role = new Role(1, "ROLE_PATIENT", "Patient", new Date(), true);
        assertEquals("ROLE_PATIENT", role.toString());
    }

    @Test
    @DisplayName("Setters fonctionnent correctement")
    void testSetters() {
        Role role = new Role();
        role.setIdRole(4);
        role.setNomRole("ROLE_ADMIN");
        role.setStatut(false);

        assertEquals(4,            role.getIdRole());
        assertEquals("ROLE_ADMIN", role.getNomRole());
        assertFalse(role.isStatut());
    }
}
