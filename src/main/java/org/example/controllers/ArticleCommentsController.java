package org.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.entities.Article;
import org.example.entities.Commentaire;
import org.example.entities.User;
import org.example.services.ServiceCommentaire;
import org.example.services.ServiceUser;
import org.example.utils.UserSession;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ArticleCommentsController implements Initializable {

    @FXML private Label articleTitleLabel;
    @FXML private Label fullArticleTitle;
    @FXML private Label fullArticleDate;
    @FXML private Label fullArticleContent;
    @FXML private Button translateBtn;
    @FXML private javafx.scene.image.ImageView qrCodeImage;
    @FXML private VBox commentsContainer;
    @FXML private VBox inputBar;
    @FXML private TextArea newCommentField;
    @FXML private Label commentErrorLabel;

    private static Article currentArticle;
    private ServiceCommentaire serviceCommentaire = new ServiceCommentaire();
    private ServiceUser serviceUser = new ServiceUser();
    private boolean isTranslated = false;
    private String translatedText = null;

    public static void setArticle(Article article) {
        currentArticle = article;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (currentArticle != null) {
            articleTitleLabel.setText("💬  Comments");
            
            if (fullArticleTitle != null) {
                fullArticleTitle.setText(currentArticle.getTitre());
            }
            if (fullArticleDate != null) {
                String dateStr = (currentArticle.getDateCreation() != null) 
                        ? currentArticle.getDateCreation().format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm")) 
                        : "Date inconnue";
                fullArticleDate.setText("📅 Publié le : " + dateStr);
            }
            if (fullArticleContent != null) {
                fullArticleContent.setText(currentArticle.getContenu());
            }

            if (qrCodeImage != null) {
                try {
                    String qrData = java.net.URLEncoder.encode(currentArticle.getTitre(), "UTF-8");
                    String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + qrData;
                    javafx.scene.image.Image image = new javafx.scene.image.Image(qrApiUrl, true);
                    qrCodeImage.setImage(image);
                } catch (Exception e) {
                    System.out.println("Error loading QR Code: " + e.getMessage());
                }
            }
        }

        loadComments();
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();

        if (currentArticle == null) return;

        List<Commentaire> comments = serviceCommentaire.getByArticle(currentArticle.getIdArticle());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        User currentUser = UserSession.getInstance().getUser();

        if (comments.isEmpty()) {
            Label empty = new Label("No comments yet. Be the first to share your thoughts!");
            empty.setStyle("-fx-text-fill: #63B8F3; -fx-font-size: 14px; -fx-padding: 30;");
            commentsContainer.getChildren().add(empty);
            return;
        }

        for (Commentaire comment : comments) {
            commentsContainer.getChildren().add(buildCommentCard(comment, fmt, currentUser));
        }
    }

    private VBox buildCommentCard(Commentaire comment, DateTimeFormatter fmt, User currentUser) {
        boolean isOwn = (comment.getIdUtilisateur() == currentUser.getIdUtilisateur());
        boolean isAdmin = (currentUser.getIdRole() == 1);

        VBox card = new VBox(6);
        card.getStyleClass().add(isOwn ? "comment-card-own" : "comment-card");

        // --- Header: author + date ---
        User author = serviceUser.getUserById(comment.getIdUtilisateur());
        String authorName = (author != null) ? author.getNom() + " " + author.getPrenom() : "Unknown";

        Label authorLabel = new Label("👤 " + authorName);
        authorLabel.getStyleClass().add("comment-author");

        String dateStr = (comment.getDateCreation() != null) ? comment.getDateCreation().format(fmt) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("comment-date");

        if (isOwn) {
            authorLabel.setText("👤 " + authorName + " (You)");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox header = new HBox(8, authorLabel, spacer, dateLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // --- Content ---
        Label contentLabel = new Label(comment.getContenu());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        card.getChildren().addAll(header, contentLabel);

        // --- Actions (conditional) ---
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        if (isOwn) {
            Button editBtn = new Button("✏ Edit");
            editBtn.getStyleClass().add("btn-warning");
            editBtn.setOnAction(e -> handleEditComment(comment, card, contentLabel));
            actions.getChildren().add(editBtn);

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> handleDeleteComment(comment));
            actions.getChildren().add(deleteBtn);
        } else if (isAdmin) {
            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> handleDeleteComment(comment));
            actions.getChildren().add(deleteBtn);
        }

        if (!actions.getChildren().isEmpty()) {
            card.getChildren().add(actions);
        }

        return card;
    }

    private void handleEditComment(Commentaire comment, VBox card, Label contentLabel) {
        // Replace content label with a text field for inline editing
        TextField editField = new TextField(comment.getContenu());
        editField.getStyleClass().add("comment-edit-field");

        Button saveBtn = new Button("✔ Save");
        saveBtn.getStyleClass().add("btn-save-edit");

        Button cancelBtn = new Button("✖ Cancel");
        cancelBtn.getStyleClass().add("btn-cancel-edit");

        HBox editActions = new HBox(8, saveBtn, cancelBtn);
        editActions.setAlignment(Pos.CENTER_LEFT);

        // Remove old content and actions, add edit UI
        int contentIndex = card.getChildren().indexOf(contentLabel);
        card.getChildren().remove(contentLabel);
        // Remove old actions row if present
        if (card.getChildren().size() > contentIndex) {
            card.getChildren().remove(contentIndex);
        }
        card.getChildren().add(contentIndex, editField);
        card.getChildren().add(contentIndex + 1, editActions);

        saveBtn.setOnAction(e -> {
            String newText = editField.getText().trim();
            if (newText.isEmpty() || newText.length() <= 3) {
                editField.setStyle(editField.getStyle() + "-fx-border-color: #e74c3c;");
                return;
            }
            comment.setContenu(newText);
            serviceCommentaire.update(comment);
            loadComments(); // Refresh
        });

        cancelBtn.setOnAction(e -> loadComments());
    }

    private void handleDeleteComment(Commentaire comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Comment");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this comment?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceCommentaire.delete(comment.getIdCommentaire());
            loadComments();
        }
    }

    @FXML
    void handlePostComment(ActionEvent event) {
        commentErrorLabel.setVisible(false);
        commentErrorLabel.setManaged(false);

        String text = newCommentField.getText().trim();

        if (text.isEmpty()) {
            showError("Comment cannot be empty.");
            return;
        }
        if (text.length() <= 3) {
            showError("Comment must be more than 3 characters.");
            return;
        }

        User user = UserSession.getInstance().getUser();
        Commentaire commentaire = new Commentaire(text, currentArticle.getIdArticle(), user.getIdUtilisateur());
        serviceCommentaire.add(commentaire);

        if (user.getIdUtilisateur() != currentArticle.getIdUtilisateur()) {
            org.example.services.ServiceNotification serviceNotif = new org.example.services.ServiceNotification();
            org.example.entities.Notification notif = new org.example.entities.Notification(
                "Nouveau Commentaire",
                "Vous avez un nouveau commentaire sur : " + currentArticle.getTitre(),
                currentArticle.getIdUtilisateur(),
                "commentaire",
                null
            );
            serviceNotif.add(notif);
        }

        newCommentField.clear();
        loadComments();
    }

    private void showError(String msg) {
        commentErrorLabel.setText(msg);
        commentErrorLabel.setVisible(true);
        commentErrorLabel.setManaged(true);
    }

    @FXML
    void handleBackToArticles(ActionEvent event) throws IOException {
        App.setRoot("article_list");
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) throws IOException {
        App.setRoot("primary");
    }

    @FXML
    void handleLogOut(ActionEvent event) throws IOException {
        UserSession.cleanUserSession();
        App.setRoot("signin");
    }

    @FXML
    void handleTranslate(ActionEvent event) {
        if (currentArticle == null || translateBtn == null) return;
        
        if (isTranslated) {
            // Revert to French
            fullArticleContent.setText(currentArticle.getContenu());
            translateBtn.setText("🌍 Traduire en Anglais");
            isTranslated = false;
        } else {
            // Translate to English
            if (translatedText != null) {
                // Use cached translation
                fullArticleContent.setText(translatedText);
                translateBtn.setText("🇫🇷 Voir en Français");
                isTranslated = true;
            } else {
                // Call API
                translateBtn.setText("⏳ Traduction...");
                translateBtn.setDisable(true);
                
                javafx.concurrent.Task<String> translationTask = new javafx.concurrent.Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        return org.example.services.ServiceTranslation.translateToEnglish(currentArticle.getContenu());
                    }
                };

                translationTask.setOnSucceeded(e -> {
                    translatedText = translationTask.getValue();
                    if (fullArticleContent != null) {
                        fullArticleContent.setText(translatedText);
                    }
                    translateBtn.setText("🇫🇷 Voir en Français");
                    translateBtn.setDisable(false);
                    isTranslated = true;
                });

                translationTask.setOnFailed(e -> {
                    translateBtn.setText("🌍 Traduire en Anglais");
                    translateBtn.setDisable(false);
                });

                new Thread(translationTask).start();
            }
        }
    }
}
