package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import org.example.App;
import org.example.entities.Article;
import org.example.entities.Categorie;
import org.example.entities.User;
import org.example.services.ServiceArticle;
import org.example.services.ServiceCategorie;
import org.example.services.ServiceCommentaire;
import org.example.services.ServiceVote;
import org.example.utils.UserSession;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ArticleListController implements Initializable {

    @FXML
    private FlowPane articlesGrid;

    @FXML
    private Button sidebarCategoriesBtn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button statsButton;

    @FXML
    private Button notifBtn;

    private ServiceArticle serviceArticle = new ServiceArticle();
    private ServiceCategorie serviceCategorie = new ServiceCategorie();
    private ServiceVote serviceVote = new ServiceVote();
    private ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
    private org.example.services.ServiceNotification serviceNotification = new org.example.services.ServiceNotification();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Hide categories button if not admin
        if (sidebarCategoriesBtn != null && UserSession.getInstance() != null) {
            if (UserSession.getInstance().getUser().getIdRole() != 1) {
                sidebarCategoriesBtn.setVisible(false);
                sidebarCategoriesBtn.setManaged(false);
            }
        }
        // Setup sort combo box
        if (sortComboBox != null) {
            sortComboBox.setItems(FXCollections.observableArrayList("Le plus récent", "Le plus ancien"));
            sortComboBox.setValue("Le plus récent");
            sortComboBox.setOnAction(e -> loadArticleCards());
        }

        // Setup search listener
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> loadArticleCards());
        }

        updateNotificationCount();
        loadArticleCards();
    }

    private void updateNotificationCount() {
        if (UserSession.getInstance() != null && notifBtn != null) {
            int count = serviceNotification.getUnreadCount(UserSession.getInstance().getUser().getIdUtilisateur());
            if (count > 0) {
                notifBtn.setText("🔔 (" + count + ")");
                notifBtn.setStyle("-fx-background-radius: 20; -fx-padding: 8 15; -fx-background-color: #ff4757; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                notifBtn.setText("🔔 (0)");
                notifBtn.setStyle("-fx-background-radius: 20; -fx-padding: 8 15; -fx-background-color: #EBF5FB; -fx-text-fill: #0C5283;");
            }
        }
    }

    private void loadArticleCards() {
        articlesGrid.getChildren().clear();
        List<Article> articles = serviceArticle.getAll();
        
        // Filter by search
        if (searchField != null && searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
            String lowerCaseFilter = searchField.getText().toLowerCase();
            articles = articles.stream()
                .filter(a -> a.getTitre() != null && a.getTitre().toLowerCase().contains(lowerCaseFilter))
                .collect(Collectors.toList());
        }
        
        // Sort
        if (sortComboBox != null && sortComboBox.getValue() != null) {
            if (sortComboBox.getValue().equals("Le plus ancien")) {
                articles.sort((a1, a2) -> {
                    if (a1.getDateCreation() == null || a2.getDateCreation() == null) return 0;
                    return a1.getDateCreation().compareTo(a2.getDateCreation());
                });
            } else {
                articles.sort((a1, a2) -> {
                    if (a1.getDateCreation() == null || a2.getDateCreation() == null) return 0;
                    return a2.getDateCreation().compareTo(a1.getDateCreation());
                });
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        if (articles.isEmpty()) {
            Label empty = new Label("No articles found.");
            empty.setStyle("-fx-text-fill: #63B8F3; -fx-font-size: 14px; -fx-padding: 40;");
            articlesGrid.getChildren().add(empty);
            return;
        }

        for (Article article : articles) {
            articlesGrid.getChildren().add(buildArticleCard(article, formatter));
        }
    }

    private VBox buildArticleCard(Article article, DateTimeFormatter formatter) {
        // Card container
        VBox card = new VBox(10);
        card.getStyleClass().add("article-card");
        card.setPrefWidth(420); // Increased width 
        card.setMinWidth(420);  // Prevent card from shrinking and crushing buttons

        // Title
        Label title = new Label(article.getTitre());
        title.getStyleClass().add("article-card-title");
        title.setWrapText(true);

        // Content excerpt
        String excerpt = article.getContenu();
        if (excerpt != null && excerpt.length() > 90) {
            excerpt = excerpt.substring(0, 90) + "...";
        }
        Label content = new Label(excerpt);
        content.getStyleClass().add("article-card-content");
        content.setWrapText(true);

        // Date
        String dateStr = article.getDateCreation() != null
                ? article.getDateCreation().format(formatter)
                : "N/A";
        Label date = new Label("📅 " + dateStr);
        date.getStyleClass().add("article-card-date");

        User currentUser = UserSession.getInstance().getUser();

        // Vote Actions
        HBox voteBox = new HBox(15);
        voteBox.setAlignment(Pos.CENTER_LEFT);
        voteBox.setStyle("-fx-padding: 5 0 5 0;");
        
        Button likeBtn = new Button();
        Button dislikeBtn = new Button();
        Label commentCountLabel = new Label();
        
        likeBtn.setCursor(javafx.scene.Cursor.HAND);
        dislikeBtn.setCursor(javafx.scene.Cursor.HAND);
        commentCountLabel.setCursor(javafx.scene.Cursor.HAND);
        commentCountLabel.setOnMouseClicked(e -> handleViewComments(article));
        
        Runnable updateVotesUI = () -> {
            int likes = serviceVote.getLikesCounter(article.getIdArticle());
            int dislikes = serviceVote.getDislikesCounter(article.getIdArticle());
            int comments = serviceCommentaire.getCommentsCounter(article.getIdArticle());
            
            likeBtn.setText("👍 " + likes);
            dislikeBtn.setText("👎 " + dislikes);
            commentCountLabel.setText("💬 " + comments);
            commentCountLabel.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-padding: 4 10 4 10;");
            
            if (currentUser != null) {
                int userVote = serviceVote.getUserVote(article.getIdArticle(), currentUser.getIdUtilisateur());
                if (userVote == 1) {
                    likeBtn.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-background-radius: 20; -fx-border-radius: 20; -fx-font-weight: bold;");
                    dislikeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-border-color: transparent;");
                } else if (userVote == -1) {
                    dislikeBtn.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; -fx-background-radius: 20; -fx-border-radius: 20; -fx-font-weight: bold;");
                    likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-border-color: transparent;");
                } else {
                    likeBtn.setStyle("-fx-background-color: #F0F4F8; -fx-text-fill: #555; -fx-background-radius: 20; -fx-border-color: transparent;");
                    dislikeBtn.setStyle("-fx-background-color: #F0F4F8; -fx-text-fill: #555; -fx-background-radius: 20; -fx-border-color: transparent;");
                }
            }
        };
        
        updateVotesUI.run();
        
        likeBtn.setOnAction(e -> {
            if (currentUser != null) {
                serviceVote.castVote(article.getIdArticle(), currentUser.getIdUtilisateur(), 1);
                updateVotesUI.run();
            }
        });
        
        dislikeBtn.setOnAction(e -> {
            if (currentUser != null) {
                serviceVote.castVote(article.getIdArticle(), currentUser.getIdUtilisateur(), -1);
                updateVotesUI.run();
            }
        });
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        voteBox.getChildren().addAll(likeBtn, dislikeBtn, spacer, commentCountLabel);

        // Spacer line
        Label divider = new Label("");
        divider.setStyle("-fx-border-color: #EBF5FB; -fx-border-width: 1 0 0 0; -fx-pref-width: 9999;");

        // Action buttons
        boolean isOwnArticle = (currentUser != null && article.getIdUtilisateur() == currentUser.getIdUtilisateur());
        boolean isAdmin = (currentUser != null && currentUser.getIdRole() == 1);

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);

        // Edit button - for owner AND for admin
        if (isOwnArticle || isAdmin) {
            Button editBtn = new Button("✏ Edit");
            editBtn.getStyleClass().add("btn-warning");
            editBtn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            editBtn.setOnAction(e -> handleEdit(article));
            actions.getChildren().add(editBtn);
        }

        // Delete button - for owner AND for admin
        if (isOwnArticle || isAdmin) {
            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            deleteBtn.setOnAction(e -> handleDelete(article));
            actions.getChildren().add(deleteBtn);
        }

        Button commentsBtn = new Button("💬 Comments");
        commentsBtn.getStyleClass().add("btn-primary");
        commentsBtn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        commentsBtn.setOnAction(e -> handleViewComments(article));
        actions.getChildren().add(commentsBtn);

        Button pdfBtn = new Button("📄 PDF");
        pdfBtn.getStyleClass().add("btn-success");
        pdfBtn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        pdfBtn.setOnAction(e -> handleGeneratePDF(article));
        actions.getChildren().add(pdfBtn);

        card.getChildren().addAll(title, content, date, voteBox, divider, actions);
        return card;
    }

    private void handleViewComments(Article article) {
        try {
            ArticleCommentsController.setArticle(article);
            App.setRoot("article_comments");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(Article article) {
        try {
            ArticleFormController.setArticleToEdit(article);
            App.setRoot("article_form");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Article article) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText(null);
        alert.setContentText("Delete the article: '" + article.getTitre() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceArticle.delete(article.getIdArticle());
            loadArticleCards();
        }
    }

    @FXML
    void handleAddNew(ActionEvent event) throws IOException {
        ArticleFormController.clearArticleToEdit();
        App.setRoot("article_form");
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        App.setRoot("primary");
    }

    @FXML
    void handleCategories(ActionEvent event) throws IOException {
        if (UserSession.getInstance() != null && UserSession.getInstance().getUser().getIdRole() == 1) {
            App.setRoot("add_categorie");
        }
    }

    @FXML
    void handleLogOut(ActionEvent event) throws IOException {
        UserSession.cleanUserSession();
        App.setRoot("signin");
    }

    @FXML
    void handleShowStats(ActionEvent event) {
        List<Article> articles = serviceArticle.getAll();
        Map<Integer, Long> categoryCounts = articles.stream()
                .collect(Collectors.groupingBy(Article::getIdCategorie, Collectors.counting()));
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<Integer, Long> entry : categoryCounts.entrySet()) {
            Categorie cat = serviceCategorie.getOne(entry.getKey());
            String catName = (cat != null) ? cat.getNom() : "Inconnu (" + entry.getKey() + ")";
            pieChartData.add(new PieChart.Data(catName, entry.getValue()));
        }
        
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Articles par catégorie");
        
        StackPane root = new StackPane(chart);
        Scene scene = new Scene(root, 600, 500);
        Stage stage = new Stage();
        stage.setTitle("Statistiques des Articles");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleShowNotifications(ActionEvent event) {
        if (UserSession.getInstance() == null) return;
        int userId = UserSession.getInstance().getUser().getIdUtilisateur();
        java.util.List<org.example.entities.Notification> notifs = serviceNotification.getByUser(userId);
        
        if (notifs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Notifications");
            alert.setHeaderText(null);
            alert.setContentText("Vous n'avez aucune notification.");
            alert.show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (org.example.entities.Notification n : notifs) {
            String status = n.isRead() ? "" : "[NOUVEAU] ";
            sb.append(status).append(n.getTitre()).append(" - ").append(n.getMessage()).append("\n")
              .append(n.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vos Notifications");
        alert.setHeaderText("Dernières notifications");
        
        javafx.scene.control.TextArea area = new javafx.scene.control.TextArea(sb.toString());
        area.setWrapText(true);
        area.setEditable(false);
        area.setPrefWidth(400);
        area.setPrefHeight(300);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();

        // Mark all as read
        serviceNotification.markAllAsRead(userId);
        updateNotificationCount();
    }

    private void handleGeneratePDF(Article article) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Article_" + article.getIdArticle() + ".pdf");

        File file = fileChooser.showSaveDialog(articlesGrid.getScene().getWindow());
        if (file != null) {
            try {
                // A4 with room for header (top=110) and footer (bottom=65)
                Document document = new Document(PageSize.A4, 55, 55, 110, 65);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

                // ── Page event: draws the branded header + footer on every page ──
                final BaseColor BRAND_BLUE  = new BaseColor(12, 82, 131);
                final BaseColor ACCENT_BLUE = new BaseColor(99, 184, 243);

                writer.setPageEvent(new PdfPageEventHelper() {
                    @Override
                    public void onEndPage(PdfWriter w, Document doc) {
                        PdfContentByte cb = w.getDirectContent();
                        float pw = doc.getPageSize().getWidth();
                        float ph = doc.getPageSize().getHeight();

                        // TOP BANNER
                        cb.saveState();
                        cb.setColorFill(BRAND_BLUE);
                        cb.rectangle(0, ph - 95, pw, 95);
                        cb.fill();
                        cb.setColorFill(ACCENT_BLUE);
                        cb.rectangle(0, ph - 98, pw, 3);
                        cb.fill();
                        cb.restoreState();

                        Font fBrand = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE);
                        Font fSub   = FontFactory.getFont(FontFactory.HELVETICA, 11, ACCENT_BLUE);
                        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                            new Phrase("VITAPLUS", fBrand), 55, ph - 50, 0);
                        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                            new Phrase("Blog Medical", fSub), 55, ph - 73, 0);

                        // White pill badge (right side)
                        cb.saveState();
                        cb.setColorFill(BaseColor.WHITE);
                        cb.roundRectangle(pw - 170, ph - 73, 125, 26, 13);
                        cb.fill();
                        cb.restoreState();
                        Font fBadge = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND_BLUE);
                        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("ARTICLE MEDICAL", fBadge), pw - 107, ph - 64, 0);

                        // BOTTOM FOOTER
                        cb.saveState();
                        cb.setColorFill(BRAND_BLUE);
                        cb.rectangle(0, 0, pw, 52);
                        cb.fill();
                        cb.setColorFill(ACCENT_BLUE);
                        cb.rectangle(0, 50, pw, 3);
                        cb.fill();
                        cb.restoreState();

                        Font fFoot = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.WHITE);
                        String genDate = java.time.LocalDate.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                            new Phrase("VitaPlus — Plateforme Medicale", fFoot), 55, 18, 0);
                        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                            new Phrase("Genere le " + genDate + "   |   Page " + w.getPageNumber(), fFoot),
                            pw - 55, 18, 0);
                    }
                });

                document.open();

                // ── Category tag ──────────────────────────────────────────────
                org.example.entities.Categorie cat = serviceCategorie.getOne(article.getIdCategorie());
                String catName = (cat != null) ? cat.getNom().toUpperCase() : "ARTICLE";
                Font fCat = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND_BLUE);
                Paragraph pCat = new Paragraph("[ " + catName + " ]", fCat);
                pCat.setAlignment(Element.ALIGN_LEFT);
                pCat.setSpacingAfter(8f);
                document.add(pCat);

                // ── Article title ─────────────────────────────────────────────
                Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new BaseColor(15, 32, 60));
                Paragraph pTitle = new Paragraph(article.getTitre(), fTitle);
                pTitle.setAlignment(Element.ALIGN_LEFT);
                pTitle.setLeading(30f);
                pTitle.setSpacingAfter(12f);
                document.add(pTitle);

                // ── Date ──────────────────────────────────────────────────────
                String dateStr = article.getDateCreation() != null
                    ? article.getDateCreation().format(
                        DateTimeFormatter.ofPattern("dd MMMM yyyy 'a' HH:mm", java.util.Locale.FRENCH))
                    : "Date inconnue";
                Font fDate = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, new BaseColor(120, 135, 155));
                Paragraph pDate = new Paragraph("Publie le " + dateStr, fDate);
                pDate.setAlignment(Element.ALIGN_LEFT);
                pDate.setSpacingAfter(18f);
                document.add(pDate);

                // ── Stats table (likes / dislikes / comments) ─────────────────
                int likes    = serviceVote.getLikesCounter(article.getIdArticle());
                int dislikes = serviceVote.getDislikesCounter(article.getIdArticle());
                int comments = serviceCommentaire.getCommentsCounter(article.getIdArticle());

                PdfPTable statsTable = new PdfPTable(3);
                statsTable.setWidthPercentage(80);
                statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                statsTable.setSpacingAfter(22f);
                addStatCell(statsTable, "J'aime : " + likes,
                    new BaseColor(212, 237, 218), new BaseColor(21, 87, 36));
                addStatCell(statsTable, "Pas j'aime : " + dislikes,
                    new BaseColor(248, 215, 218), new BaseColor(114, 28, 36));
                addStatCell(statsTable, "Commentaires : " + comments,
                    new BaseColor(204, 229, 255), new BaseColor(0, 64, 120));
                document.add(statsTable);

                // ── Accent separator ──────────────────────────────────────────
                LineSeparator sep = new LineSeparator(2.5f, 100f, BRAND_BLUE, Element.ALIGN_CENTER, 0);
                document.add(new Chunk(sep));
                document.add(new Paragraph(" "));

                // ── Article content ───────────────────────────────────────────
                Font fContent = FontFactory.getFont(FontFactory.HELVETICA, 12, new BaseColor(40, 50, 65));
                Paragraph pContent = new Paragraph(article.getContenu(), fContent);
                pContent.setAlignment(Element.ALIGN_JUSTIFIED);
                pContent.setSpacingBefore(10f);
                pContent.setLeading(22f);
                document.add(pContent);

                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succes");
                alert.setHeaderText(null);
                alert.setContentText("PDF genere avec succes !");
                alert.showAndWait();

            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors de la generation du PDF");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void addStatCell(PdfPTable table, String text, BaseColor bg, BaseColor fg) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, fg);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(10f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(3f);
        table.addCell(cell);
    }
}
