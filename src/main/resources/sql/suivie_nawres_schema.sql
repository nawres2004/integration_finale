-- ============================================================
-- Module : Gestion de Suivi (Nawres)
-- Auteur : Nawres
-- Base de données : vitaplus
-- Description : Création des tables rendez_vous et consultation
-- ============================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- Structure de la table `consultation`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `consultation` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `date_consultation` date DEFAULT NULL,
  `heure_consultation` time DEFAULT NULL,
  `mode_consultation` varchar(50) DEFAULT NULL,
  `maladie` varchar(100) DEFAULT NULL,
  `diagnostic` longtext DEFAULT NULL,
  `traitement` longtext DEFAULT NULL,
  `examens_complementaires` longtext DEFAULT NULL,
  `notes_consultation` longtext DEFAULT NULL,
  `cout_consultation` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Structure de la table `rendez_vous`
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `rendez_vous` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `date_rendez_vous` date DEFAULT NULL,
  `heure_rendez_vous` time DEFAULT NULL,
  `priorite` varchar(20) DEFAULT NULL,
  `mode_consultation` varchar(50) DEFAULT NULL,
  `statut_rendez_vous` varchar(30) DEFAULT NULL,
  `notes_rendez_vous` longtext DEFAULT NULL,
  `pays` varchar(50) NOT NULL,
  `telephone` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Index pour la table `consultation`
-- --------------------------------------------------------
ALTER TABLE `consultation`
  ADD PRIMARY KEY (`id`);

-- --------------------------------------------------------
-- Index pour la table `rendez_vous`
-- --------------------------------------------------------
ALTER TABLE `rendez_vous`
  ADD PRIMARY KEY (`id`);

-- --------------------------------------------------------
-- AUTO_INCREMENT pour la table `consultation`
-- --------------------------------------------------------
ALTER TABLE `consultation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9209;

-- --------------------------------------------------------
-- AUTO_INCREMENT pour la table `rendez_vous`
-- --------------------------------------------------------
ALTER TABLE `rendez_vous`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9132;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
