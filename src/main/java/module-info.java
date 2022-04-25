module edu.bsuir.ootpisp_lab3_javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens edu.bsuir.ootpisp_lab3_javafx to javafx.fxml;
    exports edu.bsuir.ootpisp_lab3_javafx;
}