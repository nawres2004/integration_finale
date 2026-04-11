package tn.esprit.suivie_nawres.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Smoke tests pour RendezVousServiceMockTest")
class RendezVousServiceMockTest {

    @Test
    @DisplayName("Le modèle RendezVous peut être instancié sans Mockito")
    void testModeleRendezVous() {
        RendezVous rendezVous = new RendezVous(
                1,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 30),
                "HAUTE",
                "EN_LIGNE",
                StatutRendezVous.EN_ATTENTE,
                "Consultation générale",
                "Tunisie",
                "+216 98 123 456"
        );

        assertNotNull(rendezVous);
        assertEquals(1, rendezVous.getUtilisateurId());
        assertEquals(StatutRendezVous.EN_ATTENTE, rendezVous.getStatutRendezVous());
        assertEquals("HAUTE", rendezVous.getPriorite());
        assertEquals("EN_LIGNE", rendezVous.getModeConsultation());
    }
}

