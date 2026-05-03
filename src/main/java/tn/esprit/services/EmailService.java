package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import tn.esprit.models.Ordonnance;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;

public class EmailService {

    // ⚠️ CONFIGURATION (À remplir avec tes infos)
    private static final String SENDER_EMAIL = "aidanaffouti123@gmail.com";
    private static final String SENDER_PASSWORD = "fkxmjpxuhmobfdin"; // Ton mot de passe d'application

    public void envoyerOrdonnance(String toEmail, Ordonnance ordonnance) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("📋 Votre Ordonnance VitaPlus - " + ordonnance.getNomUtilisateur());

        message.setContent(buildHtmlTemplate(ordonnance), "text/html; charset=utf-8");

        Transport.send(message);
    }

    public boolean sendResetCode(String toEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL, "VitaPlus"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Code de vérification - VitaPlus");
            msg.setContent(buildResetHtmlBody(code), "text/html; charset=utf-8");

            Transport.send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildResetHtmlBody(String code) {
        return """
            <div style="font-family:Segoe UI,Arial,sans-serif;max-width:460px;margin:auto;
                        background:#F8FAFC;padding:32px;border-radius:16px;">
              <div style="text-align:center;margin-bottom:20px;">
                <h2 style="color:#0EA5E9;margin:0;font-size:22px;">🏥 VitaPlus</h2>
                <p style="color:#64748B;margin:6px 0 0;font-size:14px;">
                  Réinitialisation de mot de passe
                </p>
              </div>
              <div style="background:white;border-radius:12px;padding:28px;text-align:center;
                          box-shadow:0 2px 10px rgba(0,0,0,0.07);">
                <p style="color:#1E293B;font-size:15px;margin:0 0 16px;">
                  Votre code de vérification est :
                </p>
                <div style="font-size:38px;font-weight:bold;letter-spacing:12px;
                            color:#0EA5E9;background:#EFF6FF;padding:16px 24px;
                            border-radius:10px;display:inline-block;">
                  %s
                </div>
                <p style="color:#64748B;font-size:13px;margin:20px 0 0;">
                  Ce code expire dans <strong>5 minutes</strong>.<br>
                  Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                </p>
              </div>
              <p style="text-align:center;color:#94A3B8;font-size:11px;margin-top:20px;">
                © 2026 VitaPlus — Ne pas répondre à cet email.
              </p>
            </div>
            """.formatted(code);
    }

    private String buildHtmlTemplate(Ordonnance o) {
        String dateStr = o.getDateOrdonnance().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));

        return "<html><body style='font-family: Arial, sans-serif; background-color: #f4f7f6; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: auto; background: white; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); overflow: hidden;'>"
                +
                "<div style='background: #0EA5E9; color: white; padding: 30px; text-align: center;'>" +
                "<h1 style='margin: 0; font-size: 28px;'>VitaPlus</h1>" +
                "<p style='margin: 5px 0 0; opacity: 0.9;'>Votre ordonnance numérique</p>" +
                "</div>" +
                "<div style='padding: 30px;'>" +
                "<h2 style='color: #1e293b;'>Bonjour " + o.getNomUtilisateur() + ",</h2>" +
                "<p style='color: #64748b; font-size: 16px;'>Vous trouverez ci-dessous les détails de votre ordonnance établie le <strong>"
                + dateStr + "</strong>.</p>" +
                "<div style='margin: 25px 0; padding: 20px; background: #f8fafc; border-left: 4px solid #0EA5E9; border-radius: 4px;'>"
                +
                "<p style='margin: 0 0 10px;'><strong>📝 Instructions :</strong></p>" +
                "<p style='color: #334155; font-style: italic;'>" + o.getInstructions().replace("\n", "<br>") + "</p>" +
                "<p style='margin: 15px 0 0;'><strong>⏱ Durée :</strong> " + o.getDureeTraitement() + "</p>" +
                "</div>" +
                "<p style='color: #64748b; font-size: 14px;'>N'oubliez pas de respecter les dosages indiqués par votre médecin.</p>"
                +
                "</div>" +
                "<div style='background: #f1f5f9; padding: 15px; text-align: center; font-size: 12px; color: #94a3b8;'>"
                +
                "<p>© 2026 VitaPlus - Système de Gestion de Santé</p>" +
                "</div>" +
                "</div></body></html>";
    }
}
