package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class DonEmailService {

    private static final String FROM     = "ranimbenhamda9@gmail.com";
    private static final String PASSWORD = "orwwxelpbccrzrbm";

    private Session creerSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });
    }

    // ──────────────────────────────────────────
    // Email don PAID — confirmation au donateur
    // ──────────────────────────────────────────
    public void envoyerConfirmationDon(String toEmail, String prenom, String nom,
                                        double montant, String projet) {
        String sujet = "✅ Confirmation de votre don — VitaPlus";
        String corps = """
                <html><body style="font-family:Arial,sans-serif; color:#333; padding:20px;">
                    <div style="max-width:600px; margin:auto; border:1px solid #e0e0e0;
                                border-radius:12px; padding:30px;">
                        <h2 style="color:#1a73e8;">🏥 VitaPlus — Confirmation de don</h2>
                        <p>Bonjour <strong>%s %s</strong>,</p>
                        <p>Votre don a bien été enregistré. Merci pour votre générosité !</p>
                        <div style="background:#f0f7ff; border-radius:8px; padding:16px; margin:20px 0;">
                            <p><strong>Projet :</strong> %s</p>
                            <p><strong>Montant :</strong> <span style="color:#1a73e8; font-size:18px;">%.2f DT</span></p>
                            <p><strong>Statut :</strong> <span style="color:#065f46; background:#d1fae5;
                               padding:3px 10px; border-radius:20px;">✅ Payé</span></p>
                        </div>
                        <p style="color:#888; font-size:12px;">Merci de contribuer à un monde meilleur.</p>
                        <p style="color:#1a73e8; font-weight:bold;">L'équipe VitaPlus</p>
                    </div>
                </body></html>
                """.formatted(prenom, nom, projet, montant);

        envoyerEmail(toEmail, sujet, corps);
    }

    // ──────────────────────────────────────────
    // Email don PENDING — virement en attente
    // ──────────────────────────────────────────
    public void envoyerDonEnAttente(String toEmail, String prenom, String nom,
                                     double montant, String projet) {
        String sujet = "⏳ Votre don est en attente — VitaPlus";
        String corps = """
                <html><body style="font-family:Arial,sans-serif; color:#333; padding:20px;">
                    <div style="max-width:600px; margin:auto; border:1px solid #e0e0e0;
                                border-radius:12px; padding:30px;">
                        <h2 style="color:#1a73e8;">🏥 VitaPlus — Don en attente</h2>
                        <p>Bonjour <strong>%s %s</strong>,</p>
                        <p>Votre don par virement est en cours de traitement.</p>
                        <div style="background:#fffbeb; border-radius:8px; padding:16px; margin:20px 0;">
                            <p><strong>Projet :</strong> %s</p>
                            <p><strong>Montant :</strong> <span style="color:#92400e; font-size:18px;">%.2f DT</span></p>
                            <p><strong>Statut :</strong> <span style="color:#92400e; background:#fef3c7;
                               padding:3px 10px; border-radius:20px;">⏳ En attente</span></p>
                        </div>
                        <p>Votre don sera validé dès réception du virement.</p>
                        <p style="color:#1a73e8; font-weight:bold;">L'équipe VitaPlus</p>
                    </div>
                </body></html>
                """.formatted(prenom, nom, projet, montant);

        envoyerEmail(toEmail, sujet, corps);
    }

    // ──────────────────────────────────────────
    // Email admin — objectif atteint
    // ──────────────────────────────────────────
    public void envoyerObjectifAtteint(String adminEmail, String titreProjet, double objectif) {
        String sujet = "🎉 Objectif atteint — " + titreProjet;
        String corps = """
                <html><body style="font-family:Arial,sans-serif; color:#333; padding:20px;">
                    <div style="max-width:600px; margin:auto; border:1px solid #e0e0e0;
                                border-radius:12px; padding:30px;">
                        <h2 style="color:#1a73e8;">🎉 Objectif atteint !</h2>
                        <p>Le projet <strong>%s</strong> a atteint son objectif de
                           <strong>%.2f DT</strong>.</p>
                        <p>Connectez-vous au dashboard pour plus de détails.</p>
                        <p style="color:#1a73e8; font-weight:bold;">L'équipe VitaPlus</p>
                    </div>
                </body></html>
                """.formatted(titreProjet, objectif);

        envoyerEmail(adminEmail, sujet, corps);
    }

    // ──────────────────────────────────────────
    // Méthode commune d'envoi
    // ──────────────────────────────────────────
    private void envoyerEmail(String to, String sujet, String corps) {
        try {
            Session session = creerSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(sujet);
            message.setContent(corps, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("✅ Email envoyé à : " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur email : " + e.getMessage());
        }
    }
}
