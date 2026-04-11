package tn.esprit.suivie_nawres;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.suivie_nawres.utils.DatabaseConnection;
import tn.esprit.suivie_nawres.utils.SceneManager;

public class VitaPlusApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        SceneManager.show("/views/ChoixRole.fxml", "VitaPlus Medical");
        stage.setOnCloseRequest(event -> DatabaseConnection.getInstance().closeConnection());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

