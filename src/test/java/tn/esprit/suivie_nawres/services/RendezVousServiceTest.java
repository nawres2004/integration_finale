package tn.esprit.suivie_nawres.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests RendezVousService - CRUD Operations")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RendezVousServiceTest {

    private static RendezVousService rendezVousService;

    @BeforeAll
    static void setUpAll() {
        rendezVousService = new RendezVousService();
        System.out.println("✓ RendezVousService initialisé pour tous les tests");
    }

    private RendezVous rendezVous(int id, StatutRendezVous statut) {
        return new RendezVous(
                id,
                "Ben Ali",
                "Nour",
                LocalDate.now().plusDays(5),
                LocalTime.of(10, 30),
                "HAUTE",
                "EN_LIGNE",
                statut,
                "Consultation générale",
                "Tunisie",
                "+216 98 123 456"
        );
    }

    private void cleanup(int id) throws SQLException {
        if (rendezVousService.existe(id)) {
            rendezVousService.supprimerRendezVous(id);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Ajouter un rendez-vous valide")
    void testAjouterRendezVousValide() throws SQLException {
        int id = 9101;
        cleanup(id);

        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        assertTrue(rendezVousService.existe(id));
        cleanup(id);
    }

    @Test
    @Order(2)
    @DisplayName("Récupérer un rendez-vous par ID")
    void testChercherParIdExistant() throws SQLException {
        int id = 9102;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        Optional<RendezVous> rdv = rendezVousService.chercherParId(id);

        assertTrue(rdv.isPresent());
        assertEquals(id, rdv.get().getUtilisateurId());
        assertEquals("Ben Ali", rdv.get().getNom());
        assertEquals("Nour", rdv.get().getPrenom());
        assertEquals("HAUTE", rdv.get().getPriorite());
        assertEquals("EN_LIGNE", rdv.get().getModeConsultation());
        cleanup(id);
    }

    @Test
    @Order(3)
    @DisplayName("Rechercher un rendez-vous qui n'existe pas")
    void testChercherParIdNonExistant() throws SQLException {
        assertFalse(rendezVousService.chercherParId(99999).isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("Modifier un rendez-vous existant")
    void testModifierRendezVousExistant() throws SQLException {
        int id = 9103;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        RendezVous modifie = rendezVous(id, StatutRendezVous.EN_ATTENTE);
        modifie.setPriorite("BASSE");
        modifie.setModeConsultation("EN_PERSONNE");
        modifie.setNotesRendezVous("Consultation modifiée");
        rendezVousService.modifierRendezVous(modifie);

        Optional<RendezVous> rdvApres = rendezVousService.chercherParId(id);
        assertTrue(rdvApres.isPresent());
        assertEquals("BASSE", rdvApres.get().getPriorite());
        assertEquals("EN_PERSONNE", rdvApres.get().getModeConsultation());
        assertEquals("Consultation modifiée", rdvApres.get().getNotesRendezVous());
        cleanup(id);
    }

    @Test
    @Order(5)
    @DisplayName("Changer le statut d'un rendez-vous en ACCEPTE")
    void testChangerStatutEnAccepte() throws SQLException {
        int id = 9104;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        rendezVousService.changerStatutRendezVous(id, StatutRendezVous.ACCEPTE);

        Optional<RendezVous> rdv = rendezVousService.chercherParId(id);
        assertTrue(rdv.isPresent());
        assertEquals(StatutRendezVous.ACCEPTE, rdv.get().getStatutRendezVous());
        cleanup(id);
    }

    @Test
    @Order(6)
    @DisplayName("Changer le statut d'un rendez-vous en REFUSE")
    void testChangerStatutEnRefuse() throws SQLException {
        int id = 9105;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        rendezVousService.changerStatutRendezVous(id, StatutRendezVous.REFUSE);

        Optional<RendezVous> rdv = rendezVousService.chercherParId(id);
        assertTrue(rdv.isPresent());
        assertEquals(StatutRendezVous.REFUSE, rdv.get().getStatutRendezVous());
        cleanup(id);
    }

    @Test
    @Order(7)
    @DisplayName("Afficher tous les rendez-vous")
    void testAfficherRendezVous() throws SQLException {
        int id = 9106;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        List<RendezVous> rendezVousList = rendezVousService.afficherRendezVous();

        assertNotNull(rendezVousList);
        assertTrue(rendezVousList.stream().anyMatch(rdv -> rdv.getUtilisateurId() == id));
        cleanup(id);
    }

    @Test
    @Order(8)
    @DisplayName("Vérifier l'existence d'un rendez-vous")
    void testExisteRendezVous() throws SQLException {
        int id = 9107;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        assertTrue(rendezVousService.existe(id));
        assertFalse(rendezVousService.existe(99999));
        cleanup(id);
    }

    @Test
    @Order(9)
    @DisplayName("Supprimer un rendez-vous existant")
    void testSupprimerRendezVousExistant() throws SQLException {
        int id = 9108;
        cleanup(id);
        rendezVousService.ajouterRendezVous(rendezVous(id, StatutRendezVous.EN_ATTENTE));

        rendezVousService.supprimerRendezVous(id);

        assertFalse(rendezVousService.existe(id));
    }

    @Test
    @Order(10)
    @DisplayName("Supprimer un rendez-vous inexistant")
    void testSupprimerRendezVousNonExistant() throws SQLException {
        rendezVousService.supprimerRendezVous(99999);
        assertTrue(true);
    }
}

