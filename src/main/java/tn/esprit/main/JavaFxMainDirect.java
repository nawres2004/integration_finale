package tn.esprit.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFxMainDirect extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        // Créer le layout directement en JavaFX
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #4CAF50; -fx-padding: 20px; -fx-alignment: center;");
        
        // Titre
        Label title = new Label("🎯 DASHBOARD DIRECT");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 20px;");
        
        // Message
        Label message = new Label("Design moderne avec fond vert - Le design fonctionne !");
        message.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-padding: 20px; -fx-background-color: #2196F3; -fx-background-radius: 10px;");
        
        // Bouton de test
        Button testBtn = new Button("Test Design");
        testBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        // Action du bouton
        testBtn.setOnAction(e -> {
            System.out.println("✅ Bouton cliqué - Le design fonctionne !");
        });
        
        root.getChildren().addAll(title, message, testBtn);
        
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("VitaPlus - Dashboard Design Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
