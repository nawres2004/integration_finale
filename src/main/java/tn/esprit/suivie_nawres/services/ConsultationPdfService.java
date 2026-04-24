package tn.esprit.suivie_nawres.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import tn.esprit.suivie_nawres.models.Consultation;

import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * 📄 SERVICE PDF POUR LES CONSULTATIONS
 * =====================================
 * Ce service permet de générer des fichiers PDF pour les consultations médicales.
 * 
 * 🎯 FONCTIONNALITÉ PRINCIPALE :
 * - Créer un PDF professionnel avec toutes les informations de la consultation
 * - Format : En-tête + Informations patient + Détails médicaux + Coût
 * 
 * 📚 BIBLIOTHÈQUE UTILISÉE : iText 7
 * - C'est une bibliothèque Java pour créer et manipuler des PDF
 * - Comme un "Word" mais en code Java
 * 
 * 💡 ANALOGIE :
 * Imagine que tu écris une lettre médicale sur papier :
 * 1. Tu écris le titre en haut (en-tête)
 * 2. Tu notes les infos du patient (nom, prénom, date)
 * 3. Tu décris la maladie et le traitement
 * 4. Tu signes en bas
 * 
 * Ce service fait exactement la même chose, mais en PDF ! 📝
 */
public class ConsultationPdfService {

    // 🎨 COULEURS POUR LE PDF (format RGB)
    private static final DeviceRgb COULEUR_ENTETE = new DeviceRgb(41, 128, 185);  // Bleu médical
    private static final DeviceRgb COULEUR_SECTION = new DeviceRgb(52, 152, 219); // Bleu clair
    
    // 📅 FORMATAGE DES DATES ET HEURES
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 🎯 MÉTHODE PRINCIPALE : GÉNÉRER LE PDF
     * ======================================
     * 
     * @param consultation L'objet Consultation contenant toutes les données
     * @param cheminFichier Le chemin où sauvegarder le PDF (ex: "C:/Users/user/Desktop/consultation.pdf")
     * @throws Exception Si une erreur survient (fichier non accessible, données manquantes, etc.)
     * 
     * 📝 ÉTAPES DE GÉNÉRATION :
     * 1. Créer le fichier PDF vide
     * 2. Ajouter l'en-tête (titre + logo virtuel)
     * 3. Ajouter les informations du patient
     * 4. Ajouter les détails médicaux
     * 5. Ajouter le coût
     * 6. Fermer et sauvegarder le fichier
     */
    public void genererPdfConsultation(Consultation consultation, String cheminFichier) throws Exception {
        // ✅ ÉTAPE 1 : Créer le fichier PDF
        // PdfWriter = "Stylo" qui écrit dans le fichier
        // PdfDocument = Le document PDF lui-même
        // Document = L'outil pour ajouter du contenu (texte, tableaux, images)
        File fichier = new File(cheminFichier);
        PdfWriter writer = new PdfWriter(fichier);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        try {
            // ✅ ÉTAPE 2 : Ajouter l'en-tête
            ajouterEntete(document);
            
            // ✅ ÉTAPE 3 : Ajouter les informations du patient
            ajouterInformationsPatient(document, consultation);
            
            // ✅ ÉTAPE 4 : Ajouter les détails médicaux
            ajouterDetailsMedicaux(document, consultation);
            
            // ✅ ÉTAPE 5 : Ajouter le coût
            ajouterCout(document, consultation);
            
            // ✅ ÉTAPE 6 : Ajouter le pied de page
            ajouterPiedDePage(document);
            
        } finally {
            // 🔒 IMPORTANT : Toujours fermer le document pour sauvegarder le fichier
            document.close();
        }
    }

    /**
     * 📋 AJOUTER L'EN-TÊTE DU PDF
     * ===========================
     * Crée un titre professionnel en haut du document
     */
    private void ajouterEntete(Document document) {
        // 🏥 Titre principal
        Paragraph titre = new Paragraph("CONSULTATION MÉDICALE")
                .setFontSize(24)
                .setBold()
                .setFontColor(COULEUR_ENTETE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(titre);

        // 🏢 Sous-titre (nom de l'application)
        Paragraph sousTitre = new Paragraph("VitaPlus - Système de Gestion Médicale")
                .setFontSize(12)
                .setItalic()
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(sousTitre);

        // ➖ Ligne de séparation
        document.add(new Paragraph("_".repeat(80))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    /**
     * 👤 AJOUTER LES INFORMATIONS DU PATIENT
     * ======================================
     * Crée un tableau avec les données personnelles du patient
     */
    private void ajouterInformationsPatient(Document document, Consultation consultation) {
        // 📌 Titre de section
        Paragraph titreSection = new Paragraph("INFORMATIONS DU PATIENT")
                .setFontSize(14)
                .setBold()
                .setFontColor(COULEUR_SECTION)
                .setMarginBottom(10);
        document.add(titreSection);

        // 📊 Créer un tableau avec 2 colonnes (Libellé | Valeur)
        // UnitValue.createPercentArray = Définir la largeur des colonnes en %
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // ➕ Ajouter les lignes du tableau
        ajouterLigneTableau(table, "Nom", consultation.getNom());
        ajouterLigneTableau(table, "Prénom", consultation.getPrenom());
        ajouterLigneTableau(table, "Date de consultation", 
                consultation.getDateConsultation() != null 
                        ? consultation.getDateConsultation().format(FORMAT_DATE) 
                        : "Non spécifiée");
        ajouterLigneTableau(table, "Heure de consultation", 
                consultation.getHeureConsultation() != null 
                        ? consultation.getHeureConsultation().format(FORMAT_HEURE) 
                        : "Non spécifiée");
        ajouterLigneTableau(table, "Mode de consultation", consultation.getModeConsultation());

        document.add(table);
    }

    /**
     * 🩺 AJOUTER LES DÉTAILS MÉDICAUX
     * ===============================
     * Crée un tableau avec les informations médicales (maladie, diagnostic, traitement)
     */
    private void ajouterDetailsMedicaux(Document document, Consultation consultation) {
        // 📌 Titre de section
        Paragraph titreSection = new Paragraph("DÉTAILS MÉDICAUX")
                .setFontSize(14)
                .setBold()
                .setFontColor(COULEUR_SECTION)
                .setMarginBottom(10);
        document.add(titreSection);

        // 📊 Créer un tableau avec 2 colonnes
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // ➕ Ajouter les lignes du tableau
        ajouterLigneTableau(table, "Maladie", consultation.getMaladie());
        ajouterLigneTableau(table, "Diagnostic", consultation.getDiagnostic());
        ajouterLigneTableau(table, "Traitement", consultation.getTraitement());
        ajouterLigneTableau(table, "Examens complémentaires", 
                consultation.getExamensComplementaires() != null 
                        ? consultation.getExamensComplementaires() 
                        : "Aucun");
        ajouterLigneTableau(table, "Notes", 
                consultation.getNotesConsultation() != null 
                        ? consultation.getNotesConsultation() 
                        : "Aucune note");

        document.add(table);
    }

    /**
     * 💰 AJOUTER LE COÛT DE LA CONSULTATION
     * =====================================
     * Affiche le prix de la consultation de manière visible
     */
    private void ajouterCout(Document document, Consultation consultation) {
        // 📌 Titre de section
        Paragraph titreSection = new Paragraph("COÛT DE LA CONSULTATION")
                .setFontSize(14)
                .setBold()
                .setFontColor(COULEUR_SECTION)
                .setMarginBottom(10);
        document.add(titreSection);

        // 💵 Afficher le montant
        String montant = consultation.getCoutConsultation() != null 
                ? consultation.getCoutConsultation().toString() + " TND" 
                : "Non spécifié";
        
        Paragraph coutParagraphe = new Paragraph("Montant : " + montant)
                .setFontSize(16)
                .setBold()
                .setFontColor(new DeviceRgb(39, 174, 96)) // Vert pour le prix
                .setMarginBottom(30);
        document.add(coutParagraphe);
    }

    /**
     * 📄 AJOUTER LE PIED DE PAGE
     * ==========================
     * Ajoute une note en bas du document
     */
    private void ajouterPiedDePage(Document document) {
        // ➖ Ligne de séparation
        document.add(new Paragraph("_".repeat(80))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setMarginBottom(10));

        // 📝 Note de bas de page
        Paragraph piedDePage = new Paragraph("Document généré automatiquement par VitaPlus")
                .setFontSize(10)
                .setItalic()
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(piedDePage);
    }

    /**
     * 🔧 MÉTHODE UTILITAIRE : AJOUTER UNE LIGNE AU TABLEAU
     * ====================================================
     * Crée une ligne avec un libellé (ex: "Nom") et une valeur (ex: "Dupont")
     * 
     * @param table Le tableau où ajouter la ligne
     * @param libelle Le titre de la ligne (ex: "Nom")
     * @param valeur La valeur de la ligne (ex: "Dupont")
     */
    private void ajouterLigneTableau(Table table, String libelle, String valeur) {
        // 🏷️ Cellule de gauche (libellé) - en gras
        Cell celluleLibelle = new Cell()
                .add(new Paragraph(libelle).setBold())
                .setBackgroundColor(new DeviceRgb(236, 240, 241)) // Gris clair
                .setPadding(8);
        
        // 📝 Cellule de droite (valeur) - texte normal
        Cell celluleValeur = new Cell()
                .add(new Paragraph(valeur != null ? valeur : "Non spécifié"))
                .setPadding(8);

        // ➕ Ajouter les deux cellules au tableau
        table.addCell(celluleLibelle);
        table.addCell(celluleValeur);
    }
}
