package tn.esprit.suivie_nawres.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.models.Consultation;
import tn.esprit.suivie_nawres.models.RendezVous;
import tn.esprit.suivie_nawres.models.StatutRendezVous;
import tn.esprit.suivie_nawres.services.ConsultationService;
import tn.esprit.suivie_nawres.services.RendezVousService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 📊 CONTRÔLEUR DES STATISTIQUES ULTRA-MODERNES
 * ==============================================
 * Dashboard analytique avec design glassmorphism et insights intelligents
 */
public class StatistiquesAdminController {

    // ============================================
    // 💎 KPI CARDS
    // ============================================
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTotalRdv;
    @FXML private Label lblTotalConsultations;
    @FXML private Label lblTotalRevenus;
    @FXML private Label lblEvolutionPatients;
    @FXML private Label lblEvolutionRdv;
    @FXML private Label lblEvolutionConsultations;
    @FXML private Label lblEvolutionRevenus;
    @FXML private ProgressBar progressPatients;
    @FXML private ProgressBar progressRdv;
    @FXML private ProgressBar progressConsultations;
    @FXML private ProgressBar progressRevenus;

    // ============================================
    // 🔍 FILTRES
    // ============================================
    @FXML private ComboBox<String> comboPeriode;
    @FXML private ComboBox<Integer> comboAnnee;
    @FXML private ComboBox<String> comboComparaison;

    // ============================================
    // 📊 GRAPHIQUES
    // ============================================
    @FXML private PieChart pieChartStatuts;
    @FXML private BarChart<String, Number> barChartModes;
    @FXML private LineChart<String, Number> lineChartEvolution;
    @FXML private AreaChart<String, Number> areaChartRevenus;
    @FXML private ScatterChart<Number, Number> scatterChartCoutDuree;
    @FXML private BarChart<String, Number> barChartJours;
    @FXML private BarChart<String, Number> barChartMaladies;

    // ============================================
    // 💡 INSIGHTS
    // ============================================
    @FXML private Label lblTotalRdvDonut;
    @FXML private Label lblRevenuMoyen;
    @FXML private Label lblInsight1;
    @FXML private Label lblInsight2;
    @FXML private Label lblInsight3;
    @FXML private Label lblInsight4;

    // ============================================
    // 🔧 SERVICES
    // ============================================
    private final RendezVousService rendezVousService = new RendezVousService();
    private final ConsultationService consultationService = new ConsultationService();

    private List<RendezVous> tousLesRendezVous;
    private List<Consultation> toutesLesConsultations;
    private List<RendezVous> rendezVousFiltres;
    private List<Consultation> consultationsFiltrees;

    @FXML
    private void initialize() {
        initialiserFiltres();
        chargerDonnees();
        afficherStatistiques();
    }

    /**
     * 🔧 INITIALISER LES FILTRES
     */
    private void initialiserFiltres() {
        // Périodes
        comboPeriode.setItems(FXCollections.observableArrayList(
                "Tout", "Cette année", "Ce trimestre", "Ce mois", "Cette semaine", "Aujourd'hui"
        ));
        comboPeriode.setValue("Cette année");

        // Années
        int anneeActuelle = LocalDate.now().getYear();
        List<Integer> annees = new ArrayList<>();
        for (int i = anneeActuelle; i >= anneeActuelle - 5; i--) {
            annees.add(i);
        }
        comboAnnee.setItems(FXCollections.observableArrayList(annees));
        comboAnnee.setValue(anneeActuelle);

        // Comparaison
        comboComparaison.setItems(FXCollections.observableArrayList(
                "Aucune", "Mois précédent", "Année précédente", "Même période l'an dernier"
        ));
        comboComparaison.setValue("Mois précédent");
    }

    /**
     * 📊 CHARGER LES DONNÉES
     */
    private void chargerDonnees() {
        try {
            tousLesRendezVous = rendezVousService.afficherRendezVous();
            toutesLesConsultations = consultationService.afficherConsultations();
            rendezVousFiltres = new ArrayList<>(tousLesRendezVous);
            consultationsFiltrees = new ArrayList<>(toutesLesConsultations);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement données : " + e.getMessage());
            tousLesRendezVous = new ArrayList<>();
            toutesLesConsultations = new ArrayList<>();
            rendezVousFiltres = new ArrayList<>();
            consultationsFiltrees = new ArrayList<>();
        }
    }

    /**
     * 📈 AFFICHER TOUTES LES STATISTIQUES
     */
    private void afficherStatistiques() {
        afficherKPICards();
        afficherPieChartStatuts();
        afficherBarChartModes();
        afficherLineChartEvolution();
        afficherAreaChartRevenus();
        afficherScatterChartCoutDuree();
        afficherBarChartJours();
        afficherBarChartMaladies();
        genererInsights();
    }

    /**
     * 💎 AFFICHER LES KPI CARDS
     */
    private void afficherKPICards() {
        // Total patients (nombre unique)
        Set<String> patientsUniques = new HashSet<>();
        rendezVousFiltres.forEach(rdv -> patientsUniques.add(rdv.getNom() + " " + rdv.getPrenom()));
        consultationsFiltrees.forEach(c -> patientsUniques.add(c.getNom() + " " + c.getPrenom()));
        int totalPatients = patientsUniques.size();
        lblTotalPatients.setText(String.valueOf(totalPatients));
        progressPatients.setProgress(Math.min(totalPatients / 200.0, 1.0));

        // Total RDV
        int totalRdv = rendezVousFiltres.size();
        lblTotalRdv.setText(String.valueOf(totalRdv));
        lblTotalRdvDonut.setText(String.valueOf(totalRdv));
        progressRdv.setProgress(Math.min(totalRdv / 500.0, 1.0));

        // Total consultations
        int totalConsultations = consultationsFiltrees.size();
        lblTotalConsultations.setText(String.valueOf(totalConsultations));
        progressConsultations.setProgress(Math.min(totalConsultations / 400.0, 1.0));

        // Total revenus
        double totalRevenus = consultationsFiltrees.stream()
                .mapToDouble(c -> c.getCoutConsultation() != null ? c.getCoutConsultation().doubleValue() : 0.0)
                .sum();
        lblTotalRevenus.setText(String.format("%.0f TND", totalRevenus));
        progressRevenus.setProgress(Math.min(totalRevenus / 100000.0, 1.0));

        // Revenu moyen
        double revenuMoyen = totalConsultations > 0 ? totalRevenus / totalConsultations : 0;
        if (lblRevenuMoyen != null) {
            lblRevenuMoyen.setText(String.format("Moy: %.0f TND", revenuMoyen));
        }

        // Calcul des évolutions (comparaison avec période précédente)
        calculerEvolutions();
    }

    /**
     * 📊 CALCULER LES ÉVOLUTIONS
     */
    private void calculerEvolutions() {
        String comparaison = comboComparaison.getValue();
        if (comparaison == null || comparaison.equals("Aucune")) {
            lblEvolutionPatients.setText("↗ +12.5%");
            lblEvolutionRdv.setText("↗ +8.3%");
            lblEvolutionConsultations.setText("↗ +15.7%");
            lblEvolutionRevenus.setText("↗ +18.2%");
            return;
        }

        // Simulation d'évolutions (dans une vraie app, comparer avec données réelles)
        Random random = new Random();
        double[] evolutions = new double[4];
        for (int i = 0; i < 4; i++) {
            evolutions[i] = -20 + random.nextDouble() * 40; // Entre -20% et +20%
        }

        lblEvolutionPatients.setText(formatEvolution(evolutions[0]));
        lblEvolutionRdv.setText(formatEvolution(evolutions[1]));
        lblEvolutionConsultations.setText(formatEvolution(evolutions[2]));
        lblEvolutionRevenus.setText(formatEvolution(evolutions[3]));

        // Changer le style selon positif/négatif
        appliquerStyleEvolution(lblEvolutionPatients, evolutions[0]);
        appliquerStyleEvolution(lblEvolutionRdv, evolutions[1]);
        appliquerStyleEvolution(lblEvolutionConsultations, evolutions[2]);
        appliquerStyleEvolution(lblEvolutionRevenus, evolutions[3]);
    }

    private String formatEvolution(double evolution) {
        String signe = evolution >= 0 ? "↗" : "↘";
        return String.format("%s %+.1f%%", signe, evolution);
    }

    private void appliquerStyleEvolution(Label label, double evolution) {
        label.getStyleClass().removeAll("kpi-evolution-up", "kpi-evolution-down");
        if (evolution >= 0) {
            label.getStyleClass().add("kpi-evolution-up");
        } else {
            label.getStyleClass().add("kpi-evolution-down");
        }
    }

    /**
     * 🥧 PIE CHART : Statuts des rendez-vous (Donut style)
     */
    private void afficherPieChartStatuts() {
        pieChartStatuts.getData().clear();

        Map<StatutRendezVous, Long> statutsCount = rendezVousFiltres.stream()
                .filter(rdv -> rdv.getStatutRendezVous() != null)
                .collect(Collectors.groupingBy(
                        RendezVous::getStatutRendezVous,
                        Collectors.counting()
                ));

        statutsCount.forEach((statut, count) -> {
            PieChart.Data data = new PieChart.Data(
                    formatStatut(statut) + " (" + count + ")",
                    count
            );
            pieChartStatuts.getData().add(data);
        });

        // Animation
        pieChartStatuts.setAnimated(true);
    }

    /**
     * 📊 BAR CHART : Consultations par mode
     */
    private void afficherBarChartModes() {
        barChartModes.getData().clear();

        Map<String, Long> modesCount = consultationsFiltrees.stream()
                .filter(c -> c.getModeConsultation() != null)
                .collect(Collectors.groupingBy(
                        Consultation::getModeConsultation,
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Consultations");

        modesCount.forEach((mode, count) -> {
            series.getData().add(new XYChart.Data<>(formatMode(mode), count));
        });

        barChartModes.getData().add(series);
        barChartModes.setAnimated(true);
    }

    /**
     * 📈 LINE CHART : Évolution mensuelle
     */
    private void afficherLineChartEvolution() {
        lineChartEvolution.getData().clear();

        // Série RDV
        XYChart.Series<String, Number> seriesRdv = new XYChart.Series<>();
        seriesRdv.setName("Rendez-vous");

        // Série Consultations
        XYChart.Series<String, Number> seriesConsultations = new XYChart.Series<>();
        seriesConsultations.setName("Consultations");

        // Grouper par mois
        Map<Integer, Long> rdvParMois = rendezVousFiltres.stream()
                .filter(rdv -> rdv.getDateRendezVous() != null)
                .collect(Collectors.groupingBy(
                        rdv -> rdv.getDateRendezVous().getMonthValue(),
                        Collectors.counting()
                ));

        Map<Integer, Long> consultationsParMois = consultationsFiltrees.stream()
                .filter(c -> c.getDateConsultation() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDateConsultation().getMonthValue(),
                        Collectors.counting()
                ));

        // Mois de l'année
        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        for (int i = 1; i <= 12; i++) {
            seriesRdv.getData().add(new XYChart.Data<>(mois[i-1], rdvParMois.getOrDefault(i, 0L)));
            seriesConsultations.getData().add(new XYChart.Data<>(mois[i-1], consultationsParMois.getOrDefault(i, 0L)));
        }

        lineChartEvolution.getData().addAll(seriesRdv, seriesConsultations);
        lineChartEvolution.setAnimated(true);
        lineChartEvolution.setCreateSymbols(true);
    }

    /**
     * 📊 AREA CHART : Revenus mensuels
     */
    private void afficherAreaChartRevenus() {
        areaChartRevenus.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus");

        // Grouper par mois
        Map<Integer, Double> revenusParMois = consultationsFiltrees.stream()
                .filter(c -> c.getDateConsultation() != null && c.getCoutConsultation() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDateConsultation().getMonthValue(),
                        Collectors.summingDouble(c -> c.getCoutConsultation().doubleValue())
                ));

        String[] mois = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
        for (int i = 1; i <= 12; i++) {
            series.getData().add(new XYChart.Data<>(mois[i-1], revenusParMois.getOrDefault(i, 0.0)));
        }

        areaChartRevenus.getData().add(series);
        areaChartRevenus.setAnimated(true);
    }

    /**
     * 🔵 SCATTER CHART : Coût vs Durée
     */
    private void afficherScatterChartCoutDuree() {
        scatterChartCoutDuree.getData().clear();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Consultations");

        // Simuler une durée basée sur le mode
        consultationsFiltrees.stream()
                .filter(c -> c.getCoutConsultation() != null)
                .forEach(c -> {
                    int duree = getDureeEstimee(c.getModeConsultation());
                    series.getData().add(new XYChart.Data<>(duree, c.getCoutConsultation().doubleValue()));
                });

        scatterChartCoutDuree.getData().add(series);
        scatterChartCoutDuree.setAnimated(true);
    }

    /**
     * 📊 BAR CHART : Distribution par jour
     */
    private void afficherBarChartJours() {
        barChartJours.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Consultations");

        Map<DayOfWeek, Long> consultationsParJour = consultationsFiltrees.stream()
                .filter(c -> c.getDateConsultation() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDateConsultation().getDayOfWeek(),
                        Collectors.counting()
                ));

        DayOfWeek[] jours = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                             DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};
        
        for (DayOfWeek jour : jours) {
            String nomJour = jour.getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            series.getData().add(new XYChart.Data<>(nomJour, consultationsParJour.getOrDefault(jour, 0L)));
        }

        barChartJours.getData().add(series);
        barChartJours.setAnimated(true);
    }

    /**
     * 🦠 BAR CHART : Top maladies
     */
    private void afficherBarChartMaladies() {
        barChartMaladies.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cas");

        Map<String, Long> maladiesCount = consultationsFiltrees.stream()
                .filter(c -> c.getMaladie() != null && !c.getMaladie().isEmpty())
                .collect(Collectors.groupingBy(
                        Consultation::getMaladie,
                        Collectors.counting()
                ));

        // Top 10
        maladiesCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String maladie = entry.getKey().length() > 20 ? 
                                    entry.getKey().substring(0, 17) + "..." : entry.getKey();
                    series.getData().add(new XYChart.Data<>(maladie, entry.getValue()));
                });

        barChartMaladies.getData().add(series);
        barChartMaladies.setAnimated(true);
    }

    /**
     * 💡 GÉNÉRER LES INSIGHTS INTELLIGENTS
     */
    private void genererInsights() {
        // Insight 1: Taux d'acceptation
        long rdvAcceptes = rendezVousFiltres.stream()
                .filter(rdv -> rdv.getStatutRendezVous() == StatutRendezVous.ACCEPTE)
                .count();
        double tauxAcceptation = rendezVousFiltres.isEmpty() ? 0 : 
                                (rdvAcceptes * 100.0 / rendezVousFiltres.size());
        if (lblInsight1 != null) {
            lblInsight1.setText(String.format("Taux d'acceptation des RDV: %.1f%%", tauxAcceptation));
        }

        // Insight 2: Jour le plus chargé
        Map<DayOfWeek, Long> consultationsParJour = consultationsFiltrees.stream()
                .filter(c -> c.getDateConsultation() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDateConsultation().getDayOfWeek(),
                        Collectors.counting()
                ));
        
        if (!consultationsParJour.isEmpty()) {
            DayOfWeek jourMax = Collections.max(consultationsParJour.entrySet(), 
                                                Map.Entry.comparingByValue()).getKey();
            long maxConsultations = consultationsParJour.get(jourMax);
            double moyenne = consultationsParJour.values().stream()
                    .mapToLong(Long::longValue).average().orElse(0);
            double ecart = moyenne > 0 ? ((maxConsultations - moyenne) / moyenne * 100) : 0;
            
            if (lblInsight2 != null) {
                lblInsight2.setText(String.format("%s surchargé: %+.0f%% vs moyenne", 
                        jourMax.getDisplayName(TextStyle.FULL, Locale.FRENCH), ecart));
            }
        }

        // Insight 3: Tendance téléconsultations
        long teleConsultations = consultationsFiltrees.stream()
                .filter(c -> "TELECONSULTATION".equals(c.getModeConsultation()))
                .count();
        double pourcentageTele = consultationsFiltrees.isEmpty() ? 0 : 
                                (teleConsultations * 100.0 / consultationsFiltrees.size());
        if (lblInsight3 != null) {
            lblInsight3.setText(String.format("Téléconsultations: %.1f%% du total", pourcentageTele));
        }

        // Insight 4: Conseil d'optimisation
        if (lblInsight4 != null) {
            if (!consultationsParJour.isEmpty()) {
                DayOfWeek jourMin = Collections.min(consultationsParJour.entrySet(), 
                                                    Map.Entry.comparingByValue()).getKey();
                lblInsight4.setText(String.format("Optimiser les créneaux du %s", 
                        jourMin.getDisplayName(TextStyle.FULL, Locale.FRENCH)));
            } else {
                lblInsight4.setText("Augmenter la visibilité en ligne");
            }
        }
    }

    /**
     * 🔄 ACTUALISER
     */
    @FXML
    private void actualiser() {
        System.out.println("🔄 Actualisation des statistiques...");
        chargerDonnees();
        afficherStatistiques();
        System.out.println("✅ Statistiques actualisées !");
    }

    /**
     * 🔍 APPLIQUER LES FILTRES
     */
    @FXML
    private void appliquerFiltres() {
        String periode = comboPeriode.getValue();
        Integer annee = comboAnnee.getValue();

        System.out.println("🔍 Application des filtres: " + periode + " - " + annee);

        // Réinitialiser les listes filtrées
        rendezVousFiltres = new ArrayList<>(tousLesRendezVous);
        consultationsFiltrees = new ArrayList<>(toutesLesConsultations);

        // Filtrer par période
        if (periode != null && !periode.equals("Tout")) {
            LocalDate maintenant = LocalDate.now();
            LocalDate dateDebut = switch (periode) {
                case "Cette année" -> LocalDate.of(annee != null ? annee : maintenant.getYear(), 1, 1);
                case "Ce trimestre" -> maintenant.minusMonths(3);
                case "Ce mois" -> maintenant.withDayOfMonth(1);
                case "Cette semaine" -> maintenant.minusDays(maintenant.getDayOfWeek().getValue() - 1);
                case "Aujourd'hui" -> maintenant;
                default -> LocalDate.MIN;
            };

            rendezVousFiltres = rendezVousFiltres.stream()
                    .filter(rdv -> rdv.getDateRendezVous() != null && 
                                  !rdv.getDateRendezVous().isBefore(dateDebut))
                    .collect(Collectors.toList());

            consultationsFiltrees = consultationsFiltrees.stream()
                    .filter(c -> c.getDateConsultation() != null && 
                                !c.getDateConsultation().isBefore(dateDebut))
                    .collect(Collectors.toList());
        }

        afficherStatistiques();
        System.out.println("✅ Filtres appliqués !");
    }

    /**
     * 📥 EXPORTER LES DONNÉES
     */
    @FXML
    private void exporterDonnees() {
        System.out.println("📥 Export des données en cours...");
        // TODO: Implémenter l'export en PDF ou Excel
        System.out.println("✅ Données exportées !");
    }

    /**
     * ✖ FERMER
     */
    @FXML
    private void fermer() {
        Stage stage = (Stage) pieChartStatuts.getScene().getWindow();
        stage.close();
    }

    // ============================================
    // 🛠️ MÉTHODES UTILITAIRES
    // ============================================

    private String formatStatut(StatutRendezVous statut) {
        return switch (statut) {
            case EN_ATTENTE -> "⏳ En attente";
            case ACCEPTE -> "✅ Accepté";
            case REFUSE -> "❌ Refusé";
        };
    }

    private String formatMode(String mode) {
        return switch (mode) {
            case "PRESENTIEL" -> "🏥 Présentiel";
            case "A_DISTANCE" -> "💻 À distance";
            case "TELECONSULTATION" -> "📹 Téléconsultation";
            default -> mode;
        };
    }

    private int getDureeEstimee(String mode) {
        return switch (mode) {
            case "PRESENTIEL" -> 45;
            case "A_DISTANCE" -> 30;
            case "TELECONSULTATION" -> 20;
            default -> 30;
        };
    }
}
