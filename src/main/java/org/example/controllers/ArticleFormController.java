package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.example.App;
import org.example.entities.Article;
import org.example.entities.Categorie;
import org.example.entities.User;
import org.example.services.ServiceArticle;
import org.example.services.ServiceCategorie;
import org.example.utils.UserSession;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ArticleFormController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private ComboBox<Categorie> categoryCombo;

    @FXML
    private Label titleErrorLabel;

    @FXML
    private Label contentErrorLabel;

    @FXML
    private Label categoryErrorLabel;

    private ServiceArticle serviceArticle = new ServiceArticle();
    private ServiceCategorie serviceCategorie = new ServiceCategorie();

    private static Article articleToEdit = null;

    public static void setArticleToEdit(Article article) {
        articleToEdit = article;
    }

    public static void clearArticleToEdit() {
        articleToEdit = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        if (articleToEdit != null) {
            populateFormForEdit();
        }
    }

    private void populateFormForEdit() {
        titleField.setText(articleToEdit.getTitre());
        contentArea.setText(articleToEdit.getContenu());

        // Pre-select category. Since ComboBox uses objects, we need to find the matching ID in the list
        for (Categorie cat : categoryCombo.getItems()) {
            if (cat.getIdCategorie() == articleToEdit.getIdCategorie()) {
                categoryCombo.getSelectionModel().select(cat);
                break;
            }
        }
    }

    private void loadCategories() {
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser == null) return;

        List<Categorie> categories = serviceCategorie.getAllByRole(currentUser.getIdRole());
        ObservableList<Categorie> observableList = FXCollections.observableArrayList(categories);
        categoryCombo.setItems(observableList);

        // Define how categories look in the dropdown
        categoryCombo.setConverter(new StringConverter<Categorie>() {
            @Override
            public String toString(Categorie categorie) {
                return categorie == null ? null : categorie.getNom();
            }

            @Override
            public Categorie fromString(String s) {
                return null; // Not needed
            }
        });
    }

    @FXML
    void handleSaveArticle(ActionEvent event) {
        // Reset errors
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
        contentErrorLabel.setVisible(false);
        contentErrorLabel.setManaged(false);
        categoryErrorLabel.setVisible(false);
        categoryErrorLabel.setManaged(false);

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        Categorie selectedCategory = categoryCombo.getValue();
        User currentUser = UserSession.getInstance().getUser();

        boolean isValid = true;

        if (title.isEmpty()) {
            showError(titleErrorLabel, "Title is required.");
            isValid = false;
        } else if (title.length() <= 5) {
            showError(titleErrorLabel, "Title must be more than 5 characters.");
            isValid = false;
        }

        if (content.isEmpty()) {
            showError(contentErrorLabel, "Content is required.");
            isValid = false;
        } else if (content.length() <= 5) {
            showError(contentErrorLabel, "Content must be more than 5 characters.");
            isValid = false;
        }

        if (selectedCategory == null) {
            showError(categoryErrorLabel, "Category is required.");
            isValid = false;
        }

        if (!isValid || currentUser == null) return;

        if (articleToEdit == null) {
            // ADD MODE
            Article article = new Article(title, content, currentUser.getIdUtilisateur(), selectedCategory.getIdCategorie());
            serviceArticle.add(article);
            showAlert(Alert.AlertType.INFORMATION, "Success!", "Article added successfully.");
        } else {
            // EDIT MODE
            articleToEdit.setTitre(title);
            articleToEdit.setContenu(content);
            articleToEdit.setIdCategorie(selectedCategory.getIdCategorie());
            serviceArticle.update(articleToEdit);
            showAlert(Alert.AlertType.INFORMATION, "Success!", "Article updated successfully.");
        }

        clearForm();
        try {
            App.setRoot("article_list"); // Navigate back to list on success
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void clearForm() {
        titleField.clear();
        contentArea.clear();
        categoryCombo.getSelectionModel().clearSelection();
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        clearArticleToEdit();
        App.setRoot("article_list");
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) throws IOException {
        clearArticleToEdit();
        App.setRoot("primary");
    }

    @FXML
    void handleLogOutFromForm(ActionEvent event) throws IOException {
        clearArticleToEdit();
        UserSession.cleanUserSession();
        App.setRoot("signin");
    }

    @FXML
    void handleGenerateAI(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Assistant de Rédaction IA");
        dialog.setHeaderText("Générer un article avec Gemini AI ✨");
        dialog.setContentText("De quoi voulez-vous parler ? (ex: Diabète, Nutrition...) :");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String sujet = result.get().trim();
            contentArea.setText("Génération en cours par l'Intelligence Artificielle...\nVeuillez patienter quelques secondes ⏳");
            contentArea.setDisable(true);

            javafx.concurrent.Task<String> aiTask = new javafx.concurrent.Task<String>() {
                @Override
                protected String call() throws Exception {
                    return org.example.services.ServiceGemini.generateArticle(sujet);
                }
            };

            aiTask.setOnSucceeded(e -> {
                contentArea.setText(aiTask.getValue());
                contentArea.setDisable(false);
            });

            aiTask.setOnFailed(e -> {
                contentArea.setText("Erreur lors de la génération.");
                contentArea.setDisable(false);
            });

            new Thread(aiTask).start();
        }
    }
}
