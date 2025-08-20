module com.project.dosmart {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;
    opens com.project.dosmart.controllers to javafx.fxml;
    opens com.project.dosmart to javafx.fxml;
    opens com.project.dosmart.models to com.fasterxml.jackson.databind;
    exports com.project.dosmart;
    exports com.project.dosmart.models;
}