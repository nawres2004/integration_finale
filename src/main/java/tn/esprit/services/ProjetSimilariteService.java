package tn.esprit.services;

import tn.esprit.models.Projet;

import java.util.*;

/**
 * Suggestion de projets similaires via TF-IDF + similarité cosinus sur les titres.
 */
public class ProjetSimilariteService {

    // Mots vides à ignorer (stop words FR/EN)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "le","la","les","de","du","des","un","une","et","en","au","aux",
        "pour","par","sur","dans","avec","the","of","for","and","a","an","to","in"
    ));

    /**
     * Retourne les `topN` projets les plus similaires au projet courant.
     */
    public List<Projet> suggerer(Projet courant, List<Projet> tous, int topN) {
        List<Projet> candidats = new ArrayList<>();
        for (Projet p : tous) {
            if (p.getId() != courant.getId() && p.getMontantCollecte() < p.getObjectifFinancier()) {
                candidats.add(p);
            }
        }

        if (candidats.isEmpty()) return Collections.emptyList();

        // Construire le corpus : tous les titres + descriptions
        List<String> corpus = new ArrayList<>();
        corpus.add(texte(courant));
        for (Projet p : candidats) corpus.add(texte(p));

        // Vocabulaire global
        Set<String> vocab = new LinkedHashSet<>();
        List<List<String>> tokenises = new ArrayList<>();
        for (String doc : corpus) {
            List<String> tokens = tokeniser(doc);
            tokenises.add(tokens);
            vocab.addAll(tokens);
        }

        List<String> vocabList = new ArrayList<>(vocab);
        int N = corpus.size();

        // Calcul IDF
        Map<String, Double> idf = new HashMap<>();
        for (String mot : vocabList) {
            long df = tokenises.stream().filter(t -> t.contains(mot)).count();
            idf.put(mot, Math.log((double)(N + 1) / (df + 1)) + 1);
        }

        // Vecteur TF-IDF du projet courant (index 0)
        double[] vecCourant = vecteur(tokenises.get(0), vocabList, idf);

        // Calculer la similarité cosinus avec chaque candidat
        List<double[]> scores = new ArrayList<>(); // [index_candidat, score]
        for (int i = 0; i < candidats.size(); i++) {
            double[] vecCandidat = vecteur(tokenises.get(i + 1), vocabList, idf);
            double sim = cosinusSimilarite(vecCourant, vecCandidat);
            scores.add(new double[]{i, sim});
        }

        // Trier par score décroissant
        scores.sort((a, b) -> Double.compare(b[1], a[1]));

        List<Projet> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, scores.size()); i++) {
            int idx = (int) scores.get(i)[0];
            if (scores.get(i)[1] > 0) { // seulement si similarité > 0
                suggestions.add(candidats.get(idx));
            }
        }

        return suggestions;
    }

    private String texte(Projet p) {
        String titre = p.getTitreProjet() != null ? p.getTitreProjet() : "";
        String desc  = p.getDescription()  != null ? p.getDescription()  : "";
        return titre + " " + desc;
    }

    private List<String> tokeniser(String texte) {
        List<String> tokens = new ArrayList<>();
        for (String mot : texte.toLowerCase().split("[^a-zA-ZÀ-ÿ]+")) {
            if (mot.length() > 2 && !STOP_WORDS.contains(mot)) {
                tokens.add(mot);
            }
        }
        return tokens;
    }

    private double[] vecteur(List<String> tokens, List<String> vocab, Map<String, Double> idf) {
        double[] vec = new double[vocab.size()];
        Map<String, Long> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1L, Long::sum);

        for (int i = 0; i < vocab.size(); i++) {
            String mot = vocab.get(i);
            double tfVal = tf.getOrDefault(mot, 0L) / (double) Math.max(tokens.size(), 1);
            vec[i] = tfVal * idf.getOrDefault(mot, 1.0);
        }
        return vec;
    }

    private double cosinusSimilarite(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
