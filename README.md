# VitaPlus Medical

Application Java Desktop en **JavaFX / FXML / MVC** connectée à la base MySQL `vitaplus` sous XAMPP.

## Lancer l’application
Depuis la racine du projet :

```powershell
mvn javafx:run
```

Au démarrage, l’application affiche d’abord un écran de choix de rôle :
- Admin
- Patient
- Médecin

Ensuite, le bon dashboard s’ouvre selon le rôle choisi.

## Connexion base de données
Configuration par défaut dans `src/main/resources/db.properties` :

- host : `localhost`
- port : `3306`
- database : `vitaplus`
- user : `root`
- password : vide

## Structure utile
- `src/main/java/tn/esprit/suivie_nawres/VitaPlusApp.java`
- `src/main/java/tn/esprit/suivie_nawres/utils/DatabaseConnection.java`
- `src/main/java/tn/esprit/suivie_nawres/models/`
- `src/main/java/tn/esprit/suivie_nawres/services/`
- `src/main/java/tn/esprit/suivie_nawres/controllers/`
- `src/main/resources/views/`
- `src/main/resources/css/app.css`

## Dashboards
- Admin
- Patient
- Médecin

## CRUD
- `rendez_vous`
- `consultation`

