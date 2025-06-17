module org.example.java_fx1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;

    opens org.example.java_fx1 to javafx.fxml;
    exports org.example.java_fx1;
}