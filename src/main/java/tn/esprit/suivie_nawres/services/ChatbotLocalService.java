package tn.esprit.suivie_nawres.services;

import java.util.*;

/**
 * 🤖 CHATBOT MÉDICAL LOCAL (100% GRATUIT, SANS API)
 * ==================================================
 * Solution locale avec intelligence artificielle basée sur des règles
 * 
 * ✅ Avantages :
 * - 100% gratuit
 * - Aucune dépendance externe
 * - Fonctionne hors ligne
 * - Réponses instantanées
 * - Aucune configuration nécessaire
 * 
 * 🎯 Fonctionnalités :
 * - Reconnaissance de mots-clés
 * - Base de connaissances médicales
 * - Réponses contextuelles
 * - Suggestions intelligentes
 */
public class ChatbotLocalService {

    private final Map<String, String> baseMedicale;
    private final Map<String, List<String>> motsClefs;

    public ChatbotLocalService() {
        this.baseMedicale = new HashMap<>();
        this.motsClefs = new HashMap<>();
        initialiserBaseConnaissances();
    }

    /**
     * 💬 ENVOIE UN MESSAGE ET REÇOIT UNE RÉPONSE
     */
    public String envoyerMessage(String messageUtilisateur) {
        System.out.println("🤖 Traitement du message : " + messageUtilisateur);
        
        String messageLower = messageUtilisateur.toLowerCase().trim();
        
        // Rechercher la meilleure réponse
        String reponse = trouverReponse(messageLower);
        
        System.out.println("✅ Réponse générée");
        return reponse;
    }

    /**
     * 🔍 TROUVE LA MEILLEURE RÉPONSE
     */
    private String trouverReponse(String message) {
        int meilleurScore = 0;
        String meilleureReponse = null;
        
        // Parcourir toutes les catégories
        for (Map.Entry<String, List<String>> entry : motsClefs.entrySet()) {
            String categorie = entry.getKey();
            List<String> mots = entry.getValue();
            
            int score = 0;
            for (String mot : mots) {
                if (message.contains(mot)) {
                    score++;
                }
            }
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureReponse = baseMedicale.get(categorie);
            }
        }
        
        // Si aucune correspondance, réponse par défaut
        if (meilleureReponse == null) {
            return reponseParDefaut(message);
        }
        
        return meilleureReponse;
    }

    /**
     * 📚 INITIALISE LA BASE DE CONNAISSANCES MÉDICALES
     */
    private void initialiserBaseConnaissances() {
        
        // === GRIPPE ===
        motsClefs.put("grippe", Arrays.asList("grippe", "fièvre", "frissons", "courbatures", "fatigue", "toux"));
        baseMedicale.put("grippe", 
            "🦠 **Grippe - Informations**\n\n" +
            "**Symptômes principaux :**\n" +
            "• Fièvre élevée (38-40°C)\n" +
            "• Frissons et sueurs\n" +
            "• Courbatures musculaires\n" +
            "• Fatigue intense\n" +
            "• Toux sèche\n" +
            "• Maux de tête\n\n" +
            "**Que faire ?**\n" +
            "• Repos au lit\n" +
            "• Boire beaucoup d'eau\n" +
            "• Paracétamol pour la fièvre\n" +
            "• Consulter si fièvre > 3 jours\n\n" +
            "⚠️ **Consultez un médecin si :**\n" +
            "• Difficultés respiratoires\n" +
            "• Fièvre persistante\n" +
            "• Douleur thoracique\n" +
            "• Confusion"
        );
        
        // === MAL DE TÊTE ===
        motsClefs.put("mal_tete", Arrays.asList("mal de tête", "maux de tête", "migraine", "céphalée", "tête"));
        baseMedicale.put("mal_tete",
            "🤕 **Maux de tête - Conseils**\n\n" +
            "**Causes fréquentes :**\n" +
            "• Stress et tension\n" +
            "• Déshydratation\n" +
            "• Manque de sommeil\n" +
            "• Fatigue oculaire\n" +
            "• Migraine\n\n" +
            "**Solutions :**\n" +
            "• Boire de l'eau\n" +
            "• Se reposer dans le calme\n" +
            "• Paracétamol ou ibuprofène\n" +
            "• Massage des tempes\n" +
            "• Compresse froide\n\n" +
            "⚠️ **Consultez si :**\n" +
            "• Douleur intense et soudaine\n" +
            "• Accompagné de fièvre\n" +
            "• Troubles de la vision\n" +
            "• Dure plus de 3 jours"
        );
        
        // === RHUME ===
        motsClefs.put("rhume", Arrays.asList("rhume", "nez bouché", "éternuements", "nez qui coule"));
        baseMedicale.put("rhume",
            "🤧 **Rhume - Traitement**\n\n" +
            "**Symptômes :**\n" +
            "• Nez qui coule ou bouché\n" +
            "• Éternuements\n" +
            "• Mal de gorge léger\n" +
            "• Toux légère\n\n" +
            "**Traitement :**\n" +
            "• Repos\n" +
            "• Hydratation (eau, tisanes)\n" +
            "• Lavage nasal (sérum physiologique)\n" +
            "• Miel pour la gorge\n" +
            "• Paracétamol si besoin\n\n" +
            "**Durée :** 7-10 jours\n\n" +
            "⚠️ Le rhume est viral, les antibiotiques ne servent à rien !"
        );
        
        // === DIABÈTE ===
        motsClefs.put("diabete", Arrays.asList("diabète", "sucre", "glycémie", "insuline"));
        baseMedicale.put("diabete",
            "🩸 **Diabète - Informations**\n\n" +
            "**Types :**\n" +
            "• Type 1 : Manque d'insuline\n" +
            "• Type 2 : Résistance à l'insuline\n\n" +
            "**Symptômes :**\n" +
            "• Soif excessive\n" +
            "• Urines fréquentes\n" +
            "• Fatigue\n" +
            "• Vision floue\n" +
            "• Perte de poids\n\n" +
            "**Prévention :**\n" +
            "• Alimentation équilibrée\n" +
            "• Activité physique régulière\n" +
            "• Maintenir un poids santé\n" +
            "• Contrôles réguliers\n\n" +
            "⚠️ **Important :** Consultez un médecin pour un diagnostic et un suivi personnalisé."
        );
        
        // === HYPERTENSION ===
        motsClefs.put("hypertension", Arrays.asList("tension", "hypertension", "pression", "artérielle"));
        baseMedicale.put("hypertension",
            "💓 **Hypertension - Prévention**\n\n" +
            "**Qu'est-ce que c'est ?**\n" +
            "Pression artérielle élevée (> 140/90 mmHg)\n\n" +
            "**Facteurs de risque :**\n" +
            "• Alimentation trop salée\n" +
            "• Surpoids\n" +
            "• Sédentarité\n" +
            "• Stress\n" +
            "• Tabac et alcool\n\n" +
            "**Prévention :**\n" +
            "• Réduire le sel\n" +
            "• Manger fruits et légumes\n" +
            "• Exercice régulier (30 min/jour)\n" +
            "• Gérer le stress\n" +
            "• Arrêter le tabac\n\n" +
            "⚠️ **Suivi médical régulier indispensable !**"
        );
        
        // === RENDEZ-VOUS ===
        motsClefs.put("rendez_vous", Arrays.asList("rendez-vous", "rdv", "consultation", "prendre rendez-vous"));
        baseMedicale.put("rendez_vous",
            "📅 **Prendre rendez-vous**\n\n" +
            "Pour prendre rendez-vous avec un médecin :\n\n" +
            "1. Cliquez sur le bouton **\"Réserver un Rendez-Vous\"** dans le dashboard\n" +
            "2. Remplissez le formulaire avec vos informations\n" +
            "3. Choisissez la date et l'heure souhaitées\n" +
            "4. Validez votre demande\n\n" +
            "Vous recevrez une notification SMS quand le médecin acceptera votre demande !\n\n" +
            "💡 **Astuce :** Vous pouvez consulter l'état de vos rendez-vous dans \"Mes Rendez-Vous\"."
        );
        
        // === URGENCE ===
        motsClefs.put("urgence", Arrays.asList("urgence", "grave", "douleur intense", "saignement", "accident"));
        baseMedicale.put("urgence",
            "🚨 **URGENCE MÉDICALE**\n\n" +
            "⚠️ **Si vous êtes en situation d'urgence :**\n\n" +
            "🚑 **Appelez immédiatement :**\n" +
            "• SAMU : 190 (Tunisie)\n" +
            "• Urgences : 197\n" +
            "• Police : 197\n\n" +
            "**Situations d'urgence :**\n" +
            "• Douleur thoracique intense\n" +
            "• Difficultés respiratoires\n" +
            "• Saignement important\n" +
            "• Perte de conscience\n" +
            "• Accident grave\n" +
            "• Brûlure étendue\n\n" +
            "⚠️ **N'attendez pas ! Appelez les secours immédiatement !**"
        );
        
        // === VACCINATION ===
        motsClefs.put("vaccination", Arrays.asList("vaccin", "vaccination", "immunisation"));
        baseMedicale.put("vaccination",
            "💉 **Vaccination - Importance**\n\n" +
            "**Pourquoi se faire vacciner ?**\n" +
            "• Protection contre les maladies graves\n" +
            "• Protection collective\n" +
            "• Prévention des épidémies\n\n" +
            "**Vaccins recommandés :**\n" +
            "• Grippe (annuel)\n" +
            "• COVID-19\n" +
            "• Tétanos (rappel tous les 10 ans)\n" +
            "• Hépatite B\n\n" +
            "**Où se faire vacciner ?**\n" +
            "• Centres de santé\n" +
            "• Cabinets médicaux\n" +
            "• Hôpitaux\n\n" +
            "💡 Consultez votre médecin pour un calendrier vaccinal personnalisé."
        );
    }

    /**
     * 💬 RÉPONSE PAR DÉFAUT
     */
    private String reponseParDefaut(String message) {
        if (message.contains("bonjour") || message.contains("salut") || message.contains("hello")) {
            return "👋 Bonjour ! Je suis votre assistant médical VitaPlus.\n\n" +
                   "Je peux vous aider avec :\n" +
                   "• Informations sur les symptômes\n" +
                   "• Conseils de prévention\n" +
                   "• Prendre rendez-vous\n" +
                   "• Questions médicales générales\n\n" +
                   "Comment puis-je vous aider aujourd'hui ?";
        }
        
        if (message.contains("merci")) {
            return "😊 De rien ! N'hésitez pas si vous avez d'autres questions.\n\n" +
                   "⚠️ Rappel : Mes conseils ne remplacent pas une consultation médicale.";
        }
        
        if (message.contains("?")) {
            return "🤔 Je n'ai pas d'information spécifique sur ce sujet.\n\n" +
                   "**Je peux vous aider avec :**\n" +
                   "• Grippe et rhume\n" +
                   "• Maux de tête\n" +
                   "• Diabète\n" +
                   "• Hypertension\n" +
                   "• Vaccination\n" +
                   "• Prendre rendez-vous\n\n" +
                   "💡 Posez-moi une question sur ces sujets !\n\n" +
                   "⚠️ Pour un diagnostic précis, consultez un médecin.";
        }
        
        return "Je suis là pour vous aider ! 😊\n\n" +
               "**Exemples de questions :**\n" +
               "• \"Quels sont les symptômes de la grippe ?\"\n" +
               "• \"Comment prévenir le diabète ?\"\n" +
               "• \"J'ai mal à la tête, que faire ?\"\n" +
               "• \"Comment prendre rendez-vous ?\"\n\n" +
               "⚠️ Mes conseils ne remplacent pas une consultation médicale réelle.";
    }

    public boolean estConfigure() {
        return true; // Toujours configuré (pas besoin d'API)
    }
}
