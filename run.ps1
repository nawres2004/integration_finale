# VITA+ - Script de lancement PowerShell
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    VITA+ - LANCEMENT" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Compilation en cours..." -ForegroundColor Green
try {
    javac --module-path "C:\Users\m\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml -d target/classes -cp "target/classes;mysql-connector.jar" src/main/java/tn/esprit/main/JavaFxMain.java src/main/java/tn/esprit/controllers/*.java src/main/java/tn/esprit/ui/*.java src/main/java/tn/esprit/models/*.java src/main/java/tn/esprit/services/*.java src/main/java/tn/esprit/utils/*.java
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERREUR de compilation!" -ForegroundColor Red
        Read-Host "Appuyez sur Entrée pour quitter..."
        exit 1
    }
    
    Write-Host "Copie des ressources..." -ForegroundColor Green
    Copy-Item -Path "src\main\resources\*" -Destination "target\classes\" -Force -Recurse
    
    Write-Host "Lancement de l'application..." -ForegroundColor Green
    Write-Host ""
    
    Set-Location "target\classes"
    java --module-path "C:\Users\m\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml -cp ".;..\..\mysql-connector.jar" tn.esprit.main.JavaFxMain
    
} catch {
    Write-Host "ERREUR: $_" -ForegroundColor Red
    Read-Host "Appuyez sur Entrée pour quitter..."
}

Write-Host ""
Write-Host "Appuyez sur Entrée pour quitter..." -ForegroundColor Cyan
Read-Host
