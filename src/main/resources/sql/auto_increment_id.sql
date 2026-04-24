-- ========================================
-- 🔧 SCRIPT SQL : AUTO-INCREMENT pour les ID
-- ========================================
-- Ce script modifie les tables pour que l'ID soit généré automatiquement

-- 1️⃣ Modifier la table rendez_vous
ALTER TABLE rendez_vous 
MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- 2️⃣ Modifier la table consultation
ALTER TABLE consultation 
MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY;

-- ========================================
-- ✅ Après avoir exécuté ce script :
-- - L'ID sera généré automatiquement
-- - Tu n'as plus besoin de le saisir manuellement
-- ========================================
