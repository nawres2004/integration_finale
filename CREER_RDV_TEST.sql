-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 🧪 SCRIPT SQL POUR CRÉER DES RDV DE TEST
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 
-- Ce script crée des rendez-vous de test pour tester le système de notifications
-- 
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 📊 VÉRIFIER LES RDV EXISTANTS
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Voir tous les RDV d'aujourd'hui
SELECT 
    utilisateur_id,
    nom,
    prenom,
    date_rendez_vous,
    heure_rendez_vous,
    TIME_TO_SEC(TIMEDIFF(heure_rendez_vous, CURTIME())) / 60 AS minutes_restantes,
    mode_consultation,
    statut_rendez_vous
FROM rendezvous
WHERE DATE(date_rendez_vous) = CURDATE()
ORDER BY heure_rendez_vous;

-- Compter les RDV d'aujourd'hui
SELECT COUNT(*) AS nb_rdv_aujourdhui
FROM rendezvous
WHERE DATE(date_rendez_vous) = CURDATE();


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 🔔 CRÉER UN RDV DANS 15 MINUTES (Notification Normale)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO rendezvous (
    utilisateur_id, 
    nom, 
    prenom, 
    date_rendez_vous, 
    heure_rendez_vous,
    priorite, 
    mode_consultation, 
    statut_rendez_vous,
    pays, 
    telephone,
    notes_rendez_vous
) VALUES (
    1,                                      -- ID du médecin
    'Martin',                               -- Nom
    'Sophie',                               -- Prénom
    CURDATE(),                              -- Date = Aujourd'hui
    ADDTIME(CURTIME(), '00:15:00'),        -- Heure = Maintenant + 15 minutes
    'NORMALE',                              -- Priorité
    'PRESENTIEL',                           -- Mode
    'ACCEPTE',                              -- Statut
    'Tunisie',                              -- Pays
    '+216 98 765 432',                      -- Téléphone
    'Test notification 15 minutes'          -- Notes
);

-- Vérifier que le RDV a été créé
SELECT 
    nom, 
    prenom, 
    heure_rendez_vous,
    TIME_TO_SEC(TIMEDIFF(heure_rendez_vous, CURTIME())) / 60 AS minutes_restantes
FROM rendezvous
WHERE nom = 'Martin' AND prenom = 'Sophie'
AND DATE(date_rendez_vous) = CURDATE();


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 🚨 CRÉER UN RDV DANS 1 MINUTE (Notification Urgente)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO rendezvous (
    utilisateur_id, 
    nom, 
    prenom, 
    date_rendez_vous, 
    heure_rendez_vous,
    priorite, 
    mode_consultation, 
    statut_rendez_vous,
    pays, 
    telephone,
    notes_rendez_vous
) VALUES (
    1,                                      -- ID du médecin
    'Dubois',                               -- Nom
    'Jean',                                 -- Prénom
    CURDATE(),                              -- Date = Aujourd'hui
    ADDTIME(CURTIME(), '00:01:00'),        -- Heure = Maintenant + 1 minute
    'URGENTE',                              -- Priorité
    'PRESENTIEL',                           -- Mode
    'ACCEPTE',                              -- Statut
    'Tunisie',                              -- Pays
    '+216 12 345 678',                      -- Téléphone
    'Test notification urgente'             -- Notes
);

-- Vérifier que le RDV a été créé
SELECT 
    nom, 
    prenom, 
    heure_rendez_vous,
    TIME_TO_SEC(TIMEDIFF(heure_rendez_vous, CURTIME())) / 60 AS minutes_restantes
FROM rendezvous
WHERE nom = 'Dubois' AND prenom = 'Jean'
AND DATE(date_rendez_vous) = CURDATE();


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 📅 CRÉER PLUSIEURS RDV POUR LA JOURNÉE
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- RDV dans 30 minutes
INSERT INTO rendezvous (
    utilisateur_id, nom, prenom, 
    date_rendez_vous, heure_rendez_vous,
    priorite, mode_consultation, statut_rendez_vous,
    pays, telephone
) VALUES (
    1, 'Bernard', 'Marie',
    CURDATE(), ADDTIME(CURTIME(), '00:30:00'),
    'NORMALE', 'A_DISTANCE', 'ACCEPTE',
    'France', '+33 6 12 34 56 78'
);

-- RDV dans 1 heure
INSERT INTO rendezvous (
    utilisateur_id, nom, prenom, 
    date_rendez_vous, heure_rendez_vous,
    priorite, mode_consultation, statut_rendez_vous,
    pays, telephone
) VALUES (
    1, 'Durand', 'Pierre',
    CURDATE(), ADDTIME(CURTIME(), '01:00:00'),
    'NORMALE', 'TELECONSULTATION', 'ACCEPTE',
    'Maroc', '+212 6 12 34 56 78'
);

-- RDV dans 2 heures
INSERT INTO rendezvous (
    utilisateur_id, nom, prenom, 
    date_rendez_vous, heure_rendez_vous,
    priorite, mode_consultation, statut_rendez_vous,
    pays, telephone
) VALUES (
    1, 'Lefebvre', 'Claire',
    CURDATE(), ADDTIME(CURTIME(), '02:00:00'),
    'NORMALE', 'PRESENTIEL', 'ACCEPTE',
    'Algérie', '+213 6 12 34 56 78'
);


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 🗑️ SUPPRIMER LES RDV DE TEST
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Supprimer tous les RDV de test d'aujourd'hui
-- DELETE FROM rendezvous 
-- WHERE DATE(date_rendez_vous) = CURDATE()
-- AND notes_rendez_vous LIKE '%Test%';

-- Supprimer un RDV spécifique
-- DELETE FROM rendezvous 
-- WHERE nom = 'Martin' AND prenom = 'Sophie'
-- AND DATE(date_rendez_vous) = CURDATE();


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 📊 STATISTIQUES
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Voir tous les RDV d'aujourd'hui avec le temps restant
SELECT 
    nom,
    prenom,
    DATE_FORMAT(heure_rendez_vous, '%H:%i') AS heure,
    CASE 
        WHEN TIME_TO_SEC(TIMEDIFF(heure_rendez_vous, CURTIME())) / 60 < 0 
        THEN CONCAT('Passé (il y a ', ABS(TIME_TO_SEC(TIMEDIFF(heure_rendez_vous, CURTIME())) / 60), ' min)')
        ELSE CONCAT('Dans ', TIME_TO_SEC(TIMEDIFF(heure_rendez_vous, CURTIME())) / 60, ' min')
    END AS timing,
    mode_consultation,
    statut_rendez_vous
FROM rendezvous
WHERE DATE(date_rendez_vous) = CURDATE()
ORDER BY heure_rendez_vous;

-- Compter les RDV par statut
SELECT 
    statut_rendez_vous,
    COUNT(*) AS nombre
FROM rendezvous
WHERE DATE(date_rendez_vous) = CURDATE()
GROUP BY statut_rendez_vous;


-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 💡 INSTRUCTIONS D'UTILISATION
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/*

📋 COMMENT UTILISER CE SCRIPT :

1️⃣ VÉRIFIER LES RDV EXISTANTS
   - Exécuter la première requête SELECT pour voir les RDV d'aujourd'hui
   - Si vous avez déjà des RDV, notez leur heure

2️⃣ CRÉER UN RDV DE TEST DANS 15 MINUTES
   - Exécuter la requête INSERT avec ADDTIME(CURTIME(), '00:15:00')
   - Attendre 1 minute (le service vérifie toutes les minutes)
   - Une notification bleue devrait apparaître

3️⃣ CRÉER UN RDV DE TEST MAINTENANT
   - Exécuter la requête INSERT avec ADDTIME(CURTIME(), '00:01:00')
   - Attendre 1 minute
   - Une notification rouge urgente devrait apparaître

4️⃣ VÉRIFIER LES LOGS
   - Regarder la console de l'application
   - Vous devriez voir :
     🔍 VÉRIFICATION DES RDV PROCHES
     📋 Nombre total de RDV dans la base: X
     📅 RDV #1: ...
     🔔 → DÉCLENCHEMENT NOTIFICATION

5️⃣ NETTOYER
   - Décommenter et exécuter les requêtes DELETE pour supprimer les RDV de test

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

⚠️ IMPORTANT :

- Les notifications s'affichent UNIQUEMENT pour les RDV d'AUJOURD'HUI
- Le service vérifie toutes les 1 MINUTE
- Notification normale : 15 minutes avant (±1 minute)
- Notification urgente : 0-2 minutes avant
- Si vous ne voyez pas de notification, vérifiez les logs dans la console

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🎯 RÉSULTAT ATTENDU :

Après avoir créé un RDV dans 15 minutes et attendu 1 minute, vous devriez :
✅ Voir une notification bleue en haut à droite
✅ Entendre un son "beep"
✅ Voir les détails du patient dans la notification
✅ Voir les logs détaillés dans la console

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

*/
