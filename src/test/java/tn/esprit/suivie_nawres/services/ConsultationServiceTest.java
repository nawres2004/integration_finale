package tn.esprit.suivie_nawres.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import tn.esprit.suivie_nawres.models.Consultation;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests ConsultationService - CRUD Operations")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConsultationServiceTest {

    private static ConsultationService consultationService;

    @BeforeAll
    static void setUpAll() {
        consultationService = new ConsultationService();
        System.out.println("✓ ConsultationService initialisé pour tous les tests");
    }

    private Consultation consultation(int id, BigDecimal cout) {
        return new Consultation(
                id,
                "Trabelsi",
                "Yasmine",
                LocalDate.now(),
                LocalTime.of(14, 30),
                "EN_LIGNE",
                "Grippe",
                "Grippe saisonnière",
                "Repos et antiviraux",
                "Test sanguin",
                "Consultation en ligne effectuée",
                cout
        );
    }

    private void cleanup(int id) throws SQLException {
        if (consultationService.existe(id)) {
            consultationService.supprimerConsultation(id);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Ajouter une consultation valide")
    void testAjouterConsultationValide() throws SQLException {
        int id = 9201;
        cleanup(id);

        consultationService.ajouterConsultation(consultation(id, new BigDecimal("50.00")));

        assertTrue(consultationService.existe(id));
        cleanup(id);
    }

    @Test
    @Order(2)
    @DisplayName("Récupérer une consultation par ID")
    void testChercherParIdExistant() throws SQLException {
        int id = 9202;
        cleanup(id);
        consultationService.ajouterConsultation(consultation(id, new BigDecimal("50.00")));

        Optional<Consultation> consultation = consultationService.chercherParId(id);

        assertTrue(consultation.isPresent());
        assertEquals(id, consultation.get().getUtilisateurId());
        assertEquals("Trabelsi", consultation.get().getNom());
        assertEquals("Grippe", consultation.get().getMaladie());
        assertEquals("Grippe saisonnière", consultation.get().getDiagnostic());
        assertEquals("EN_LIGNE", consultation.get().getModeConsultation());
        assertEquals(0, consultation.get().getCoutConsultation().compareTo(new BigDecimal("50.00")));
        cleanup(id);
    }

    @Test
    @Order(3)
    @DisplayName("Rechercher une consultation qui n'existe pas")
    void testChercherParIdNonExistant() throws SQLException {
        assertFalse(consultationService.chercherParId(99999).isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("Modifier une consultation existante")
    void testModifierConsultationExistante() throws SQLException {
        int id = 9203;
        cleanup(id);
        consultationService.ajouterConsultation(consultation(id, new BigDecimal("50.00")));

        Consultation modifiee = consultation(id, new BigDecimal("30.00"));
        modifiee.setMaladie("Rhume");
        modifiee.setDiagnostic("Rhume banal");
        modifiee.setTraitement("Repos et tisanes");
        modifiee.setNotesConsultation("Consultation modifiée");
        consultationService.modifierConsultation(modifiee);

        Optional<Consultation> consultationApres = consultationService.chercherParId(id);
        assertTrue(consultationApres.isPresent());
        assertEquals("Rhume", consultationApres.get().getMaladie());
        assertEquals("Rhume banal", consultationApres.get().getDiagnostic());
        assertEquals("Repos et tisanes", consultationApres.get().getTraitement());
        assertEquals(0, consultationApres.get().getCoutConsultation().compareTo(new BigDecimal("30.00")));
        assertEquals("Consultation modifiée", consultationApres.get().getNotesConsultation());
        cleanup(id);
    }

    @Test
    @Order(5)
    @DisplayName("Afficher toutes les consultations")
    void testAfficherConsultations() throws SQLException {
        int id = 9204;
        cleanup(id);
        consultationService.ajouterConsultation(consultation(id, new BigDecimal("50.00")));

        List<Consultation> consultations = consultationService.afficherConsultations();

        assertNotNull(consultations);
        assertTrue(consultations.stream().anyMatch(c -> c.getUtilisateurId() == id));
        cleanup(id);
    }

    @Test
    @Order(6)
    @DisplayName("Vérifier l'existence d'une consultation")
    void testExisteConsultation() throws SQLException {
        int id = 9205;
        cleanup(id);
        consultationService.ajouterConsultation(consultation(id, new BigDecimal("50.00")));

        assertTrue(consultationService.existe(id));
        assertFalse(consultationService.existe(99999));
        cleanup(id);
    }

    @Test
    @Order(7)
    @DisplayName("Supprimer une consultation existante")
    void testSupprimerConsultationExistante() throws SQLException {
        int id = 9206;
        cleanup(id);
        consultationService.ajouterConsultation(consultation(id, new BigDecimal("50.00")));

        consultationService.supprimerConsultation(id);

        assertFalse(consultationService.existe(id));
    }

    @Test
    @Order(8)
    @DisplayName("Supprimer une consultation inexistante")
    void testSupprimerConsultationNonExistante() throws SQLException {
        consultationService.supprimerConsultation(99999);
        assertTrue(true);
    }

    @Test
    @Order(9)
    @DisplayName("Tester avec coût de consultation null")
    void testConsultationAvecCoutNull() throws SQLException {
        int id = 9207;
        cleanup(id);

        consultationService.ajouterConsultation(consultation(id, null));

        Optional<Consultation> retrieved = consultationService.chercherParId(id);
        assertTrue(retrieved.isPresent());
        assertEquals(0, retrieved.get().getCoutConsultation().compareTo(BigDecimal.ZERO));
        cleanup(id);
    }

    @Test
    @Order(10)
    @DisplayName("Tester avec données de consultation complètes")
    void testConsultationAvecDonneesCompletes() throws SQLException {
        int id = 9208;
        cleanup(id);

        Consultation consultationComplete = new Consultation(
                id,
                "Gharbi",
                "Sami",
                LocalDate.now().minusDays(2),
                LocalTime.of(11, 0),
                "EN_PERSONNE",
                "Douleurs dorsales",
                "Hernie discale",
                "Physiothérapie et anti-inflammatoires",
                "Radiographie, IRM",
                "Consultation approfondie effectuée avec succès",
                new BigDecimal("120.50")
        );

        consultationService.ajouterConsultation(consultationComplete);
        Optional<Consultation> retrieved = consultationService.chercherParId(id);

        assertTrue(retrieved.isPresent());
        assertEquals("Hernie discale", retrieved.get().getDiagnostic());
        assertEquals(0, retrieved.get().getCoutConsultation().compareTo(new BigDecimal("120.50")));
        cleanup(id);
    }
}

