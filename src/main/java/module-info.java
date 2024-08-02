module com.example.paint_brush {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.paint_brush to javafx.fxml;
    exports com.example.paint_brush;
}