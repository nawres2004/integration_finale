package tn.esprit.suivie_nawres.services;

import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 🧪 CLASSE DE TEST POUR LE SERVICE SMS VONAGE
 * 
 * Cette classe permet de tester l'envoi de SMS sans lancer toute l'application
 * 
 * IMPORTANT : Remplacez le numéro de téléphone par votre propre numéro tunisien !
 */
public class VonageSmsServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🧪 TEST DU SERVICE SMS VONAGE");
        System.out.println("=================================================\n");
        
        // ========================================
        // TEST 1 : VÉRIFIER LE SOLDE
        // ========================================
        System.out.println("📊 Test 1 : Vérification du solde Vonage");
        System.out.println("-------------------------------------------------");
        VonageSmsService.verifierSolde();
        System.out.println();
        
        // ========================================
        // TEST 2 : ENVOYER UN SMS DE TEST
        // ========================================
        System.out.println("📱 Test 2 : Envoi d'un SMS de test");
        System.out.println("-------------------------------------------------");
        
        // ⚠️ IMPORTANT : Remplacez par votre numéro de téléphone tunisien !
        String numeroTest = "+216 98 765 432";  // ⬅️ CHANGEZ CE NUMÉRO !
        
        System.out.println("Envoi d'un SMS de test à : " + numeroTest);
        boolean resultatTest = VonageSmsService.envoyerSmsTest(numeroTest);
        
        if (resultatTest) {
            System.out.println("✅ SMS de test envoyé avec succès !");
        } else {
            System.out.println("❌ Échec de l'envoi du SMS de test");
            System.out.println("⚠️ Vérifiez votre configuration dans vonage.properties");
        }
        System.out.println();
        
        // ========================================
        // TEST 3 : SIMULER UN RENDEZ-VOUS
        // ========================================
        System.out.println("📅 Test 3 : Simulation d'un rendez-vous");
        System.out.println("-------------------------------------------------");
        
        // Crée un rendez-vous fictif pour tester
        RendezVous rdvTest = new RendezVous();
        rdvTest.setUtilisateurId(999);
        rdvTest.setNom("Dupont");
        rdvTest.setPrenom("Ahmed");
        rdvTest.setDateRendezVous(LocalDate.now().plusDays(3));
        rdvTest.setHeureRendezVous(LocalTime.of(14, 30));
        rdvTest.setPriorite("NORMALE");
        rdvTest.setModeConsultation("PRESENTIEL");
        rdvTest.setStatutRendezVous(StatutRendezVous.ACCEPTE);
        rdvTest.setNotesRendezVous("Consultation de contrôle");
        rdvTest.setPays("Tunisie");
        rdvTest.setTelephone(numeroTest);  // Utilise le même numéro
        
        System.out.println("Envoi d'un SMS de confirmation de rendez-vous...");
        boolean resultatConfirmation = VonageSmsService.envoyerSmsConfirmationRendezVous(rdvTest);
        
        if (resultatConfirmation) {
            System.out.println("✅ SMS de confirmation envoyé avec succès !");
        } else {
            System.out.println("❌ Échec de l'envoi du SMS de confirmation");
        }
        System.out.println();
        
        // ========================================
        // TEST 4 : SIMULER UNE ACCEPTATION
        // ========================================
        System.out.println("✅ Test 4 : Simulation d'une acceptation de RDV");
        System.out.println("-------------------------------------------------");
        
        System.out.println("Envoi d'un SMS d'acceptation de rendez-vous...");
        boolean resultatAcceptation = VonageSmsService.envoyerSmsAcceptationRendezVous(rdvTest);
        
        if (resultatAcceptation) {
            System.out.println("✅ SMS d'acceptation envoyé avec succès !");
        } else {
            System.out.println("❌ Échec de l'envoi du SMS d'acceptation");
        }
        System.out.println();
        
        // ========================================
        // RÉSUMÉ FINAL
        // ========================================
        System.out.println("=================================================");
        System.out.println("📊 RÉSUMÉ DES TESTS");
        System.out.println("=================================================");
        System.out.println("Test 1 - Vérification du solde : ✅");
        System.out.println("Test 2 - SMS de test : " + (resultatTest ? "✅" : "❌"));
        System.out.println("Test 3 - SMS de confirmation : " + (resultatConfirmation ? "✅" : "❌"));
        System.out.println("Test 4 - SMS d'acceptation : " + (resultatAcceptation ? "✅" : "❌"));
        System.out.println("=================================================");
        
        if (resultatTest && resultatConfirmation && resultatAcceptation) {
            System.out.println("\n🎉 TOUS LES TESTS SONT RÉUSSIS !");
            System.out.println("Votre configuration SMS Vonage fonctionne parfaitement !");
        } else {
            System.out.println("\n⚠️ CERTAINS TESTS ONT ÉCHOUÉ");
            System.out.println("Vérifiez votre configuration dans vonage.properties");
            System.out.println("Assurez-vous que :");
            System.out.println("  - vonage.api.key = e4d746dc");
            System.out.println("  - vonage.api.secret = mWbwuncUR2ZZ9sx7");
            System.out.println("  - vonage.enabled = true");
            System.out.println("  - Vous avez du crédit sur votre compte Vonage");
        }
    }
}
