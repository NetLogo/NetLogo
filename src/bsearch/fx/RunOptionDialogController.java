package bsearch.fx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.nlogo.api.MersenneTwisterFast;

import bsearch.app.BehaviorSearch;
import bsearch.app.BehaviorSearch.RunOptions;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class RunOptionDialogController {
    @FXML
    AnchorPane anchorPane;
    @FXML
    Button browseButton;
    @FXML
    TextField outputPathTextField;
    @FXML
    Spinner<Integer> searchesNumSpinner;
    @FXML
    Spinner<Integer> startingSearchIDSpinner;
    @FXML
    Spinner<Integer> iniRanSeedSpinner;
    @FXML
    Button newRanSeedButton;
    @FXML
    Spinner<Integer> threadNumSpinner;
    @FXML
    CheckBox briefOutputCheckBox;
    @FXML
    Button startSearchButton;
    @FXML
    Button cancelButton;

    private MainController main;

    private RunOptions runOptions;

    public void ini(RunOptions runOptions, MainController main) {
        this.runOptions = runOptions;
        outputPathTextField.setText(runOptions.outputStem);

        searchesNumSpinner
                .setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, runOptions.numSearches));
        startingSearchIDSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, runOptions.firstSearchNumber));
        threadNumSpinner
                .setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1024, runOptions.numThreads));
        iniRanSeedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,
                Integer.MAX_VALUE, runOptions.randomSeed.intValue()));
        briefOutputCheckBox.setSelected(runOptions.briefOutput);
        searchesNumSpinner.getValueFactory()
                .setConverter(new myStringConverter<>(searchesNumSpinner.getValueFactory().getConverter()));
        startingSearchIDSpinner.getValueFactory()
                .setConverter(new myStringConverter<>(startingSearchIDSpinner.getValueFactory().getConverter()));
        threadNumSpinner.getValueFactory()
                .setConverter(new myStringConverter<>(threadNumSpinner.getValueFactory().getConverter()));
        iniRanSeedSpinner.getValueFactory()
                .setConverter(new myStringConverter<>(iniRanSeedSpinner.getValueFactory().getConverter()));

        this.main = main;
        //http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user

        searchesNumSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (nv) return;
            commitEditorText(searchesNumSpinner);
        });
        startingSearchIDSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (nv) return;
            commitEditorText(startingSearchIDSpinner);
        });
        threadNumSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (nv) return;
            commitEditorText(threadNumSpinner);
        });
        iniRanSeedSpinner.focusedProperty().addListener((s, ov, nv) -> {
            if (nv) return;
            commitEditorText(iniRanSeedSpinner);
        });

    }

    private Window getMainWindow() {
        if (anchorPane != null && anchorPane.getScene() != null) {
            return anchorPane.getScene().getWindow();
        } else {
            return null;
        }
    }

    public void browseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File parentFolder = new File(outputPathTextField.getText()).getParentFile();
        if (parentFolder != null && parentFolder.exists()) {
            fileChooser.setInitialDirectory(parentFolder);
            // file type???
            fileChooser.setInitialFileName("MySearchOutput");

        }

        File selectedFile = fileChooser.showSaveDialog(getMainWindow());
        if (selectedFile != null) {
            outputPathTextField.setText(selectedFile.getPath());
        }

    }

    public void newRanSeed(ActionEvent event) {
        iniRanSeedSpinner.getValueFactory().setValue(new MersenneTwisterFast().nextInt());

    }

    public void updateOptions(ActionEvent event) {
        runOptions.outputStem = outputPathTextField.getText();
        runOptions.numSearches = (Integer) searchesNumSpinner.getValue();
        runOptions.firstSearchNumber = (Integer) startingSearchIDSpinner.getValue();
        runOptions.numThreads = (Integer) threadNumSpinner.getValue();
        runOptions.randomSeed = (Integer) iniRanSeedSpinner.getValue();
        runOptions.briefOutput = briefOutputCheckBox.isSelected();

        // trying to open progress from here

        Node source = (Node) event.getSource();
        Stage thisStage = (Stage) source.getScene().getWindow();

        thisStage.close();
        main.displayProgressDialog();
    }

    public void cancel(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    //http://stackoverflow.com/questions/32340476/manually-typing-in-text-in-javafx-spinner-is-not-updating-the-value-unless-user
    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }

    class myStringConverter<Interger> extends StringConverter<Integer> {
        StringConverter<Integer> original;

        public myStringConverter(StringConverter<Integer> original) {
            this.original = original;
        }

        @Override
        public Integer fromString(String value) {
            try {
                return original.fromString(value);
            } catch (NumberFormatException nfe) {
                MainController.handleError("Bad integer: " + value);

                return 0;
            }
        }

        @Override
        public String toString(Integer value) {
            return original.toString(value);
        }
    };

}
