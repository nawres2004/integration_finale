package tn.esprit.suivie_nawres.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * 🚀 LANCEUR DU CHATBOT MÉDICAL
 * ==============================
 * Classe utilitaire pour ouvrir facilement le chatbot depuis n'importe où dans l'application
 */
public class ChatbotLauncher {

    /**
     * 🤖 OUVRE LA FENÊTRE DU CHATBOT
     * ==============================
     * 
     * Exemple d'utilisation dans un contrôleur :
     * 
     * @FXML
     * private void ouvrirChatbot() {
     *     ChatbotLauncher.ouvrir();
     * }
     */
    public static void ouvrir() {
        try {
            System.out.println("🤖 Ouverture du chatbot médical...");
            
            FXMLLoader loader = new FXMLLoader(
                ChatbotLauncher.class.getResource("/views/Chatbot.fxml")
            );
            
            Scene scene = new Scene(loader.load(), 800, 600);
            scene.getStylesheets().add(
                Objects.requireNonNull(
                    ChatbotLauncher.class.getResource("/css/app.css")
                ).toExternalForm()
            );
            
            Stage stage = new Stage();
            stage.initModality(Modality.NONE); // Permet d'utiliser le chatbot en parallèle
            stage.setTitle("🤖 Assistant Médical VitaPlus");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(600);
            stage.setMinHeight(400);
            
            System.out.println("✅ Chatbot ouvert avec succès");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ouverture du chatbot :");
            e.printStackTrace();
        }
    }
}
