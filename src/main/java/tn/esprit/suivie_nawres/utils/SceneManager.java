package tn.esprit.suivie_nawres.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public final class SceneManager {
    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void show(String fxmlPath, String title) {
        try {
            Parent root = loadView(fxmlPath);
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(Objects.requireNonNull(SceneManager.class.getResource("/css/app.css")).toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de charger la vue : " + fxmlPath, exception);
        }
    }

    public static void replaceContent(Pane container, String fxmlPath) {
        try {
            Parent content = loadView(fxmlPath);
            container.getChildren().setAll(content);
            if (content instanceof javafx.scene.layout.Region region) {
                region.prefWidthProperty().bind(container.widthProperty());
                region.prefHeightProperty().bind(container.heightProperty());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de charger le contenu : " + fxmlPath, exception);
        }
    }

    public static Parent loadView(String fxmlPath) throws IOException {
        URL url = Objects.requireNonNull(SceneManager.class.getResource(fxmlPath), "FXML introuvable : " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(url);
        return loader.load();
    }
}

