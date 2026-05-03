package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.MedicalLocationService;
import tn.esprit.services.SessionService;

import java.net.URL;

public class MedecinLocationController {

    @FXML private WebView mapView;
    @FXML private Label   coordLabel;
    @FXML private Label   latLabel;
    @FXML private Label   lonLabel;
    @FXML private Label   statusLabel;
    @FXML private Button  saveBtn;

    private WebEngine engine;
    private final MedicalLocationService locationService = new MedicalLocationService();
    private Utilisateur currentUser;
    private double selectedLat = 0;
    private double selectedLon = 0;

    @FXML
    public void initialize() {
        currentUser = SessionService.getInstance().getCurrentUser();
        engine = mapView.getEngine();

        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) engine.load(mapUrl.toExternalForm());

        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                injectBridge();
                loadExistingLocation();
            }
        });

        saveBtn.setDisable(true);
    }

    private void injectBridge() {
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("javaConnector", new JavaBridge());
    }

    private void loadExistingLocation() {
        if (currentUser == null) return;
        double lat = currentUser.getLatitude();
        double lon = currentUser.getLongitude();
        if (lat != 0 && lon != 0) {
            selectedLat = lat;
            selectedLon = lon;
            String name = "Dr. " + currentUser.getPrenom() + " " + currentUser.getNom();
            engine.executeScript(String.format("showPatient(%f, %f, '%s')", lat, lon, name));
            updateLabels(lat, lon);
        }
    }

    public class JavaBridge {
        public void onMapClick(String lat, String lon) {
            Platform.runLater(() -> {
                selectedLat = Double.parseDouble(lat);
                selectedLon = Double.parseDouble(lon);
                updateLabels(selectedLat, selectedLon);
                saveBtn.setDisable(false);
                String name = "Dr. " + currentUser.getPrenom() + " " + currentUser.getNom();
                engine.executeScript(String.format("showPatient(%s, %s, '%s')", lat, lon, name));
            });
        }
    }

    private void updateLabels(double lat, double lon) {
        latLabel.setText(String.format("%.6f", lat));
        lonLabel.setText(String.format("%.6f", lon));
        coordLabel.setText(String.format("📍 %.4f, %.4f", lat, lon));
    }

    @FXML
    public void saveLocation() {
        if (selectedLat == 0 && selectedLon == 0) return;
        boolean ok = locationService.saveLocation(
            currentUser.getIdUtilisateur(), selectedLat, selectedLon);
        if (ok) {
            currentUser.setLatitude(selectedLat);
            currentUser.setLongitude(selectedLon);
            SessionService.getInstance().setCurrentUser(currentUser);
            statusLabel.setText("✅ Localisation sauvegardée !");
            statusLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 12px; -fx-font-weight: bold;");
            saveBtn.setDisable(true);
        } else {
            statusLabel.setText("❌ Erreur lors de la sauvegarde.");
            statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
        }
    }
}
