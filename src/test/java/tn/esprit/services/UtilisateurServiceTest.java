package tn.esprit.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.esprit.models.Utilisateur;

import java.sql.*;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests UtilisateurService")
class UtilisateurServiceTest {

    @Mock private Connection mockCnx;
    @Mock private PreparedStatement mockPs;
    @Mock private Statement mockSt;
    @Mock private ResultSet mockRs;
    @Mock private ResultSetMetaData mockMeta;

    private UtilisateurService service;

    @BeforeEach
    void setUp() throws Exception {
        // Injecter la connexion mockée via réflexion
        service = new UtilisateurService();
        var field = UtilisateurService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, mockCnx);
    }

    // ─────────────────────────────────────────────────────────
    // afficher()
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("afficher() retourne une liste vide si connexion null")
    void testAfficher_connexionNull() throws Exception {
        var field = UtilisateurService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, null);

        List<Utilisateur> result = service.afficher();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("afficher() retourne la liste des utilisateurs")
    void testAfficher_retourneListe() throws Exception {
        when(mockCnx.createStatement()).thenReturn(mockSt);
        when(mockSt.executeQuery("SELECT * FROM utilisateur")).thenReturn(mockRs);

        // Simuler 2 lignes
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getInt("idUtilisateur")).thenReturn(1, 2);
        when(mockRs.getString("nom")).thenReturn("Dupont", "Martin");
        when(mockRs.getString("prenom")).thenReturn("Jean", "Sophie");
        when(mockRs.getString("email")).thenReturn("jean@test.com", "sophie@test.com");
        when(mockRs.getString("telephone")).thenReturn("12345678", "87654321");
        when(mockRs.getInt("idRole")).thenReturn(3, 1);
        when(mockRs.getString("specialite")).thenReturn("Cardio", null);
        when(mockRs.getDate("dateCreation")).thenReturn(null);

        // Mock métadonnées pour isActive
        when(mockRs.getMetaData()).thenReturn(mockMeta);
        when(mockMeta.getColumnCount()).thenReturn(1);
        when(mockMeta.getColumnName(1)).thenReturn("isActive");
        when(mockRs.getBoolean(1)).thenReturn(true);

        // Mock motDePasse
        when(mockRs.getString("motDePasse")).thenReturn("hash1", "hash2");

        List<Utilisateur> result = service.afficher();

        assertEquals(2, result.size());
        assertEquals("Dupont", result.get(0).getNom());
        assertEquals("Martin", result.get(1).getNom());
    }

    // ─────────────────────────────────────────────────────────
    // setActiveStatus()
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("setActiveStatus() retourne false si connexion null")
    void testSetActiveStatus_connexionNull() throws Exception {
        var field = UtilisateurService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, null);

        assertFalse(service.setActiveStatus(1, true));
    }

    @Test
    @DisplayName("setActiveStatus() retourne true si update réussi")
    void testSetActiveStatus_succes() throws Exception {
        when(mockCnx.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        assertTrue(service.setActiveStatus(5, true));
        verify(mockPs).setBoolean(1, true);
        verify(mockPs).setInt(2, 5);
    }

    @Test
    @DisplayName("setActiveStatus() retourne false si aucune ligne modifiée")
    void testSetActiveStatus_echecAucuneLigne() throws Exception {
        when(mockCnx.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(0);

        assertFalse(service.setActiveStatus(99, false));
    }

    // ─────────────────────────────────────────────────────────
    // supprimer()
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("supprimer() exécute le DELETE avec le bon ID")
    void testSupprimer() throws Exception {
        when(mockCnx.prepareStatement("DELETE FROM utilisateur WHERE idUtilisateur=?"))
            .thenReturn(mockPs);

        service.supprimer(7);

        verify(mockPs).setInt(1, 7);
        verify(mockPs).executeUpdate();
    }

    @Test
    @DisplayName("supprimer() ne plante pas si connexion null")
    void testSupprimer_connexionNull() throws Exception {
        var field = UtilisateurService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, null);

        assertDoesNotThrow(() -> service.supprimer(1));
    }

    // ─────────────────────────────────────────────────────────
    // updateProfile()
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile() retourne true si mise à jour réussie")
    void testUpdateProfile_succes() throws Exception {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(3);
        u.setNom("Nouveau");
        u.setPrenom("Prenom");
        u.setTelephone("99999999");

        when(mockCnx.prepareStatement(
            "UPDATE utilisateur SET nom=?, prenom=?, telephone=? WHERE idUtilisateur=?"))
            .thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        assertTrue(service.updateProfile(u));
        verify(mockPs).setString(1, "Nouveau");
        verify(mockPs).setString(2, "Prenom");
        verify(mockPs).setString(3, "99999999");
        verify(mockPs).setInt(4, 3);
    }

    @Test
    @DisplayName("updateProfile() retourne false si connexion null")
    void testUpdateProfile_connexionNull() throws Exception {
        var field = UtilisateurService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, null);

        assertFalse(service.updateProfile(new Utilisateur()));
    }
}
