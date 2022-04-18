package com.gmail.alexejkrawez.pdf_merger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Controller {

    @FXML
    private Button addFiles;
    @FXML
    private Button moveSelectedUp;
    @FXML
    private Button moveSelectedDown;
    @FXML
    private Button removeSelected;
    @FXML
    private Button removeAll;
    @FXML
    private Button mergeFiles;
    @FXML
    private ListView<String> listView;
    @FXML
    private Label footerLabel;
    @FXML
    private Button themeChanger;
    @FXML
    private FontIcon themeChangerIcon;


    @FXML
    public void initialize() {

        if (PDFMerger.getTheme().equals("css/dark-style.css")) {
            themeChangerIcon.setIconLiteral("fltfmz-weather-sunny-20");
        }

        addListViewFooterLabelInfoEventListener();
        addListDragAndDropEventListener();
    }

    @FXML
    protected void buttonAddFiles() {

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF-files");
        fileChooser.setInitialDirectory(new File(PDFMerger.getLastOpenedDirectory()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );


        Stage stage = (Stage) addFiles.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(stage);

        showFiles(files);

        try {
            PDFMerger.setLastOpenedDirectory(files.get(0).getParent());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showFiles(List<File> files) {
        if (files != null && !files.isEmpty()) {

            int size = 0;
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith("pdf")) {
                    listView.getItems().add(file.toString());
                    size++;
                }
            }

            if (size > 0) {
                if (size == 1) {
                    footerLabel.setText("Added 1 file");
                } else {
                    footerLabel.setText("Added " + size + " files");
                }
            }

            if (listView.getItems().size() == 1) {
                removeSelected.setDisable(false);
                removeAll.setDisable(false);
            } else if (listView.getItems().size() > 1) {
                setButtonsActive();
            }

        }

    }


    @FXML
    protected void buttonMoveSelectedUp() {

        ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();

        if (selectedIndices.size() == 1) {
            int selectedIndex = selectedIndices.get(0);

            if (selectedIndex == 0) {
                return;
            } else {
                Collections.swap(listView.getItems(), selectedIndex - 1, selectedIndex);
                listView.getSelectionModel().clearAndSelect(selectedIndex - 1);
            }

        } else if (selectedIndices.size() == 0) {
            return;
        } else {
            int firstSelectedIndex = selectedIndices.get(0);
            int lastSelectedIndex = selectedIndices.get(selectedIndices.size() - 1);

            if (firstSelectedIndex == 0) {
                return;
            } else {
                Collections.rotate(listView.getItems().subList(firstSelectedIndex - 1, lastSelectedIndex + 1), -1);

                listView.getSelectionModel().clearSelection();
                listView.getSelectionModel().selectRange(firstSelectedIndex - 1, lastSelectedIndex);
            }
        }

        footerLabel.setText("Moved up");
    }

    @FXML
    protected void buttonMoveSelectedDown() {
        ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();

        if (selectedIndices.size() == 1) {
            int selectedIndex = selectedIndices.get(0);

            if (selectedIndex == listView.getItems().size() - 1) {
                return;
            } else {
                Collections.swap(listView.getItems(), selectedIndex + 1, selectedIndex);
                listView.getSelectionModel().clearAndSelect(selectedIndex + 1);
            }

        } else if (selectedIndices.size() == 0) {
            return;
        } else {
            int firstSelectedIndex = selectedIndices.get(0);
            int lastSelectedIndex = selectedIndices.get(selectedIndices.size() - 1);

            if (lastSelectedIndex == listView.getItems().size() - 1) {
                return;
            } else {
                Collections.rotate(listView.getItems().subList(firstSelectedIndex, lastSelectedIndex + 2), 1);

                listView.getSelectionModel().clearSelection();
                listView.getSelectionModel().selectRange(firstSelectedIndex + 1, lastSelectedIndex + 2);
            }
        }

        footerLabel.setText("Moved down");
    }

    @FXML
    protected void buttonRemoveSelected() {
        ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();

        if (listView.getSelectionModel().getSelectedIndices().size() == 0) {
            return;
        } else {
            listView.getItems().remove(selectedIndices.get(0), selectedIndices.get(selectedIndices.size() - 1) + 1);

            footerLabel.setText("Selected files removed");
        }


        if (listView.getItems().size() == 1) {
            moveSelectedUp.setDisable(true);
            moveSelectedDown.setDisable(true);
            mergeFiles.setDisable(true);
        } else if (listView.getItems().size() == 0) {
            setButtonsInactive();
        }
    }

    @FXML
    protected void buttonRemoveAll() {
        listView.getItems().clear();
        setButtonsInactive();
        footerLabel.setText("Cleared");
    }

    @FXML
    protected void buttonMergeFiles() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select directory for save");
        fileChooser.setInitialDirectory(new File(PDFMerger.getLastOpenedDirectory()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        Stage stage = (Stage) addFiles.getScene().getWindow();
        File savedFile = fileChooser.showSaveDialog(stage);

        try {
            PDFMerger.setLastOpenedDirectory(savedFile.getParent());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        boolean isMerged = mergePDFs(savedFile.getAbsolutePath());

        if (isMerged) {
            footerLabel.setText("Files merged as " + savedFile.getName());

            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(savedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            footerLabel.setText("Files not bound: attempt to save to one of the bound files");
        }

    }

    private boolean mergePDFs(String pathForSave) {

        PDFMergerUtility mergerUtility = new PDFMergerUtility();

        PDDocumentInformation documentInformation = new PDDocumentInformation();
        documentInformation.setTitle("PDFMerger document");
        documentInformation.setSubject("Merging PDF documents with Apache PDF Box by PDFMerger app");

        try {
            for (String path : listView.getItems()) {
                if (pathForSave.equalsIgnoreCase(path)) {
                    return false;
                } else {
                    mergerUtility.addSource(path);
                    mergerUtility.setDestinationFileName(pathForSave);
                    mergerUtility.setDestinationDocumentInformation(documentInformation);
                    mergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    @FXML
    protected void buttonThemeChanger() {
        Scene scene = themeChanger.getScene();

        if (PDFMerger.getTheme().equals("css/light-style.css")) {
            scene.getStylesheets().set(0, getClass().getResource("css/dark-style.css").toExternalForm());
            themeChangerIcon.setIconLiteral("fltfmz-weather-sunny-20");
            PDFMerger.setTheme("css/dark-style.css");
            footerLabel.setText("Dark theme enabled");
        } else {
            scene.getStylesheets().set(0, getClass().getResource("css/light-style.css").toExternalForm());
            themeChangerIcon.setIconLiteral("fltfmz-weather-moon-20");
            PDFMerger.setTheme("css/light-style.css");
            footerLabel.setText("Light theme enabled");
        }
    }


    private boolean isListViewReady() {
        if (listView.getItems().size() <= 1) {
            return false;
        } else {
            return true;
        }
    }

    private void setButtonsActive() {
        moveSelectedUp.setDisable(false);
        moveSelectedDown.setDisable(false);
        removeSelected.setDisable(false);
        removeAll.setDisable(false);
        if (isListViewReady()) {
            mergeFiles.setDisable(false);
        }
    }

    private void setButtonsInactive() {
        moveSelectedUp.setDisable(true);
        moveSelectedDown.setDisable(true);
        removeSelected.setDisable(true);
        removeAll.setDisable(true);
        mergeFiles.setDisable(true);
    }


    private void addListViewFooterLabelInfoEventListener() {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                footerLabel.setText("Selected: " + listView.getSelectionModel().getSelectedIndices().size());
            }
        });
    }

    private void addListDragAndDropEventListener() {
        listView.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });

        listView.setOnDragDropped((DragEvent event) -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasFiles()) {
                showFiles(dragboard.getFiles());

                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }

            event.consume();
        });
    }

}