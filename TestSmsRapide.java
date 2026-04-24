import tn.esprit.suivie_nawres.services.VonageSmsService;

public class TestSmsRapide {
    public static void main(String[] args) {
        System.out.println("=== TEST SMS RAPIDE ===\n");
        
        // Test 1 : Vérifier le solde
        System.out.println("1. Vérification du solde...");
        VonageSmsService.verifierSolde();
        System.out.println();
        
        // Test 2 : Envoyer un SMS de test
        System.out.println("2. Envoi d'un SMS de test...");
        String numeroTest = "+216 26018082";  // Votre numéro
        
        boolean resultat = VonageSmsService.envoyerSmsTest(numeroTest);
        
        System.out.println();
        if (resultat) {
            System.out.println("✅ SMS envoyé avec succès !");
            System.out.println("Vérifiez votre téléphone dans quelques secondes...");
        } else {
            System.out.println("❌ Échec de l'envoi du SMS");
            System.out.println("Vérifiez les messages d'erreur ci-dessus");
        }
    }
}
