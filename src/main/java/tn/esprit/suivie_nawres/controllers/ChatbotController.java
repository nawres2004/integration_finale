package tn.esprit.suivie_nawres.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import tn.esprit.suivie_nawres.services.ChatbotLocalService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 🤖 CONTRÔLEUR DU CHATBOT MÉDICAL
 * =================================
 * Interface de chat moderne avec l'assistant médical IA (LOCAL - 100% GRATUIT)
 */
public class ChatbotController {

    @FXML private VBox chatContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField txtMessage;
    @FXML private Button btnEnvoyer;
    @FXML private Label lblStatus;

    private final ChatbotLocalService chatbotService = new ChatbotLocalService();
    private final DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        // Configuration du ScrollPane pour auto-scroll
        scrollPane.vvalueProperty().bind(chatContainer.heightProperty());
        
        // Vérifier si l'API est configurée
        if (!chatbotService.estConfigure()) {
            afficherMessageSysteme("⚠️ Erreur de configuration");
            btnEnvoyer.setDisable(true);
            return;
        }
        
        // Message de bienvenue
        afficherMessageBot(
            "👋 Bonjour ! Je suis votre assistant médical virtuel VitaPlus.\n\n" +
            "Je peux vous aider avec :\n" +
            "• Des informations sur les symptômes (grippe, maux de tête, etc.)\n" +
            "• Des conseils de prévention (diabète, hypertension, etc.)\n" +
            "• Prendre rendez-vous avec un médecin\n" +
            "• Des questions médicales générales\n\n" +
            "⚠️ Rappel : Mes conseils ne remplacent pas une consultation médicale réelle.\n\n" +
            "Comment puis-je vous aider aujourd'hui ?"
        );
        
        // Permettre l'envoi avec la touche Entrée
        txtMessage.setOnAction(e -> envoyerMessage());
        
        lblStatus.setText("✅ Prêt");
    }

    /**
     * 📤 ENVOIE UN MESSAGE AU CHATBOT
     */
    @FXML
    private void envoyerMessage() {
        String message = txtMessage.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        // Afficher le message de l'utilisateur
        afficherMessageUtilisateur(message);
        txtMessage.clear();
        
        // Désactiver l'envoi pendant le traitement
        btnEnvoyer.setDisable(true);
        txtMessage.setDisable(true);
        lblStatus.setText("🤖 Le chatbot réfléchit...");
        
        // Envoyer la requête dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                String reponse = chatbotService.envoyerMessage(message);
                
                // Afficher la réponse dans le thread JavaFX
                Platform.runLater(() -> {
                    afficherMessageBot(reponse);
                    btnEnvoyer.setDisable(false);
                    txtMessage.setDisable(false);
                    txtMessage.requestFocus();
                    lblStatus.setText("✅ Prêt");
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    afficherMessageErreur(
                        "❌ Erreur de communication avec le chatbot.\n" +
                        "Vérifiez votre connexion internet et votre clé API.\n\n" +
                        "Détails : " + e.getMessage()
                    );
                    btnEnvoyer.setDisable(false);
                    txtMessage.setDisable(false);
                    lblStatus.setText("❌ Erreur");
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 💬 AFFICHE UN MESSAGE DE L'UTILISATEUR
     */
    private void afficherMessageUtilisateur(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 50));
        
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("chat-bubble-user");
        bubble.setMaxWidth(500);
        
        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.getStyleClass().add("chat-text-user");
        textFlow.getChildren().add(text);
        
        Label heure = new Label(LocalTime.now().format(heureFormatter));
        heure.getStyleClass().add("chat-time-user");
        
        bubble.getChildren().addAll(textFlow, heure);
        messageBox.getChildren().add(bubble);
        
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * 🤖 AFFICHE UN MESSAGE DU CHATBOT
     */
    private void afficherMessageBot(String message) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 50, 5, 10));
        
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("chat-bubble-bot");
        bubble.setMaxWidth(500);
        
        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.getStyleClass().add("chat-text-bot");
        textFlow.getChildren().add(text);
        
        Label heure = new Label("🤖 " + LocalTime.now().format(heureFormatter));
        heure.getStyleClass().add("chat-time-bot");
        
        bubble.getChildren().addAll(textFlow, heure);
        messageBox.getChildren().add(bubble);
        
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * ⚠️ AFFICHE UN MESSAGE SYSTÈME
     */
    private void afficherMessageSysteme(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10, 20, 10, 20));
        
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("chat-bubble-system");
        bubble.setMaxWidth(600);
        
        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.getStyleClass().add("chat-text-system");
        textFlow.getChildren().add(text);
        
        bubble.getChildren().add(textFlow);
        messageBox.getChildren().add(bubble);
        
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * ❌ AFFICHE UN MESSAGE D'ERREUR
     */
    private void afficherMessageErreur(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(10, 20, 10, 20));
        
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("chat-bubble-error");
        bubble.setMaxWidth(600);
        
        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.getStyleClass().add("chat-text-error");
        textFlow.getChildren().add(text);
        
        bubble.getChildren().add(textFlow);
        messageBox.getChildren().add(bubble);
        
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * 📜 FAIT DÉFILER VERS LE BAS
     */
    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /**
     * 🗑️ EFFACE LA CONVERSATION
     */
    @FXML
    private void effacerConversation() {
        chatContainer.getChildren().clear();
        initialize();
    }

    /**
     * ✖️ FERME LA FENÊTRE
     */
    @FXML
    private void fermer() {
        javafx.stage.Stage stage = (javafx.stage.Stage) chatContainer.getScene().getWindow();
        stage.close();
    }
}
