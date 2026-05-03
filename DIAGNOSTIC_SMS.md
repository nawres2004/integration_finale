# 🔍 Diagnostic - Pourquoi je ne reçois pas de SMS ?

## ✅ Le code est correct !

J'ai vérifié le code et **l'envoi de SMS est bien implémenté** dans les deux cas :

1. ✅ **Quand le médecin accepte un RDV** → SMS envoyé (ligne 115-125 de `TraiterDemandesRendezVousController.java`)
2. ✅ **Quand le médecin crée un RDV** → SMS envoyé (ligne 234-242 de `AjouterRendezVousController.java`)

---

## 🔍 Vérifications à faire

### 1. Vérifier les logs dans la console

Quand vous acceptez ou créez un rendez-vous, regardez la **console** (terminal où vous avez lancé `mvn javafx:run`).

Vous devriez voir :

```
✅ Service SMS Vonage initialisé avec succès
📤 Expéditeur : VitaPlus
📱 Envoi de SMS à +216XXXXXXXX
✅ SMS envoyé avec succès !
```

**Si vous voyez :**
- `ℹ️ Service SMS désactivé` → Mettez `vonage.enabled=true` dans `vonage.properties`
- `❌ Erreur lors de l'envoi du SMS` → Problème avec Vonage (crédit épuisé ou clés invalides)
- Rien du tout → Le SMS n'est pas envoyé (bug dans le code)

---

### 2. Vérifier le crédit Vonage

1. Allez sur : **https://dashboard.nexmo.com/**
2. Connectez-vous avec votre compte
3. Regardez votre **solde** en haut à droite

**Si le solde est à 0€** :
- Ajoutez du crédit (minimum 10€)
- OU créez un nouveau compte pour avoir 2€ gratuits

---

### 3. Vérifier le format du numéro de téléphone

Le numéro doit être au **format international** :

✅ **Correct** :
- `+216 26018082`
- `+216 98765432`

❌ **Incorrect** :
- `26018082` (manque le +216)
- `0026018082` (mauvais format)
- `216 26018082` (manque le +)

---

### 4. Vérifier que vonage.enabled=true

Ouvrez `src/main/resources/vonage.properties` et vérifiez :

```properties
vonage.enabled=true
```

Si c'est `false`, changez-le en `true`.

---

### 5. Tester manuellement l'envoi de SMS

Créez un fichier de test pour vérifier que Vonage fonctionne :

```java
// TestSmsManuel.java
import tn.esprit.suivie_nawres.services.VonageSmsService;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import java.time.LocalDate;
import java.time.LocalTime;

public class TestSmsManuel {
    public static void main(String[] args) {
        RendezVous rdv = new RendezVous();
        rdv.setNom("Test");
        rdv.setPrenom("Patient");
        rdv.setTelephone("+216 VOTRE_NUMERO");  // ⚠️ Mettez votre vrai numéro
        rdv.setDateRendezVous(LocalDate.now());
        rdv.setHeureRendezVous(LocalTime.of(14, 30));
        rdv.setStatutRendezVous(StatutRendezVous.ACCEPTE);
        
        boolean resultat = VonageSmsService.envoyerSmsAcceptationRendezVous(rdv);
        
        if (resultat) {
            System.out.println("✅ SMS envoyé avec succès !");
        } else {
            System.out.println("❌ Échec de l'envoi du SMS");
        }
    }
}
```

Compilez et exécutez :
```bash
javac -cp "target/classes:lib/*" TestSmsManuel.java
java -cp "target/classes:lib/*:." TestSmsManuel
```

---

## 🐛 Solutions selon le problème

### Problème 1 : "Service SMS désactivé"
**Solution** : Mettez `vonage.enabled=true` dans `vonage.properties`

### Problème 2 : "Configuration Vonage incomplète"
**Solution** : Vérifiez que `vonage.api.key` et `vonage.api.secret` sont corrects

### Problème 3 : "Erreur 401 - Authentication failed"
**Solution** : Vos clés API Vonage sont invalides. Créez de nouvelles clés sur https://dashboard.nexmo.com/

### Problème 4 : "Insufficient credit"
**Solution** : Ajoutez du crédit sur votre compte Vonage

### Problème 5 : Le SMS est envoyé mais pas reçu
**Solutions possibles** :
- Le numéro est incorrect
- Le téléphone est éteint
- Problème de réseau
- Le SMS est dans les spams
- Attendez 1-2 minutes (délai de livraison)

---

## 📊 Checklist complète

- [ ] `vonage.enabled=true` dans `vonage.properties`
- [ ] Clés API Vonage valides
- [ ] Crédit Vonage > 0€
- [ ] Numéro au format international (+216...)
- [ ] Logs visibles dans la console
- [ ] Test manuel réussi

---

## 💡 Astuce : Activer les logs détaillés

Pour voir TOUS les logs, ajoutez ceci au début de la méthode `accepterDemande()` :

```java
System.out.println("=== DÉBUT ACCEPTATION RDV ===");
System.out.println("Téléphone : " + rendezVous.getTelephone());
System.out.println("Statut : " + rendezVous.getStatutRendezVous());
```

Et à la fin :

```java
System.out.println("=== FIN ACCEPTATION RDV ===");
```

---

## 🆘 Si rien ne fonctionne

Envoyez-moi :
1. Les logs de la console (copier-coller)
2. Le contenu de `vonage.properties` (sans les clés secrètes)
3. Le format du numéro de téléphone utilisé

Je vous aiderai à résoudre le problème ! 🚀
