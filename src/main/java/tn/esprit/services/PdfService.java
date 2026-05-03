package tn.esprit.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import tn.esprit.models.Ordonnance;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PdfService {

    private QrCodeService qrCodeService = new QrCodeService();

    public void generateOrdonnancePdf(Ordonnance ordonnance, String filePath) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // QR Code
        try {
            String qrData = String.format("Patient: %s\nDate: %s\nInstructions: %s",
                    ordonnance.getNomUtilisateur() != null ? ordonnance.getNomUtilisateur() : "N/A", 
                    ordonnance.getDateOrdonnance(), 
                    ordonnance.getInstructions());
            
            byte[] qrBytes = qrCodeService.generateQrCodeByteArray(qrData, 100, 100);
            com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(qrBytes);
            qrImage.setAbsolutePosition(470, 730);
            document.add(qrImage);
        } catch (Exception e) {
            System.err.println("Could not add QR code: " + e.getMessage());
        }

        // Header Bar (Bleu)
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell(new Phrase("VITAPLUS - SANTÉ CONNECTÉE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE)));
        headerCell.setBackgroundColor(new Color(14, 165, 233));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(15);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        document.add(new Paragraph("\n"));

        // QR Code positionné élégamment
        try {
            String qrData = String.format("Ordonnance VitaPlus\nPatient: %s\nDate: %s",
                    ordonnance.getNomUtilisateur(), ordonnance.getDateOrdonnance());
            byte[] qrBytes = qrCodeService.generateQrCodeByteArray(qrData, 80, 80);
            com.lowagie.text.Image qrImage = com.lowagie.text.Image.getInstance(qrBytes);
            qrImage.setAbsolutePosition(PageSize.A4.getWidth() - 110, PageSize.A4.getHeight() - 100);
            document.add(qrImage);
        } catch (Exception e) {}

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(30, 41, 59));
        Paragraph title = new Paragraph("ORDONNANCE MÉDICALE", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingBefore(20);
        title.setSpacingAfter(30);
        document.add(title);

        // Info Box
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(100, 116, 139));
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(30, 41, 59));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(30);

        addTableCellWithLabel(infoTable, "PATIENT", ordonnance.getNomUtilisateur(), labelFont, contentFont);
        addTableCellWithLabel(infoTable, "DATE D'ÉMISSION", ordonnance.getDateOrdonnance().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), labelFont, contentFont);
        addTableCellWithLabel(infoTable, "DURÉE DU TRAITEMENT", ordonnance.getDureeTraitement(), labelFont, contentFont);

        document.add(infoTable);

        // Instructions Section
        Paragraph instrHeader = new Paragraph("INSTRUCTIONS ET PRESCRIPTIONS", labelFont);
        instrHeader.setSpacingAfter(10);
        document.add(instrHeader);

        PdfPTable instrBox = new PdfPTable(1);
        instrBox.setWidthPercentage(100);
        PdfPCell instrCell = new PdfPCell(new Phrase(ordonnance.getInstructions(), contentFont));
        instrCell.setPadding(20);
        instrCell.setBackgroundColor(new Color(248, 250, 252));
        instrCell.setBorderColor(new Color(226, 232, 240));
        instrCell.setBorderWidth(1);
        instrBox.addCell(instrCell);
        document.add(instrBox);

        // Signature
        Paragraph signature = new Paragraph("\n\n\nCachet et Signature du Médecin", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);

        document.close();
    }

    private void addTableCellWithLabel(PdfPTable table, String label, String value, Font labelFont, Font contentFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10);
        
        Paragraph p = new Paragraph(label + "\n", labelFont);
        p.add(new Chunk(value != null ? value : "N/A", contentFont));
        cell.addElement(p);
        
        table.addCell(cell);
    }
}
