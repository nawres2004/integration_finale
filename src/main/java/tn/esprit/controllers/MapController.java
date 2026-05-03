package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.MedicalLocationService;
import tn.esprit.services.MedicalLocationService.MedecinDistance;
import tn.esprit.services.SessionService;

import java.net.URL;
import java.util.List;

public class MapController {

    @FXML private WebView mapView;
    @FXML private ListView<String> doctorListView;
    @FXML private Label statusLabel;
    @FXML private Label coordLabel;
    @FXML private Button saveLocationBtn;

    private WebEngine engine;
    private final MedicalLocationService locationService = new MedicalLocationService();
    private Utilisateur currentUser;
    private double selectedLat = 0;
    private double selectedLon = 0;
    private List<MedecinDistance> nearestDoctors;

    @FXML
    public void initialize() {
        currentUser = SessionService.getInstance().getCurrentUser();
        engine = mapView.getEngine();

        // Charger le HTML Mapbox
        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) {
            engine.load(mapUrl.toExternalForm());
        }

        // Quand la page est chargée → injecter le bridge Java↔JS
        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                injectJavaBridge();
                loadPatientAndDoctors();
            }
        });

        saveLocationBtn.setDisable(true);
    }

    // ── Bridge Java ↔ JavaScript ────────────────────────────
    private void injectJavaBridge() {
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("javaConnector", new JavaBridge());
    }

    /** Appelé depuis JavaScript quand l'utilisateur clique sur la carte */
    public class JavaBridge {
        public void onMapClick(String lat, String lon) {
            Platform.runLater(() -> {
                selectedLat = Double.parseDouble(lat);
                selectedLon = Double.parseDouble(lon);
                coordLabel.setText(String.format("📍 Sélectionné: %.4f, %.4f", selectedLat, selectedLon));
                saveLocationBtn.setDisable(false);

                // Mettre à jour le marqueur patient
                String name = currentUser.getPrenom() + " " + currentUser.getNom();
                engine.executeScript(String.format(
                    "showPatient(%s, %s, '%s')", lat, lon, name));

                // Recalculer les médecins proches
                refreshDoctorList(selectedLat, selectedLon);
            });
        }
    }

    // ── Charger patient + médecins ──────────────────────────
    private void loadPatientAndDoctors() {
        if (currentUser == null) return;

        double lat = currentUser.getLatitude();
        double lon = currentUser.getLongitude();

        // Si le patient a déjà une localisation
        if (lat != 0 && lon != 0) {
            selectedLat = lat;
            selectedLon = lon;
            String name = currentUser.getPrenom() + " " + currentUser.getNom();
            engine.executeScript(String.format("showPatient(%f, %f, '%s')", lat, lon, name));
            refreshDoctorList(lat, lon);
            coordLabel.setText(String.format("📍 Position: %.4f, %.4f", lat, lon));
        } else {
            statusLabel.setText("Cliquez sur la carte pour définir votre position.");
        }
    }

    private void refreshDoctorList(double patLat, double patLon) {
        nearestDoctors = locationService.getNearestDoctors(patLat, patLon);

        // Effacer les anciens marqueurs médecins
        engine.executeScript("clearDoctors()");

        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < Math.min(nearestDoctors.size(), 10); i++) {
            MedecinDistance md = nearestDoctors.get(i);
            // Ajouter marqueur sur la carte
            engine.executeScript(String.format(
                "addDoctor(%f, %f, '%s %s', '%s', '%s')",
                md.medecin.getLatitude(), md.medecin.getLongitude(),
                md.medecin.getPrenom(), md.medecin.getNom(),
                md.medecin.getSpecialite() != null ? md.medecin.getSpecialite() : "",
                md.getDistanceLabel()
            ));
            items.add((i + 1) + ". " + md.toString());
        }

        doctorListView.setItems(items);
        engine.executeScript("fitAllMarkers()");

        statusLabel.setText(nearestDoctors.size() + " médecin(s) trouvé(s) près de vous.");
    }

    // ── Sauvegarder la position ─────────────────────────────
    @FXML
    public void saveLocation() {
        if (selectedLat == 0 && selectedLon == 0) return;

        boolean ok = locationService.saveLocation(
            currentUser.getIdUtilisateur(), selectedLat, selectedLon);

        if (ok) {
            currentUser.setLatitude(selectedLat);
            currentUser.setLongitude(selectedLon);
            SessionService.getInstance().setCurrentUser(currentUser);
            statusLabel.setText("✅ Position sauvegardée !");
            saveLocationBtn.setDisable(true);
        } else {
            statusLabel.setText("❌ Erreur lors de la sauvegarde.");
        }
    }

    // ── Voir médecin sélectionné sur carte ──────────────────
    @FXML
    public void viewOnMap() {
        int idx = doctorListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || nearestDoctors == null || idx >= nearestDoctors.size()) return;

        MedecinDistance md = nearestDoctors.get(idx);
        engine.executeScript(String.format(
            "map.flyTo({center:[%f,%f], zoom:15, duration:1000})",
            md.medecin.getLongitude(), md.medecin.getLatitude()
        ));
    }
}
