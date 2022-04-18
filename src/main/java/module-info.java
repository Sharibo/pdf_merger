module com.gmail.alexejkrawez.pdf_merger {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fluentui;
    requires java.desktop;
    requires org.apache.pdfbox;


    opens com.gmail.alexejkrawez.pdf_merger to javafx.fxml;
    exports com.gmail.alexejkrawez.pdf_merger;
}