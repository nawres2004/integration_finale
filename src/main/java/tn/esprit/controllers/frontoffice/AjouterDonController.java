package tn.esprit.controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.models.Don;
import tn.esprit.models.Projet;
import tn.esprit.services.DonService;
import tn.esprit.services.DonEmailService;
import tn.esprit.services.DonPdfService;
import tn.esprit.services.ProjetService;
import tn.esprit.services.ProjetSimilariteService;
import tn.esprit.services.SessionService;
import tn.esprit.services.StripeService;

import java.util.List;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

public class AjouterDonController {

    @FXML private Label            lblProjet;
    @FXML private Label            topUserLabel;
    @FXML private TextField        tfNom;
    @FXML private TextField        tfPrenom;
    @FXML private TextField        tfEmail;
    @FXML private TextField        tfMontant;
    @FXML private ComboBox<String> cbMode;
    @FXML private TextField        tfMessage;
    @FXML private Button           btnVerifier;
    @FXML private Button           btnRecu;

    private final DonService    ds  = new DonService();
    private final DonEmailService  es  = new DonEmailService();
    private final StripeService ss  = new StripeService();
    private final DonPdfService    pdf = new DonPdfService();
    private final ProjetService projetService = new ProjetService();
    private final ProjetSimilariteService similariteService = new ProjetSimilariteService();

    private Projet projetSelectionne;
    private String stripePaymentId;
    private Don    dernierDon; // stocké après enregistrement

    public void initialize() {
        cbMode.setItems(FXCollections.observableArrayList("stripe", "virement", "sur_place"));

        if (btnVerifier != null) {
            btnVerifier.setVisible(false);
            btnVerifier.setManaged(false);
        }

        cbMode.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (btnVerifier != null) {
                boolean isStripe = "stripe".equals(newVal);
                btnVerifier.setVisible(isStripe);
                btnVerifier.setManaged(isStripe);
            }
        });

        // Pré-remplir avec les infos du compte connecté
        tn.esprit.models.Utilisateur u = SessionService.getInstance().getCurrentUser();
        if (u != null) {
            if (u.getNom()    != null) tfNom.setText(u.getNom());
            if (u.getPrenom() != null) tfPrenom.setText(u.getPrenom());
            if (u.getEmail()  != null) tfEmail.setText(u.getEmail());
            if (topUserLabel  != null) {
                String full = (u.getPrenom() != null ? u.getPrenom() : "") + " " +
                              (u.getNom() != null ? u.getNom() : "");
                topUserLabel.setText("💰 " + full.trim());
            }
        }
    }

    public void setProjet(Projet p) {
        this.projetSelectionne = p;
        lblProjet.setText("Projet : " + p.getTitreProjet());
    }

    @FXML
    public void ajouterDon() {
        String nom    = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String email  = tfEmail.getText().trim();
        String mont   = tfMontant.getText().trim();

        if (projetSelectionne == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun projet sélectionné."); return;
        }
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()
                || mont.isEmpty() || cbMode.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Tous les champs sont obligatoires."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Saisissez un email valide."); return;
        }

        try {
            double montant = Double.parseDouble(mont);
            if (montant <= 0) {
                showAlert(Alert.AlertType.WARNING, "Montant invalide", "Le montant doit être > 0."); return;
            }

            String mode = cbMode.getValue();

            // ── CAS STRIPE ──
            if ("stripe".equals(mode)) {
                initierPaiementStripe(montant);
                return;
            }

            // ── CAS VIREMENT / SUR PLACE ──
            String statut = "virement".equals(mode) ? "pending" : "paid";
            enregistrerDon(montant, mode, email, nom, prenom, null, statut);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre.");
        }
    }

    // ──────────────────────────────────────────
    // STRIPE : Créer un PaymentIntent
    // ──────────────────────────────────────────
    private void initierPaiementStripe(double montant) {
        try {
            demarrerServeurLocal();

            String[] result = ss.creerSessionCheckout(montant, projetSelectionne.getTitreProjet());
            stripePaymentId = result[0]; // stocker l'ID de session
            String url      = result[1]; // URL de la page Stripe

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }

            if (btnVerifier != null) {
                btnVerifier.setVisible(true);
                btnVerifier.setManaged(true);
            }

            showAlert(Alert.AlertType.INFORMATION,
                    "Paiement Stripe",
                    "La page de paiement Stripe s'est ouverte.\n" +
                    "Carte test : 4242 4242 4242 4242\n" +
                    "Date : 12/34 | CVC : 123\n\n" +
                    "Après paiement, cliquez 'Confirmer paiement'.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Stripe", e.getMessage());
        }
    }

    private void demarrerServeurLocal() {
        new Thread(() -> {
            try {
                com.sun.net.httpserver.HttpServer server =
                    com.sun.net.httpserver.HttpServer.create(
                        new java.net.InetSocketAddress(9090), 0);

                server.createContext("/success", exchange -> {
                    String html = """
                        <html><body style='font-family:Arial;text-align:center;padding:60px;background:#e8f4fd;'>
                            <h1 style='color:#1a73e8;'>✅ Paiement réussi !</h1>
                            <p>Merci pour votre don. Retournez sur l'application VitaPlus.</p>
                            <script>setTimeout(()=>window.close(),3000);</script>
                        </body></html>""";
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, html.getBytes().length);
                    exchange.getResponseBody().write(html.getBytes());
                    exchange.getResponseBody().close();
                    server.stop(1);
                });

                server.createContext("/cancel", exchange -> {
                    String html = """
                        <html><body style='font-family:Arial;text-align:center;padding:60px;background:#fee2e2;'>
                            <h1 style='color:#e53935;'>❌ Paiement annulé</h1>
                            <p>Retournez sur l'application VitaPlus.</p>
                            <script>setTimeout(()=>window.close(),3000);</script>
                        </body></html>""";
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, html.getBytes().length);
                    exchange.getResponseBody().write(html.getBytes());
                    exchange.getResponseBody().close();
                    server.stop(1);
                });

                server.start();
            } catch (Exception e) {
                System.err.println("Serveur local : " + e.getMessage());
            }
        }).start();
    }

    // ──────────────────────────────────────────
    // STRIPE : Confirmer le paiement (mode test)
    // ──────────────────────────────────────────
    @FXML
    public void verifierPaiementStripe() {
        if (stripePaymentId == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Aucun paiement initié."); return;
        }
        try {
            // En mode test, on enregistre directement avec statut "paid"
            double montant = Double.parseDouble(tfMontant.getText().trim());
            enregistrerDon(
                    montant, "stripe",
                    tfEmail.getText().trim(),
                    tfNom.getText().trim(),
                    tfPrenom.getText().trim(),
                    stripePaymentId,
                    "paid"
            );
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ──────────────────────────────────────────
    // Enregistrer le don + envoyer email
    // ──────────────────────────────────────────
    private void enregistrerDon(double montant, String mode, String email,
                                 String nom, String prenom,
                                 String paymentId, String statut) {
        try {
            Don d = new Don(
                    montant,
                    Date.valueOf(java.time.LocalDate.now()),
                    mode, email,
                    tfMessage.getText().trim(),
                    projetSelectionne.getId(),
                    nom, prenom,
                    paymentId,
                    statut
            );

            ds.ajouter(d);
            dernierDon = d;

            // Afficher bouton reçu uniquement si statut "paid"
            if (btnRecu != null && "paid".equalsIgnoreCase(statut)) {
                btnRecu.setVisible(true);
                btnRecu.setManaged(true);
            }

            // Email selon statut
            final String projetTitre = projetSelectionne.getTitreProjet();
            Thread emailThread = new Thread(() -> {
                if ("paid".equalsIgnoreCase(statut)) {
                    es.envoyerConfirmationDon(email, prenom, nom, montant, projetTitre);
                } else if ("pending".equalsIgnoreCase(statut)) {
                    es.envoyerDonEnAttente(email, prenom, nom, montant, projetTitre);
                }
            });
            emailThread.setDaemon(false);
            emailThread.start();

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "✅ Don enregistré !\nStatut : " + statut.toUpperCase()
                    + "\n📧 Email de confirmation envoyé.");

            afficherSuggestions();

            // Ne pas naviguer automatiquement — laisser le donateur télécharger son reçu

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    private void afficherSuggestions() {
        try {
            List<Projet> tous = projetService.afficherAll();
            List<Projet> suggestions = similariteService.suggerer(projetSelectionne, tous, 3);

            if (suggestions.isEmpty()) return;

            StringBuilder sb = new StringBuilder("Vous pourriez aussi soutenir :\n\n");
            for (int i = 0; i < suggestions.size(); i++) {
                Projet s = suggestions.get(i);
                sb.append(i + 1).append(". ").append(s.getTitreProjet()).append("\n");
                double pct = s.getObjectifFinancier() > 0
                        ? s.getMontantCollecte() / s.getObjectifFinancier() * 100 : 0;
                sb.append("   📊 Progression : ").append(String.format("%.0f%%", pct)).append("\n\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("🤖 Projets similaires");
            alert.setHeaderText("Merci pour votre don ! Voici d'autres projets qui pourraient vous intéresser :");
            alert.setContentText(sb.toString());
            alert.showAndWait();

        } catch (SQLException e) {
            System.err.println("Suggestions : " + e.getMessage());
        }
    }

    @FXML
    public void telechargerRecu() {
        if (dernierDon == null) return;
        try {
            String chemin = pdf.genererRecuDon(dernierDon, projetSelectionne.getTitreProjet());
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(chemin));
            }
            showAlert(Alert.AlertType.INFORMATION, "Reçu PDF",
                    "✅ Reçu téléchargé :\n" + chemin);
            goToFrontProjets();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur PDF", e.getMessage());
        }
    }

    @FXML
    public void goToFrontProjets() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/frontoffice/FrontProjets.fxml"));
            tfNom.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
