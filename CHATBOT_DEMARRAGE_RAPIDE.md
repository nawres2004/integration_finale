# 🚀 Démarrage Rapide - Chatbot Médical

## ✅ Tout est prêt !

Votre chatbot médical est **100% configuré et prêt à l'emploi** ! 🎉

---

## 🎯 Comment tester le chatbot

### Étape 1 : Lancer l'application
```bash
mvn clean javafx:run
```

### Étape 2 : Accéder au Dashboard Patient
1. Choisir le rôle **"Patient"**
2. Vous verrez un nouveau bouton **"🤖 Assistant Médical"** en haut à droite

### Étape 3 : Ouvrir le chatbot
Cliquez sur le bouton **"🤖 Assistant Médical"**

### Étape 4 : Poser une question
Exemples de questions à tester :

```
"Quels sont les symptômes de la grippe ?"
"Comment prévenir les maladies cardiovasculaires ?"
"J'ai mal à la tête, que faire ?"
"Quand dois-je consulter un médecin ?"
```

---

## 📁 Fichiers créés

✅ **Service IA**
- `src/main/java/tn/esprit/suivie_nawres/services/ChatbotService.java`

✅ **Contrôleur**
- `src/main/java/tn/esprit/suivie_nawres/controllers/ChatbotController.java`

✅ **Interface graphique**
- `src/main/resources/views/Chatbot.fxml`

✅ **Utilitaire**
- `src/main/java/tn/esprit/suivie_nawres/utils/ChatbotLauncher.java`

✅ **Configuration**
- `src/main/resources/gemini.properties` (avec votre clé API)

✅ **Styles CSS**
- Ajoutés dans `src/main/resources/css/app.css`

✅ **Intégration**
- Bouton ajouté dans `DashboardPatient.fxml`
- Méthode ajoutée dans `DashboardPatientController.java`

---

## 🎨 Aperçu de l'interface

```
┌─────────────────────────────────────────────────────┐
│ 🤖 Assistant Médical VitaPlus    ✅ Prêt  🗑️ ✖️   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  👋 Bonjour ! Je suis votre assistant médical...   │
│  🤖 10:30                                           │
│                                                     │
│                    Quels sont les symptômes de     │
│                    la grippe ?                      │
│                                           10:31     │
│                                                     │
│  Les symptômes de la grippe incluent...            │
│  🤖 10:31                                           │
│                                                     │
├─────────────────────────────────────────────────────┤
│ 💬 Posez votre question médicale...    📤 Envoyer  │
│ ⚠️ Les conseils ne remplacent pas une consultation │
└─────────────────────────────────────────────────────┘
```

---

## 🔧 Ajouter le chatbot ailleurs

### Dans DashboardMedecin

**1. Modifier `DashboardMedecinController.java` :**
```java
import tn.esprit.suivie_nawres.utils.ChatbotLauncher;

@FXML
private void ouvrirChatbot() {
    ChatbotLauncher.ouvrir();
}
```

**2. Modifier `DashboardMedecin.fxml` :**
```xml
<Button text="🤖 Assistant Médical" 
        onAction="#ouvrirChatbot" 
        styleClass="primary-button"/>
```

---

## 💡 Fonctionnalités

✅ **Interface moderne** avec bulles de chat
✅ **Réponses en temps réel** de l'IA Gemini
✅ **Contexte médical** spécialisé
✅ **Historique de conversation** dans la session
✅ **Bouton "Effacer"** pour recommencer
✅ **Messages d'erreur** clairs
✅ **Responsive** et redimensionnable

---

## 🐛 En cas de problème

### Le chatbot ne s'ouvre pas
```bash
# Vérifier que tous les fichiers existent
ls src/main/java/tn/esprit/suivie_nawres/services/ChatbotService.java
ls src/main/java/tn/esprit/suivie_nawres/controllers/ChatbotController.java
ls src/main/resources/views/Chatbot.fxml
```

### Erreur "Configuration requise"
```bash
# Vérifier la clé API
cat src/main/resources/gemini.properties
```

### Erreur de compilation
```bash
# Recompiler le projet
mvn clean compile
```

---

## 📊 Quota API Gemini (Gratuit)

- ✅ **60 requêtes par minute**
- ✅ **1500 requêtes par jour**
- ✅ **GRATUIT** pour toujours

---

## 🎓 Améliorations futures

1. **Sauvegarder l'historique** dans la base de données
2. **Suggestions de questions** fréquentes
3. **Intégration avec les rendez-vous** ("Prendre RDV")
4. **Analyse de symptômes** guidée
5. **Support multilingue** (Arabe, Anglais)

---

## ✨ C'est tout !

Votre chatbot médical est **opérationnel** ! 🎉

Lancez l'application et testez-le dès maintenant !

```bash
mvn clean javafx:run
```

---

**Créé avec ❤️ pour VitaPlus**
*Propulsé par Google Gemini AI*
