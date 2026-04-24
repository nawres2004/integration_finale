package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.RendezVousService;
import tn.esprit.suivie_nawres.utils.RoleContext;
import tn.esprit.suivie_nawres.utils.UserRole;

import java.time.LocalTime;
import java.sql.SQLException;

/**
 * 🎯 CONTRÔLEUR : Gère le formulaire d'ajout/modification de rendez-vous
 * 
 * Ce contrôleur fait 3 choses principales :
 * 1. Affiche le formulaire de rendez-vous
 * 2. VALIDE tous les champs (contrôle de saisie) ⭐
 * 3. Sauvegarde dans la base de données
 */
public class AjouterRendezVousController {
    
    // ========================================
    // 📋 REGEX : Règle pour valider Nom/Prénom
    // ========================================
    // Cette règle dit : "Commence par une lettre, puis 1-49 lettres/espaces/tirets/apostrophes"
    // Exemples valides : "Ahmed", "Ben Ali", "O'Connor", "Jean-Pierre"
    // Exemples invalides : "123", "Ahmed123", "A" (trop court)
    private static final String NOM_PRENOM_REGEX = "^[\\p{L}][\\p{L} '\\-]{1,49}$";
    
    // Variables statiques pour le mode édition
    private static RendezVous rendezVousAEditer;
    private static AfficherRendezVousMedecinController parentMedecinController;
    private static boolean fermerApresEnregistrement;

    // ========================================
    // 🎨 CHAMPS DU FORMULAIRE (liés au FXML)
    // ========================================
    // @FXML signifie : "Ce champ est lié à un élément dans le fichier FXML"
    // Par exemple : txtNom est lié à <TextField fx:id="txtNom" /> dans le FXML
    
    // ❌ SUPPRIMÉ : @FXML private TextField txtUtilisateurId;  // L'ID est maintenant auto-généré !
    @FXML private TextField txtNom;                  // Champ : Nom du patient
    @FXML private TextField txtPrenom;               // Champ : Prénom du patient
    @FXML private DatePicker datePickerRendezVous;   // Sélecteur de date
    @FXML private TextField txtHeureRendezVous;      // Champ : Heure (format HH:mm)
    @FXML private ComboBox<String> comboPriorite;    // Liste déroulante : Priorité
    @FXML private ComboBox<String> comboModeConsultation; // Liste déroulante : Mode
    @FXML private TextArea txtNotesRendezVous;       // Zone de texte : Notes
    @FXML private TextField txtPays;                 // Champ : Pays
    @FXML private TextField txtTelephone;            // Champ : Téléphone
    @FXML private Label lblMessage;                  // Label pour afficher les messages

    // ========================================
    // 🔧 SERVICES ET VARIABLES
    // ========================================
    private final RendezVousService rendezVousService = new RendezVousService(); // Service pour accéder à la DB
    private StatutRendezVous statutEdition = StatutRendezVous.EN_ATTENTE;        // Statut par défaut
    private boolean modeEdition;                                                  // true = modification, false = création

    public static void setRendezVousAEditer(RendezVous rendezVous) {
        rendezVousAEditer = rendezVous;
    }

    public static void setParentMedecinController(AfficherRendezVousMedecinController controller) {
        parentMedecinController = controller;
        fermerApresEnregistrement = true;
    }

    public static void setFermerApresEnregistrement(boolean fermer) {
        fermerApresEnregistrement = fermer;
    }

    /**
     * 🎬 FONCTION INITIALIZE
     * 
     * Cette fonction est appelée AUTOMATIQUEMENT quand le formulaire s'ouvre
     * Elle prépare les listes déroulantes et remplit le formulaire si on est en mode édition
     */
    @FXML
    private void initialize() {
        // Remplit la liste déroulante "Priorité" avec 4 options
        comboPriorite.setItems(FXCollections.observableArrayList("BASSE", "NORMALE", "HAUTE", "URGENTE"));
        
        // Remplit la liste déroulante "Mode Consultation" avec 3 options
        comboModeConsultation.setItems(FXCollections.observableArrayList("A_DISTANCE", "PRESENTIEL", "TELECONSULTATION"));
        
        // Sélectionne le premier élément par défaut
        comboPriorite.getSelectionModel().selectFirst();
        comboModeConsultation.getSelectionModel().selectFirst();
        
        // Vérifie si on est en mode édition (modifier un RDV existant)
        modeEdition = rendezVousAEditer != null;
        
        // Si on modifie un RDV existant, remplit le formulaire avec ses données
        if (rendezVousAEditer != null) {
            remplirFormulaire(rendezVousAEditer);
            // ❌ SUPPRIMÉ : txtUtilisateurId.setDisable(true);  // Plus besoin, l'ID n'est plus affiché
            rendezVousAEditer = null;
        }
    }

    /**
     * 💾 FONCTION ENREGISTRER
     * 
     * Cette fonction est appelée quand tu cliques sur le bouton "Enregistrer"
     * 
     * ÉTAPES :
     * 1. VALIDE tous les champs (contrôle de saisie) ⭐
     * 2. Si erreur → Affiche le message et STOP
     * 3. Si OK → Crée l'objet RendezVous
     * 4. Sauvegarde dans la base de données (l'ID est auto-généré)
     * 5. Affiche le message de succès
     */
    @FXML
    private void enregistrer() {
        try {
            // ========================================
            // ÉTAPE 1 : VALIDATION (Contrôle de saisie) ⭐
            // ========================================
            // Appelle la fonction qui vérifie TOUS les champs
            String erreurValidation = validerChampsObligatoires();
            
            // Si il y a une erreur (erreurValidation != null)
            if (erreurValidation != null) {
                afficherErreur(erreurValidation);  // Affiche le message d'erreur en ROUGE
                return;  // STOP ! Ne continue pas, ne sauvegarde pas
            }

            // ========================================
            // ÉTAPE 2 : CRÉATION DE L'OBJET RENDEZ-VOUS
            // ========================================
            // ✅ PLUS BESOIN de parseUtilisateurId() !
            // L'ID sera généré automatiquement par MySQL
            
            // Crée un nouvel objet RendezVous
            RendezVous rendezVous = new RendezVous();
            
            // Remplit l'objet avec les données du formulaire (SANS l'ID)
            rendezVous.setNom(txtNom.getText().trim());              // .trim() enlève les espaces au début/fin
            rendezVous.setPrenom(txtPrenom.getText().trim());
            rendezVous.setDateRendezVous(datePickerRendezVous.getValue());
            rendezVous.setHeureRendezVous(parseHeure(txtHeureRendezVous.getText().trim()));
            rendezVous.setPriorite(comboPriorite.getValue());
            rendezVous.setModeConsultation(comboModeConsultation.getValue());
            rendezVous.setStatutRendezVous(resolveStatutPourSauvegarde());
            rendezVous.setNotesRendezVous(txtNotesRendezVous.getText().trim());
            rendezVous.setPays(txtPays.getText().trim());
            rendezVous.setTelephone(txtTelephone.getText().trim());

            // ========================================
            // ÉTAPE 3 : SAUVEGARDE DANS LA BASE DE DONNÉES
            // ========================================
            if (modeEdition) {
                // Mode MODIFICATION : Met à jour le RDV existant
                rendezVousService.modifierRendezVous(rendezVous);
                afficherInfo("Rendez-vous modifie avec succes.");
            } else {
                // Mode CRÉATION : Ajoute un nouveau RDV
                // ✅ L'ID est généré automatiquement et retourné
                int idGenere = rendezVousService.ajouterRendezVous(rendezVous);
                afficherInfo("Rendez-vous ajoute avec succes ! ID genere : " + idGenere);
            }
            
            // ========================================
            // ÉTAPE 4 : ACTIONS APRÈS SAUVEGARDE
            // ========================================
            // Si c'est un patient, sauvegarde son ID dans le contexte
            if (RoleContext.getCurrentRole() == UserRole.PATIENT) {
                RoleContext.setCurrentUtilisateurId(rendezVous.getUtilisateurId());
            }
            
            // Rafraîchit la liste si on vient du dashboard médecin
            if (parentMedecinController != null) {
                parentMedecinController.rafraichirListe();
            }
            
            // Ferme la fenêtre ou vide le formulaire
            if (fermerApresEnregistrement) {
                fermerModale();
            } else {
                viderChamps();
            }
            
        } catch (IllegalArgumentException exception) {
            // Erreur de validation (ex: format invalide)
            afficherErreur(exception.getMessage());
        } catch (SQLException exception) {
            // Erreur de base de données
            afficherErreur("Erreur base de donnees : " + exception.getMessage());
        } catch (Exception exception) {
            // Autre erreur
            afficherErreur("Operation impossible. Verifie les champs saisis.");
        }
    }

    private void fermerModale() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) txtUtilisateurId.getScene().getWindow();
            stage.close();
            fermerApresEnregistrement = false;
        } catch (Exception e) {
            System.err.println("Erreur fermeture modale : " + e.getMessage());
        }
    }

    @FXML
    private void vider() {
        viderChamps();
        afficherInfo("Pret.");
    }

    private void viderChamps() {
        // ❌ SUPPRIMÉ : txtUtilisateurId.clear();  // Plus besoin, l'ID n'est plus affiché
        // ❌ SUPPRIMÉ : txtUtilisateurId.setDisable(false);
        txtNom.clear();
        txtPrenom.clear();
        datePickerRendezVous.setValue(null);
        txtHeureRendezVous.clear();
        comboPriorite.getSelectionModel().selectFirst();
        comboModeConsultation.getSelectionModel().selectFirst();
        txtNotesRendezVous.clear();
        txtPays.clear();
        txtTelephone.clear();
        statutEdition = StatutRendezVous.EN_ATTENTE;
        modeEdition = false;
    }

    private StatutRendezVous resolveStatutPourSauvegarde() {
        if (modeEdition) {
            return statutEdition;
        }
        return RoleContext.getCurrentRole() == UserRole.MEDECIN
                ? StatutRendezVous.ACCEPTE
                : StatutRendezVous.EN_ATTENTE;
    }

    /**
     * ✅ FONCTION DE VALIDATION COMPLÈTE ⭐⭐⭐
     * 
     * C'EST ICI QUE SE FAIT LE CONTRÔLE DE SAISIE !
     * 
     * Cette fonction vérifie TOUS les champs du formulaire
     * Si un champ est invalide, elle retourne un message d'erreur
     * Si tout est OK, elle retourne null
     * 
     * ✅ PLUS BESOIN de valider l'ID (il est auto-généré)
     * 
     * @return String - Message d'erreur OU null si tout est OK
     */
    private String validerChampsObligatoires() {
        
        // ❌ SUPPRIMÉ : Validation de l'ID (plus besoin, il est auto-généré)
        
        // ========================================
        // VALIDATION 1 : NOM (vide ?)
        // ========================================
        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            return "Le champ Nom est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 2 : NOM (format correct ?)
        // ========================================
        // Vérifie avec REGEX : seulement des lettres, espaces, tirets, apostrophes
        // Exemples valides : "Ahmed", "Ben Ali", "O'Connor"
        // Exemples invalides : "Ahmed123", "A" (trop court)
        if (!txtNom.getText().trim().matches(NOM_PRENOM_REGEX)) {
            return "Nom invalide : utilise uniquement des lettres, espaces, tiret ou apostrophe (2 a 50 caracteres).";
        }
        
        // ========================================
        // VALIDATION 3 : PRÉNOM (vide ?)
        // ========================================
        if (txtPrenom.getText() == null || txtPrenom.getText().trim().isEmpty()) {
            return "Le champ Prenom est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 4 : PRÉNOM (format correct ?)
        // ========================================
        if (!txtPrenom.getText().trim().matches(NOM_PRENOM_REGEX)) {
            return "Prenom invalide : utilise uniquement des lettres, espaces, tiret ou apostrophe (2 a 50 caracteres).";
        }
        
        // ========================================
        // VALIDATION 5 : DATE (vide ?)
        // ========================================
        if (datePickerRendezVous.getValue() == null) {
            return "Le champ Date rendez-vous est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 6 : HEURE (vide ?)
        // ========================================
        if (txtHeureRendezVous.getText() == null || txtHeureRendezVous.getText().trim().isEmpty()) {
            return "Le champ Heure rendez-vous est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 7 : PRIORITÉ (vide ?)
        // ========================================
        if (comboPriorite.getValue() == null || comboPriorite.getValue().trim().isEmpty()) {
            return "Le champ Priorite est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 8 : MODE CONSULTATION (vide ?)
        // ========================================
        if (comboModeConsultation.getValue() == null || comboModeConsultation.getValue().trim().isEmpty()) {
            return "Le champ Mode consultation est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 9 : NOTES (vide ?)
        // ========================================
        if (txtNotesRendezVous.getText() == null || txtNotesRendezVous.getText().trim().isEmpty()) {
            return "Le champ Notes est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 10 : PAYS (vide ?)
        // ========================================
        if (txtPays.getText() == null || txtPays.getText().trim().isEmpty()) {
            return "Le champ Pays est obligatoire.";
        }
        
        // ========================================
        // VALIDATION 11 : TÉLÉPHONE (vide ?)
        // ========================================
        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            return "Le champ Telephone est obligatoire.";
        }
        
        // ========================================
        // ✅ TOUT EST OK !
        // ========================================
        // Si on arrive ici, tous les champs sont valides
        return null;  // null = pas d'erreur
    }

    private void remplirFormulaire(RendezVous rendezVous) {
        // ❌ SUPPRIMÉ : txtUtilisateurId.setText(String.valueOf(rendezVous.getUtilisateurId()));
        txtNom.setText(rendezVous.getNom());
        txtPrenom.setText(rendezVous.getPrenom());
        datePickerRendezVous.setValue(rendezVous.getDateRendezVous());
        txtHeureRendezVous.setText(rendezVous.getHeureRendezVous() == null ? "" : rendezVous.getHeureRendezVous().toString());
        comboPriorite.setValue(rendezVous.getPriorite());
        comboModeConsultation.setValue(rendezVous.getModeConsultation());
        txtNotesRendezVous.setText(rendezVous.getNotesRendezVous());
        txtPays.setText(rendezVous.getPays());
        txtTelephone.setText(rendezVous.getTelephone());
        statutEdition = rendezVous.getStatutRendezVous() == null ? StatutRendezVous.EN_ATTENTE : rendezVous.getStatutRendezVous();
        afficherInfo("Mode modification actif.");
    }

    /**
     * 🔢 FONCTION PARSE HEURE
     * 
     * Convertit le texte de l'heure en objet LocalTime
     * Ajoute les secondes si nécessaire (14:30 → 14:30:00)
     */
    private LocalTime parseHeure(String valeur) {
        String texte = valeur == null ? "" : valeur.trim();
        if (texte.length() == 5) {
            texte = texte + ":00";
        }
        return LocalTime.parse(texte);
    }

    // ❌ SUPPRIMÉ : parseUtilisateurId() - Plus besoin, l'ID est auto-généré !

    /**
     * 📩 AFFICHE UN MESSAGE
     * 
     * Affiche un message dans le label lblMessage
     */
    private void afficherMessage(String message) {
        if (lblMessage != null) {
            lblMessage.setText(message);
        }
    }

    /**
     * ℹ️ AFFICHE UN MESSAGE D'INFO (noir)
     * 
     * Affiche un message normal (pas d'erreur)
     */
    private void afficherInfo(String message) {
        if (lblMessage != null) {
            lblMessage.setStyle("");  // Style normal (noir)
        }
        afficherMessage(message);
    }

    /**
     * ❌ AFFICHE UN MESSAGE D'ERREUR (rouge)
     * 
     * Affiche un message d'erreur en ROUGE
     */
    private void afficherErreur(String message) {
        if (lblMessage != null) {
            lblMessage.setStyle("-fx-text-fill: #d32f2f;");  // Style rouge
        }
        afficherMessage(message);
    }
}
