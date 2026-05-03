package tn.esprit.services;

import java.util.ArrayList;
import java.util.List;

/**
 * CAPTCHA comportemental basé sur l'analyse des patterns de frappe.
 *
 * Principe IA :
 * - Un humain tape avec des variations naturelles (timing irrégulier)
 * - Un bot tape de façon uniforme/mécanique (timing régulier)
 *
 * Modèle utilisé : classification par règles statistiques
 * Features analysées :
 *   1. Variance des intervalles entre frappes
 *   2. Vitesse moyenne de frappe
 *   3. Ratio de pauses longues
 *   4. Entropie du pattern de frappe
 */
public class BehaviorCaptchaService {

    // Seuils calibrés empiriquement
    private static final double MIN_VARIANCE        = 15.0;   // ms² — variance minimale humaine
    private static final double MAX_AVG_SPEED       = 50.0;   // ms — vitesse max (trop rapide = bot)
    private static final double MIN_AVG_SPEED       = 5.0;    // ms — vitesse min (trop lent = bot)
    private static final double MIN_ENTROPY         = 0.3;    // entropie minimale
    private static final int    MIN_KEYSTROKES      = 4;      // frappes minimales pour analyser
    private static final double HUMAN_SCORE_THRESHOLD = 0.5;  // score > 0.5 = humain

    /**
     * Analyse les timestamps de frappe et retourne un score humain [0.0 - 1.0].
     * Score > 0.5 = humain, Score <= 0.5 = bot probable
     *
     * @param keystrokeTimestamps liste des timestamps en ms (System.currentTimeMillis())
     * @return résultat d'analyse
     */
    public AnalysisResult analyze(List<Long> keystrokeTimestamps) {
        if (keystrokeTimestamps == null || keystrokeTimestamps.size() < MIN_KEYSTROKES) {
            // Pas assez de données → on laisse passer (bénéfice du doute)
            return new AnalysisResult(true, 0.7, "Données insuffisantes — accès autorisé");
        }

        // Calculer les intervalles entre frappes
        List<Double> intervals = computeIntervals(keystrokeTimestamps);

        // Extraire les features
        double avgSpeed   = computeMean(intervals);
        double variance   = computeVariance(intervals, avgSpeed);
        double entropy    = computeEntropy(intervals);
        double pauseRatio = computePauseRatio(intervals);

        // Score composite [0.0 - 1.0]
        double score = computeHumanScore(avgSpeed, variance, entropy, pauseRatio);

        boolean isHuman = score > HUMAN_SCORE_THRESHOLD;
        String reason = buildReason(avgSpeed, variance, entropy, score);

        System.out.printf("[BehaviorCaptcha] avgSpeed=%.1fms variance=%.1f entropy=%.2f " +
                          "pauseRatio=%.2f score=%.2f → %s%n",
                          avgSpeed, variance, entropy, pauseRatio,
                          score, isHuman ? "HUMAIN" : "BOT");

        return new AnalysisResult(isHuman, score, reason);
    }

    // ── Calcul des intervalles ──────────────────────────────
    private List<Double> computeIntervals(List<Long> timestamps) {
        List<Double> intervals = new ArrayList<>();
        for (int i = 1; i < timestamps.size(); i++) {
            intervals.add((double)(timestamps.get(i) - timestamps.get(i - 1)));
        }
        return intervals;
    }

    // ── Moyenne ─────────────────────────────────────────────
    private double computeMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // ── Variance ────────────────────────────────────────────
    private double computeVariance(List<Double> values, double mean) {
        return values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average().orElse(0);
    }

    // ── Entropie de Shannon (mesure de l'irrégularité) ──────
    private double computeEntropy(List<Double> intervals) {
        if (intervals.isEmpty()) return 0;
        double max = intervals.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double min = intervals.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double range = max - min;
        if (range == 0) return 0; // Tous identiques → bot

        // Discrétiser en 5 buckets
        int[] buckets = new int[5];
        for (double v : intervals) {
            int idx = (int) Math.min(4, (v - min) / range * 5);
            buckets[idx]++;
        }

        double entropy = 0;
        int total = intervals.size();
        for (int count : buckets) {
            if (count > 0) {
                double p = (double) count / total;
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }
        return entropy / Math.log(5) / Math.log(2); // Normaliser [0-1]
    }

    // ── Ratio de pauses longues (> 500ms) ───────────────────
    private double computePauseRatio(List<Double> intervals) {
        long pauses = intervals.stream().filter(v -> v > 500).count();
        return (double) pauses / intervals.size();
    }

    // ── Score humain composite ───────────────────────────────
    private double computeHumanScore(double avgSpeed, double variance,
                                      double entropy, double pauseRatio) {
        double score = 0.0;

        // Feature 1: Variance (humain = irrégulier)
        if (variance >= MIN_VARIANCE) score += 0.35;
        else score += 0.35 * (variance / MIN_VARIANCE);

        // Feature 2: Vitesse dans la plage humaine
        if (avgSpeed >= MIN_AVG_SPEED && avgSpeed <= MAX_AVG_SPEED) score += 0.30;
        else if (avgSpeed < MIN_AVG_SPEED) score += 0.05; // trop rapide = bot
        else score += 0.20; // lent mais possible

        // Feature 3: Entropie (humain = distribution variée)
        if (entropy >= MIN_ENTROPY) score += 0.25;
        else score += 0.25 * (entropy / MIN_ENTROPY);

        // Feature 4: Pauses naturelles (humain fait des pauses)
        if (pauseRatio > 0.0 && pauseRatio < 0.5) score += 0.10;

        return Math.min(1.0, score);
    }

    private String buildReason(double avgSpeed, double variance, double entropy, double score) {
        if (score > 0.8) return "Comportement très humain détecté";
        if (score > 0.5) return "Comportement humain probable";
        if (avgSpeed < MIN_AVG_SPEED) return "Frappe trop rapide (bot probable)";
        if (variance < MIN_VARIANCE / 2) return "Frappe trop régulière (bot probable)";
        return "Comportement suspect détecté";
    }

    // ── Classe résultat ─────────────────────────────────────
    public static class AnalysisResult {
        public final boolean isHuman;
        public final double  score;      // [0.0 - 1.0]
        public final String  reason;

        public AnalysisResult(boolean isHuman, double score, String reason) {
            this.isHuman = isHuman;
            this.score   = score;
            this.reason  = reason;
        }

        public String getScoreLabel() {
            if (score > 0.8) return "✅ Humain confirmé";
            if (score > 0.5) return "✅ Humain probable";
            if (score > 0.3) return "⚠️ Suspect";
            return "❌ Bot détecté";
        }
    }
}
