package tn.esprit.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import tn.esprit.models.Don;

import java.io.FileOutputStream;
import java.io.IOException;

public class DonPdfService {

    // Couleurs VitaPlus
    private static final BaseColor BLEU       = new BaseColor(26, 115, 232);
    private static final BaseColor BLEU_CLAIR = new BaseColor(219, 234, 254);
    private static final BaseColor GRIS       = new BaseColor(100, 116, 139);
    private static final BaseColor VERT       = new BaseColor(22, 163, 74);

    // ──────────────────────────────────────────
    // Générer un reçu PDF pour un don
    // Retourne le chemin du fichier généré
    // ──────────────────────────────────────────
    public String genererRecuDon(Don don, String titreProjet) throws DocumentException, IOException {

        String fileName = "recu_don_" + don.getId() + ".pdf";
        String filePath = System.getProperty("user.home") + "/Downloads/" + fileName;

        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // ── En-tête ──
        Font fontTitre  = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,   BLEU);
        Font fontSous   = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, GRIS);
        Font fontLabel  = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   BaseColor.DARK_GRAY);
        Font fontValeur = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fontMontant= new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,   BLEU);
        Font fontStatut = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   VERT);

        // Logo / Titre
        Paragraph titre = new Paragraph("VitaPlus", fontTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(20);
        document.add(titre);

        // Ligne séparatrice
        LineSeparator line = new LineSeparator(1, 100, BLEU, Element.ALIGN_CENTER, -2);
        document.add(new Chunk(line));

        // Titre reçu
        Paragraph recu = new Paragraph("\nREÇU DE DON",
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BLEU));
        recu.setAlignment(Element.ALIGN_CENTER);
        recu.setSpacingAfter(20);
        document.add(recu);

        // ── Tableau des infos ──
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        ajouterLigne(table, "Donateur",    don.getPrenomDonateur() + " " + don.getNomDonateur(), fontLabel, fontValeur);
        ajouterLigne(table, "Email",       don.getEmailDonateur(),        fontLabel, fontValeur);
        ajouterLigne(table, "Projet",      titreProjet,                   fontLabel, fontValeur);
        ajouterLigne(table, "Date du don", don.getDateDon().toString(),   fontLabel, fontValeur);
        ajouterLigne(table, "Mode",        don.getModePaiement(),         fontLabel, fontValeur);

        if (don.getMessageSoutien() != null && !don.getMessageSoutien().isEmpty()) {
            ajouterLigne(table, "Message", don.getMessageSoutien(), fontLabel, fontValeur);
        }

        document.add(table);

        // ── Montant ──
        PdfPTable tableMontant = new PdfPTable(1);
        tableMontant.setWidthPercentage(100);
        PdfPCell cellMontant = new PdfPCell();
        cellMontant.setBackgroundColor(BLEU_CLAIR);
        cellMontant.setBorderColor(BLEU);
        cellMontant.setPadding(16);

        Paragraph montantLabel = new Paragraph("Montant du don", fontSous);
        montantLabel.setAlignment(Element.ALIGN_CENTER);
        Paragraph montantVal = new Paragraph(String.format("%.2f DT", don.getMontant()), fontMontant);
        montantVal.setAlignment(Element.ALIGN_CENTER);

        cellMontant.addElement(montantLabel);
        cellMontant.addElement(montantVal);
        tableMontant.addCell(cellMontant);
        tableMontant.setSpacingAfter(20);
        document.add(tableMontant);

        // ── Statut ──
        String statutTexte = "paid".equalsIgnoreCase(don.getPaymentStatus())
                ? "✅ Paiement confirmé" : "⏳ En attente de confirmation";
        Paragraph statut = new Paragraph(statutTexte, fontStatut);
        statut.setAlignment(Element.ALIGN_CENTER);
        statut.setSpacingAfter(30);
        document.add(statut);

        // ── Pied de page ──
        document.add(new Chunk(new LineSeparator(1, 100, GRIS, Element.ALIGN_CENTER, -2)));
        Paragraph footer = new Paragraph(
                "\nMerci pour votre générosité. Ce reçu est généré automatiquement par VitaPlus.",
                new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, GRIS));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        System.out.println("✅ PDF généré : " + filePath);
        return filePath;
    }

    private void ajouterLigne(PdfPTable table, String label, String valeur,
                               Font fontLabel, Font fontValeur) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBackgroundColor(new BaseColor(248, 250, 255));
        cellLabel.setPadding(8);
        cellLabel.setBorderColor(new BaseColor(226, 232, 240));

        PdfPCell cellValeur = new PdfPCell(new Phrase(valeur != null ? valeur : "-", fontValeur));
        cellValeur.setPadding(8);
        cellValeur.setBorderColor(new BaseColor(226, 232, 240));

        table.addCell(cellLabel);
        table.addCell(cellValeur);
    }
}
