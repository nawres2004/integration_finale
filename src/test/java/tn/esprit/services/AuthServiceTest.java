package tn.esprit.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tn.esprit.models.Utilisateur;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests AuthService")
class AuthServiceTest {

    @Mock private Connection mockCnx;
    @Mock private PreparedStatement mockPs;
    @Mock private ResultSet mockRs;
    @Mock private ResultSetMetaData mockMeta;
    @Mock private DatabaseMetaData mockDbMeta;

    private AuthService service;

    @BeforeEach
    void setUp() throws Exception {
        // Stub getMetaData pour ensureActiveColumnExists()
        when(mockCnx.getMetaData()).thenReturn(mockDbMeta);
        ResultSet emptyRs = mock(ResultSet.class);
        when(emptyRs.next()).thenReturn(true); // colonne existe déjà
        when(mockDbMeta.getColumns(null, null, "utilisateur", "isActive")).thenReturn(emptyRs);

        service = new AuthService();
        var field = AuthService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, mockCnx);
    }

    // ─────────────────────────────────────────────────────────
    // login()
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("login() retourne null si connexion null")
    void testLogin_connexionNull() throws Exception {
        var field = AuthService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, null);

        assertNull(service.login("test@test.com", "pass"));
    }

    @Test
    @DisplayName("login() retourne null si email introuvable")
    void testLogin_emailIntrouvable() throws Exception {
        when(mockCnx.prepareStatement("SELECT * FROM utilisateur WHERE email = ?"))
            .thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        assertNull(service.login("inconnu@test.com", "pass"));
    }

    @Test
    @DisplayName("login() retourne null si mot de passe incorrect")
    void testLogin_mauvaisMotDePasse() throws Exception {
        String hash = BCrypt.hashpw("bonMotDePasse", BCrypt.gensalt(4));

        when(mockCnx.prepareStatement("SELECT * FROM utilisateur WHERE email = ?"))
            .thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("motDePasse")).thenReturn(hash);

        assertNull(service.login("test@test.com", "mauvaisPass"));
    }

    @Test
    @DisplayName("login() retourne l'utilisateur si identifiants corrects")
    void testLogin_succes() throws Exception {
        String hash = BCrypt.hashpw("MonPass1!", BCrypt.gensalt(4));

        when(mockCnx.prepareStatement("SELECT * FROM utilisateur WHERE email = ?"))
            .thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("motDePasse")).thenReturn(hash);
        when(mockRs.getInt("idUtilisateur")).thenReturn(10);
        when(mockRs.getString("nom")).thenReturn("Dupont");
        when(mockRs.getString("prenom")).thenReturn("Jean");
        when(mockRs.getString("email")).thenReturn("jean@test.com");
        when(mockRs.getString("telephone")).thenReturn("12345678");
        when(mockRs.getInt("idRole")).thenReturn(1);
        when(mockRs.getString("specialite")).thenReturn(null);
        when(mockRs.getDate("dateCreation")).thenReturn(null);

        // isActive
        when(mockRs.getBoolean("isActive")).thenReturn(true);

        Utilisateur result = service.login("jean@test.com", "MonPass1!");

        assertNotNull(result);
        assertEquals(10,             result.getIdUtilisateur());
        assertEquals("Dupont",       result.getNom());
        assertEquals("jean@test.com", result.getEmail());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("login() fonctionne avec mot de passe en clair (anciens comptes)")
    void testLogin_motDePasseClair() throws Exception {
        when(mockCnx.prepareStatement("SELECT * FROM utilisateur WHERE email = ?"))
            .thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("motDePasse")).thenReturn("plaintext123");
        when(mockRs.getInt("idUtilisateur")).thenReturn(5);
        when(mockRs.getString("nom")).thenReturn("Test");
        when(mockRs.getString("prenom")).thenReturn("User");
        when(mockRs.getString("email")).thenReturn("test@test.com");
        when(mockRs.getString("telephone")).thenReturn(null);
        when(mockRs.getInt("idRole")).thenReturn(4);
        when(mockRs.getString("specialite")).thenReturn(null);
        when(mockRs.getDate("dateCreation")).thenReturn(null);
        when(mockRs.getBoolean("isActive")).thenReturn(true);

        Utilisateur result = service.login("test@test.com", "plaintext123");
        assertNotNull(result);
        assertEquals(5, result.getIdUtilisateur());
    }

    // ─────────────────────────────────────────────────────────
    // updatePassword()
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePassword() retourne true si update réussi")
    void testUpdatePassword_succes() throws Exception {
        when(mockCnx.prepareStatement(
            "UPDATE utilisateur SET motDePasse = ? WHERE idUtilisateur = ?"))
            .thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(1);

        assertTrue(service.updatePassword(3, "NouveauPass1!"));
        verify(mockPs).setInt(2, 3);
    }

    @Test
    @DisplayName("updatePassword() retourne false si aucune ligne modifiée")
    void testUpdatePassword_echecAucuneLigne() throws Exception {
        when(mockCnx.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeUpdate()).thenReturn(0);

        assertFalse(service.updatePassword(99, "Pass1234!"));
    }

    @Test
    @DisplayName("updatePassword() retourne false si connexion null")
    void testUpdatePassword_connexionNull() throws Exception {
        var field = AuthService.class.getDeclaredField("cnx");
        field.setAccessible(true);
        field.set(service, null);

        assertFalse(service.updatePassword(1, "pass"));
    }
}
