@echo off
echo ========================================
echo    VITA+ - LANCEMENT
echo ========================================
echo.

echo Compilation en cours...
javac --module-path "C:\Users\m\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml -d target/classes -cp "target/classes;mysql-connector.jar" src/main/java/tn/esprit/main/JavaFxMain.java src/main/java/tn/esprit/controllers/*.java src/main/java/tn/esprit/models/*.java src/main/java/tn/esprit/services/*.java src/main/java/tn/esprit/utils/*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERREUR de compilation!
    pause
    exit /b
)

echo Copie des ressources...
copy src\main\resources\* target\classes\ >nul

echo Lancement de l'application...
echo.
cd target\classes
java --module-path "C:\Users\m\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;C:\Users\m\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar" --add-modules javafx.controls,javafx.fxml -cp ".;..\..\mysql-connector.jar" tn.esprit.main.JavaFxMain

pause
