module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;
    requires java.sql;


    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
}