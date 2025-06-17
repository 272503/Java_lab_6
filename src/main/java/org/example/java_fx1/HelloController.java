package org.example.java_fx1;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import javafx.geometry.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;


public class HelloController {

    @FXML private ComboBox<String> operationComboBox;
    @FXML private ImageView logoImage;
    @FXML private ImageView originalImageView;
    @FXML private VBox dynamicOptionsBox;
    @FXML private Button executeButton;
    @FXML private Button scaleButton;
    @FXML private Button rotateRightButton;
    @FXML private Button rotateLeftButton;
    @FXML private Button saveResultButton;
    @FXML private Button BatchProcessButton;
    @FXML private HBox additionalResultsBox;

    private File currentImageFile;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);


    public class LoggerUtil {
        private static final String LOG_FILE = "applog.txt";

        public static void log(String message, String level) {
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.write("[" + timestamp + "] [" + level + "] " + message + "\n");
            } catch (IOException e) {
                // Ewentualnie można to pominąć albo wypisać do stderr
                System.err.println("Błąd zapisu logu: " + e.getMessage());
            }
        }
    }

    @FXML
    public void initialize() {
        operationComboBox.getItems().addAll("Negatyw", "Progowanie", "Konturowanie"); // już bez Skalowania i Obrotu
        operationComboBox.setOnAction(e -> updateOptionsPanel());
        executeButton.setDisable(true);
        saveResultButton.setDisable(true);
        loadLogo();

    }

    private void loadLogo() {
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/java_fx1/logo/logo.png")));
        logoImage.setImage(logo);
    }

    @FXML
    private void onLoadImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik obrazu");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JPG", "*.jpg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Sprawdzenie rozszerzenia
                String name = selectedFile.getName().toLowerCase();
                if (!name.endsWith(".jpg")) {
                    showToast("Niedozwolony format pliku");
                    LoggerUtil.log("Użytkownik próbował wczytać niedozwolony format pliku: " + name, "WARN");
                    return;
                }

                Image image = new Image(selectedFile.toURI().toString());
                if (image.isError()) {
                    showToast("Nie udało się załadować pliku");
                    LoggerUtil.log("Błąd przy ładowaniu obrazu: " + name, "ERROR");
                    return;
                }

                currentImageFile = selectedFile;
                originalImageView.setImage(image);
                executeButton.setDisable(false);
                saveResultButton.setDisable(false);
                additionalResultsBox.getChildren().clear();
                rotateLeftButton.setDisable(false);
                rotateRightButton.setDisable(false);
                scaleButton.setDisable(false);
                executeButton.setDisable(false);
                saveResultButton.setDisable(false);
                BatchProcessButton.setDisable(false);
                executeButton.setDisable(false);

                showToast("Pomyślnie załadowano plik");
                LoggerUtil.log("Użytkownik wczytał obraz: " + name, "INFO");

            } catch (Exception e) {
                showToast("Nie udało się załadować pliku");
                LoggerUtil.log("Wyjątek podczas ładowania pliku: " + e.getMessage(), "ERROR");
            }
        }
    }

    @FXML
    private void onExecuteClick() {
        String selected = operationComboBox.getValue();
        if (selected == null || currentImageFile == null) {
            showToast("Wybierz operację i załaduj obraz.");
            return;
        }

        switch (selected) {
            case "Negatyw" -> applyNegative();
            case "Progowanie" -> applyThreshold();
            case "Konturowanie" -> applyContour();
        }
        LoggerUtil.log("Użytkownik uruchomił operację: " + selected, "INFO");


    }

    @FXML
    private void onSaveResultClick() {
        if (currentImageFile == null) return;

        if (!currentImageFile.getName().contains("temp_result") && !currentImageFile.getName().contains("result_")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Brak operacji");
            alert.setHeaderText(null);
            alert.setContentText("Na pliku nie zostały wykonane żadne operacje!");
            alert.showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Zapisz obraz");

        Label nameLabel = new Label("Nazwa pliku:");
        TextField nameField = new TextField();
        nameField.setPromptText("Wprowadź nazwę pliku (3-100 znaków)");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        VBox vbox = new VBox(10, nameLabel, nameField, errorLabel);
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setMinHeight(200);

        ButtonType saveButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Pobierz przycisk i nadpisz jego akcję
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String fileName = nameField.getText().trim();

            // Walidacja
            if (fileName.length() < 3) {
                errorLabel.setText("Wpisz co najmniej 3 znaki");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume(); // Zatrzymaj zamykanie
                return;
            }

            if (fileName.length() > 100) {
                errorLabel.setText("Nazwa pliku jest za długa (max 100 znaków)");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
                return;
            }

            String picturesPath = System.getProperty("user.home") + File.separator + "Pictures";
            File outputFile = new File(picturesPath + File.separator + fileName + ".jpg");

            if (outputFile.exists()) {
                errorLabel.setText("Plik już istnieje – wybierz inną nazwę");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                event.consume();
                return;
            }

            try {
                BufferedImage buffered = ImageIO.read(currentImageFile);
                ImageIO.write(buffered, "jpg", outputFile);
                showToast("Zapisano obraz w pliku " + fileName + ".jpg");
                LoggerUtil.log("Zapisano obraz: " + outputFile.getAbsolutePath(), "INFO");
            } catch (IOException e) {
                showToast("Nie udało się zapisać pliku " + fileName + ".jpg");
                LoggerUtil.log("Błąd zapisu obrazu: " + fileName + ".jpg", "ERROR");
            }
        });

        dialog.showAndWait();

        // Czyszczenie po zamknięciu
        nameField.clear();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }



    @FXML
    private void onBatchProcessClick() {
        if (currentImageFile == null) {
            showToast("Wczytaj obraz najpierw.");
            return;
        }

        additionalResultsBox.getChildren().clear();
        LoggerUtil.log("Rozpoczęto przetwarzanie zbiorcze", "INFO");

        executor.submit(() -> applyBatchProcess("Negatyw"));
        executor.submit(() -> applyBatchProcess("Progowanie"));
        executor.submit(() -> applyBatchProcess("Konturowanie"));
    }


    private void applyBatchProcess(String operationType) {
        try {
            BufferedImage img = ImageIO.read(currentImageFile);
            BufferedImage result = switch (operationType) {
                case "Negatyw" -> negative(img);
                case "Progowanie" -> threshold(img, 128);
                case "Konturowanie" -> contour(img);
                default -> img;
            };

            if (result != null) {
                File tmp = new File("result_" + operationType + ".png");
                ImageIO.write(result, "png", tmp);
                Image fx = SwingFXUtils.toFXImage(result, null);

                ImageView view = new ImageView(fx);
                view.setPreserveRatio(true);
                view.setFitWidth(200);

                Label label = new Label(operationType);
                VBox box = new VBox(5, label, view);
                Platform.runLater(() -> additionalResultsBox.getChildren().add(box));
                LoggerUtil.log("Operacja zbiorcza zakończona: " + operationType, "INFO");

            }
        } catch (IOException e) {
            showToast("Błąd przetwarzania " + operationType);
            LoggerUtil.log("Błąd przetwarzania zbiorczego: " + operationType, "ERROR");

        }
    }

    @FXML
    private void onScaleImageClick() {
        if (currentImageFile == null) {
            showToast("Najpierw wczytaj obraz.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Skalowanie obrazu");

        // Pola
        Label widthLabel = new Label("Szerokość:");
        TextField widthField = new TextField();
        widthField.setPromptText("np. 800");

        Label heightLabel = new Label("Wysokość:");
        TextField heightField = new TextField();
        heightField.setPromptText("np. 600");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(widthLabel, 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(heightLabel, 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Przycisk przywróć oryginalne wymiary (opcjonalnie)
        ButtonType resetBtn = new ButtonType("Przywróć oryginał", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().add(resetBtn);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    if (width <= 0 || width > 3000 || height <= 0 || height > 3000)
                        throw new NumberFormatException();
                    BufferedImage img = ImageIO.read(currentImageFile);
                    Image scaledImage = SwingFXUtils.toFXImage(img, null);
                    originalImageView.setFitWidth(width);
                    originalImageView.setFitHeight(height);
                    originalImageView.setImage(scaledImage);
                    showToast("Skalowanie zakończone.");
                    LoggerUtil.log("Użytkownik przeskalował obraz do: " + width + "x" + height, "INFO");
                } catch (NumberFormatException | IOException e) {
                    showToast("Nieprawidłowe dane lub błąd przetwarzania.");
                    LoggerUtil.log("Błąd skalowania obrazu", "ERROR");
                }
            } else if (button == resetBtn) {
                originalImageView.setFitWidth(350);
                originalImageView.setFitHeight(0); // przywróć automatyczne dopasowanie
                originalImageView.setImage(new Image(currentImageFile.toURI().toString()));
                showToast("Przywrócono oryginalne wymiary.");
                LoggerUtil.log("Użytkownik przywrócił oryginalne wymiary obrazu", "INFO");
            }
            return null;
        });

        dialog.showAndWait();
    }





    private BufferedImage negative(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = img.getRGB(x, y);
                Color color = new Color(rgba, true);
                int red = 255 - color.getRed();
                int green = 255 - color.getGreen();
                int blue = 255 - color.getBlue();
                result.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
        return result;
    }

    private BufferedImage threshold(BufferedImage img, int threshold) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(img.getRGB(x, y));
                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                int newColor = (gray < threshold) ? 0 : 255;  // Binary thresholding
                result.setRGB(x, y, new Color(newColor, newColor, newColor).getRGB());
            }
        }
        return result;
    }

    private BufferedImage contour(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, img.getType());

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int rgb = img.getRGB(x, y);
                int rgbRight = img.getRGB(x + 1, y);
                int rgbBottom = img.getRGB(x, y + 1);
                int diff = Math.abs(rgb - rgbRight) + Math.abs(rgb - rgbBottom);
                int edgeColor = (diff > 50) ? 0 : 255;  // Detect edge with simple difference
                result.setRGB(x, y, new Color(edgeColor, edgeColor, edgeColor).getRGB());
            }
        }
        return result;
    }

    private void applyNegative() {
        try {
            BufferedImage img = ImageIO.read(currentImageFile);
            BufferedImage result = negative(img);
            updateImage(result);
            showToast("Operacja negatywu zakończona.");
            LoggerUtil.log("Operacja negatywu zakończona", "INFO");

        } catch (IOException e) {
            showToast("Błąd przy wykonaniu operacji negatywu.");
            LoggerUtil.log("Błąd operacji negatywu", "ERROR");
        }

    }

    private void applyThreshold() {
        try {
            // Sprawdź, czy dynamicOptionsBox zawiera wystarczającą liczbę elementów
            if (dynamicOptionsBox.getChildren().size() > 1) {
                TextField field = (TextField) dynamicOptionsBox.getChildren().get(1); // Get threshold value
                int threshold = Integer.parseInt(field.getText());
                if (threshold < 0 || threshold > 255) throw new NumberFormatException();

                BufferedImage img = ImageIO.read(currentImageFile);
                BufferedImage result = threshold(img, threshold);
                updateImage(result);
                showToast("Operacja progowania zakończona.");
                LoggerUtil.log("Operacja progowania zakończona", "INFO");

            } else {
                showToast("Nie podano wartości progu.");
            }
        } catch (NumberFormatException e) {
            showToast("Nieprawidłowa wartość progu. Wprowadź liczbę między 0 a 255.");
            LoggerUtil.log("Nieprawidłowa wartość progu: ", "WARNING");
        } catch (IOException e) {
            showToast("Błąd przy wykonaniu operacji progowania.");
            LoggerUtil.log("Błąd operacji progowania", "ERROR");
        }
    }



    private void applyContour() {
        try {
            BufferedImage img = ImageIO.read(currentImageFile);
            BufferedImage result = contour(img);
            updateImage(result);
            showToast("Operacja konturowania zakończona.");
            LoggerUtil.log("Operacja konturowania zakończona", "INFO");

        } catch (IOException e) {
            showToast("Błąd przy wykonaniu operacji konturowania.");
            LoggerUtil.log("Błąd operacji konturowania", "ERROR");

        }
    }

    private BufferedImage rotateImage(BufferedImage img, int angle) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage rotated = new BufferedImage(width, height, img.getType());
        Graphics2D g2d = rotated.createGraphics();
        g2d.rotate(Math.toRadians(angle), width / 2, height / 2);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return rotated;
    }

    @FXML
    private void onRotateLeftClick() {
        rotateByAngle(-90);
    }

    @FXML
    private void onRotateRightClick() {
        rotateByAngle(90);
    }

    private void rotateByAngle(int angle) {
        if (currentImageFile == null) {
            showToast("Najpierw wczytaj obraz.");
            return;
        }
        try {
            BufferedImage img = ImageIO.read(currentImageFile);
            BufferedImage result = rotateImage(img, angle);
            updateImage(result);
            showToast("Obraz obrócono o " + angle + "°.");
            LoggerUtil.log("Obraz obrócono o " + angle + "°", "INFO");

        } catch (IOException e) {
            showToast("Błąd przy obrocie obrazu.");
            LoggerUtil.log("Błąd obrotu obrazu", "ERROR");

        }
    }


    private void updateImage(BufferedImage img) throws IOException {
        File temp = new File("temp_result.png");
        ImageIO.write(img, "png", temp);
        currentImageFile = temp;
        originalImageView.setImage(SwingFXUtils.toFXImage(img, null));
    }


    private void updateOptionsPanel() {
        dynamicOptionsBox.getChildren().clear();  // Wyczyść poprzednie elementy
        String selected = operationComboBox.getValue();

        if (selected.equals("Progowanie")) {
            Label label = new Label("Wartość progu (0-255):");
            TextField field = new TextField();
            field.setPromptText("0-255");
            dynamicOptionsBox.getChildren().addAll(label, field);
        }
    }


    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Komunikat");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
