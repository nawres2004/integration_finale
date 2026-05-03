# 🤖 Guide du Chatbot Médical VitaPlus

## 📋 Vue d'ensemble

Le chatbot médical VitaPlus est un assistant virtuel intelligent qui utilise l'IA Google Gemini pour répondre aux questions médicales des patients et des médecins.

---

## ✅ Configuration (DÉJÀ FAIT ✓)

Votre clé API Gemini est déjà configurée dans `src/main/resources/gemini.properties`

```properties
gemini.api.key=AIzaSyCJkomZoXvI8je4jU4Iz47yKPvA9GbbfRw
```

---

## 🚀 Comment ouvrir le chatbot

### Méthode 1 : Depuis n'importe quel contrôleur

Ajoutez simplement cette méthode dans votre contrôleur :

```java
@FXML
private void ouvrirChatbot() {
    ChatbotLauncher.ouvrir();
}
```

Et dans votre fichier FXML, ajoutez un bouton :

```xml
<Button text="🤖 Assistant Médical" onAction="#ouvrirChatbot" styleClass="btn-primary"/>
```

### Méthode 2 : Depuis le code Java

```java
import tn.esprit.suivie_nawres.utils.ChatbotLauncher;

// N'importe où dans votre code
ChatbotLauncher.ouvrir();
```

---

## 📍 Où ajouter le bouton du chatbot ?

### Option 1 : Dashboard Patient (RECOMMANDÉ)
Ajoutez un bouton dans `DashboardPatient.fxml` pour que les patients puissent poser des questions.

### Option 2 : Dashboard Médecin
Ajoutez un bouton dans `DashboardMedecin.fxml` pour que les médecins puissent consulter l'assistant.

### Option 3 : Menu principal
Ajoutez un bouton dans tous les dashboards pour un accès universel.

---

## 💡 Exemple d'intégration dans DashboardPatient

### Étape 1 : Modifier `DashboardPatientController.java`

Ajoutez cette méthode :

```java
@FXML
private void ouvrirChatbot() {
    ChatbotLauncher.ouvrir();
}
```

### Étape 2 : Modifier `DashboardPatient.fxml`

Ajoutez ce bouton dans votre interface :

```xml
<Button text="🤖 Assistant Médical" 
        onAction="#ouvrirChatbot" 
        styleClass="primary-button"
        prefWidth="200"/>
```

---

## 🎯 Fonctionnalités du chatbot

### ✅ Ce que le chatbot peut faire :

- ✅ Répondre aux questions médicales générales
- ✅ Expliquer des symptômes
- ✅ Donner des conseils de prévention
- ✅ Fournir des informations sur les maladies
- ✅ Suggérer quand consulter un médecin
- ✅ Répondre en français de manière claire

### ⚠️ Ce que le chatbot NE peut PAS faire :

- ❌ Remplacer une consultation médicale réelle
- ❌ Prescrire des médicaments
- ❌ Diagnostiquer des maladies graves
- ❌ Accéder aux données de votre base de données

---

## 🧪 Tester le chatbot

### Questions de test :

1. **Question simple :**
   - "Quels sont les symptômes de la grippe ?"

2. **Question de prévention :**
   - "Comment prévenir les maladies cardiovasculaires ?"

3. **Question sur un symptôme :**
   - "J'ai mal à la tête depuis 2 jours, que faire ?"

4. **Question sur un traitement :**
   - "Comment traiter une entorse ?"

---

## 🔧 Architecture technique

### Fichiers créés :

```
src/main/java/tn/esprit/suivie_nawres/
├── services/
│   └── ChatbotService.java          # Service de communication avec Gemini AI
├── controllers/
│   └── ChatbotController.java       # Contrôleur de l'interface du chat
└── utils/
    └── ChatbotLauncher.java         # Utilitaire pour ouvrir le chatbot

src/main/resources/
├── views/
│   └── Chatbot.fxml                 # Interface graphique du chat
├── css/
│   └── app.css                      # Styles du chatbot (ajoutés)
└── gemini.properties                # Configuration de l'API
```

### Flux de données :

```
Utilisateur → ChatbotController → ChatbotService → API Gemini
                    ↓                                    ↓
              Interface JavaFX ← Réponse formatée ← Réponse JSON
```

---

## 🎨 Personnalisation

### Modifier le contexte médical

Dans `ChatbotService.java`, méthode `creerPromptMedical()` :

```java
private String creerPromptMedical(String messageUtilisateur) {
    return "Tu es un assistant médical virtuel pour l'application VitaPlus. " +
           "VOTRE CONTEXTE PERSONNALISÉ ICI..." +
           "Question du patient : " + messageUtilisateur;
}
```

### Modifier l'apparence

Dans `src/main/resources/css/app.css`, section "STYLES DU CHATBOT MÉDICAL" :

- `.chat-bubble-user` : Bulles de l'utilisateur
- `.chat-bubble-bot` : Bulles du chatbot
- `.chat-header` : En-tête du chat

---

## 🐛 Dépannage

### Problème : "Configuration requise"

**Solution :** Vérifiez que la clé API est bien dans `gemini.properties`

### Problème : "Erreur de communication"

**Solutions possibles :**
1. Vérifiez votre connexion internet
2. Vérifiez que la clé API est valide
3. Vérifiez les logs dans la console

### Problème : Le chatbot ne s'ouvre pas

**Solution :** Vérifiez que tous les fichiers sont bien créés :
- `Chatbot.fxml`
- `ChatbotController.java`
- `ChatbotService.java`
- `ChatbotLauncher.java`

---

## 📊 Limites de l'API gratuite

- **Quota :** 60 requêtes par minute
- **Tokens :** 1000 tokens par réponse (environ 750 mots)
- **Coût :** GRATUIT pour un usage normal

---

## 🚀 Prochaines étapes

### Améliorations possibles :

1. **Historique des conversations**
   - Sauvegarder les conversations dans la base de données

2. **Suggestions automatiques**
   - Proposer des questions fréquentes

3. **Intégration avec les rendez-vous**
   - "Prendre rendez-vous avec un médecin"

4. **Analyse de symptômes**
   - Formulaire guidé pour décrire les symptômes

5. **Multilingue**
   - Support de l'arabe et de l'anglais

---

## 📞 Support

Pour toute question sur le chatbot, consultez :
- Documentation Gemini : https://ai.google.dev/docs
- Code source : `src/main/java/tn/esprit/suivie_nawres/services/ChatbotService.java`

---

**Créé pour VitaPlus** 🏥
*Assistant médical intelligent propulsé par Google Gemini AI*
