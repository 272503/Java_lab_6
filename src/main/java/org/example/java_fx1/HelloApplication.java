package org.example.java_fx1;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;




public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Ładowanie pliku FXML
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        BorderPane root = fxmlLoader.load();

        // Tworzymy scenę i pokazujemy okno
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Aplikacja Startowa");
        stage.show();
        HelloController.LoggerUtil.log("Uruchomienie aplikacji", "INFO");

        // Log zamknięcia
        stage.setOnCloseRequest(e -> HelloController.LoggerUtil.log("Zamknięcie aplikacji", "INFO"));
    }

    public static void main(String[] args) {
        launch();
    }
}

