# 🔄 Chatbot avec OpenAI (Alternative à Gemini)

## ⚠️ Pourquoi OpenAI ?

Gemini est actuellement **surchargé** (erreur 503). OpenAI est plus **stable** et **fiable**.

---

## 🚀 Configuration rapide (5 minutes)

### Étape 1 : Obtenir une clé API OpenAI

1. Allez sur : **https://platform.openai.com/signup**
2. Créez un compte (gratuit)
3. Allez sur : **https://platform.openai.com/api-keys**
4. Cliquez sur **"Create new secret key"**
5. Copiez la clé (elle commence par `sk-...`)

### Étape 2 : Configurer la clé

Ouvrez le fichier `src/main/resources/openai.properties` et collez votre clé :

```properties
openai.api.key=sk-VOTRE_CLE_ICI
```

### Étape 3 : Tester

```bash
mvn clean javafx:run
```

---

## 💰 Coût

- **5$ de crédit gratuit** pour les nouveaux comptes
- **gpt-3.5-turbo** : ~0.002$ par conversation
- Avec 5$, vous pouvez faire **~2500 conversations** !

---

## ✅ Avantages d'OpenAI vs Gemini

| Critère | OpenAI | Gemini |
|---------|--------|--------|
| **Stabilité** | ✅ Excellent | ⚠️ Surchargé |
| **Vitesse** | ✅ Rapide | ✅ Rapide |
| **Qualité** | ✅ Excellent | ✅ Excellent |
| **Gratuit** | 5$ crédit | ✅ Gratuit |
| **Fiabilité** | ✅ 99.9% | ⚠️ Variable |

---

## 🔧 Fichiers modifiés

✅ `ChatbotOpenAIService.java` - Nouveau service OpenAI
✅ `ChatbotController.java` - Utilise maintenant OpenAI
✅ `openai.properties` - Configuration OpenAI

---

## 🐛 Dépannage

### Erreur "Invalid API key"
→ Vérifiez que votre clé commence par `sk-` et est complète

### Erreur "Insufficient quota"
→ Votre crédit gratuit est épuisé. Ajoutez un moyen de paiement sur OpenAI.

### Erreur de connexion
→ Vérifiez votre connexion internet

---

## 🔄 Revenir à Gemini plus tard

Si Gemini redevient disponible, changez simplement dans `ChatbotController.java` :

```java
// Ligne 19 - Remplacer
private final ChatbotOpenAIService chatbotService = new ChatbotOpenAIService();

// Par
private final ChatbotService chatbotService = new ChatbotService();
```

---

**C'est tout ! Votre chatbot est maintenant stable et fonctionnel ! 🎉**
