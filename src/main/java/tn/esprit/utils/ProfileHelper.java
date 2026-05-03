package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Helper réutilisable pour ouvrir le modal profil depuis n'importe quel dashboard.
 * Usage: ProfileHelper.open(anyNode.getScene().getWindow());
 */
public class ProfileHelper {

    public static void open(javafx.stage.Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ProfileHelper.class.getResource("/profile_modal.fxml"));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(owner);
            modal.initStyle(StageStyle.TRANSPARENT);
            modal.setTitle("Mon Profil");
            modal.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            root.setEffect(new DropShadow(20, 0, 4, Color.rgb(0, 0, 0, 0.25)));

            modal.setScene(scene);
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (Exception e) {
            System.out.println("❌ ProfileHelper.open: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void openChangePassword(javafx.stage.Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                ProfileHelper.class.getResource("/change_password_modal.fxml"));
            Parent root = loader.load();

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(owner);
            modal.initStyle(StageStyle.TRANSPARENT);
            modal.setTitle("Changer le mot de passe");
            modal.setResizable(false);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            root.setEffect(new DropShadow(20, 0, 4, Color.rgb(0, 0, 0, 0.25)));

            modal.setScene(scene);
            modal.centerOnScreen();
            modal.showAndWait();

        } catch (Exception e) {
            System.out.println("❌ ProfileHelper.openChangePassword: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
