package com.handshape.justneuralnets.ui;

import com.handshape.justneuralnets.datafields.Word2VecDataField;
import com.handshape.justneuralnets.JNNModelSpec;
import com.handshape.justneuralnets.datafields.NormalizedDataField;
import com.handshape.justneuralnets.datafields.EnglishWordStemDataField;
import com.handshape.justneuralnets.datafields.FrenchIncidenceFilteredTextField;
import com.handshape.justneuralnets.JNNModelTrainer;
import com.handshape.justneuralnets.datafields.EnglishIncidenceFilteredTextField;
import com.handshape.justneuralnets.JNNModelEvaluator;
import com.handshape.justneuralnets.JNNTrainingListener;
import com.handshape.justneuralnets.datafields.UnboundedDataField;
import com.handshape.justneuralnets.datafields.FreeTextDataField;
import com.handshape.justneuralnets.datafields.MultiValuedClosedVocabDataField;
import com.handshape.justneuralnets.datafields.BooleanDataField;
import com.handshape.justneuralnets.datafields.DataField;
import com.handshape.justneuralnets.datafields.LabelDataField;
import com.handshape.justneuralnets.datafields.FrenchWordStemDataField;
import com.handshape.justneuralnets.datafields.ClosedVocabDataField;
import com.handshape.justneuralnets.JNNFitnessEvaluator;
import com.handshape.justneuralnets.design.DesignFieldConfig;
import com.handshape.justneuralnets.input.CsvTabularInput;
import com.handshape.justneuralnets.input.ExcelTabularInput;
import com.handshape.justneuralnets.input.ITabularInput;
import com.jfoenix.controls.*;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nd4j.evaluation.classification.Evaluation;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author JoTurner
 */
public class InteractiveDesignUIController {

    private static final int DEFAULT_LAYER_WIDTH = 50;
    private final TreeSet<String> datasourceFields = new TreeSet<>();
    private final JNNModelTrainer trainer = new JNNModelTrainer();
    private final FileChooser datasourceFileChooser = new FileChooser();
    private final FileChooser schemeFileChooser = new FileChooser();
    private final FileChooser modelFileChooser = new FileChooser();
    private JNNModelSpec model = new JNNModelSpec();
    private File modelFile = null;
    private File schemaFile = null;
    private ITabularInput dataSource;
    private Thread trainerThread = new Thread("Training Thread");
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox rootContainer;

    @FXML
    private WebView dataSourceWebView;

    @FXML
    private JFXSpinner trainingProgressIndicator;

    @FXML
    private ListView<Integer> hiddenLayerListView;

    @FXML
    private TableView<DesignFieldConfig> fieldTable;

    @FXML
    private TableColumn<DesignFieldConfig, String> fieldNameColumn;

    @FXML
    private TableColumn<DesignFieldConfig, DesignFieldConfig.Role> fieldPurposeColumn;

    @FXML
    private TableColumn<DesignFieldConfig, DesignFieldConfig.DataFieldType> fieldDataTypeColumn;

    @FXML
    private TableColumn<DesignFieldConfig, String> fieldParametersColumn;

    @FXML
    private TableView<InteractiveDataTuple> evaluationTableView;

    @FXML
    private TableColumn<InteractiveDataTuple, String> evaluationKeyTableColumn;

    @FXML
    private TableColumn<InteractiveDataTuple, String> evaluationValueTableColumn;

    @FXML
    private BorderPane evaluationBorderPane;

    @FXML
    private JFXButton trainButton;

    @FXML
    private JFXButton stopTrainingButton;

    @FXML
    private JFXTabPane tabPane;

    @FXML
    private Tab fitnessTab;

    @FXML
    private BorderPane trainingMonitorBorderPane;

    @FXML
    private BorderPane fitnessBorderPane;

    @FXML
    private VBox trainingGaugesVBox;

    @FXML
    private Spinner<Integer> epochSpinner;

    @FXML
    private Slider trainingRatioSlider;

    @FXML
    private Label trainingRatioLabel;

    private Tile evaluationConfidenceGaugeTile = eu.hansolo.tilesfx.TileBuilder.create()
            .prefSize(150, 150)
            .title("Confidence")
            .skinType(SkinType.GAUGE)
            .unit("%")
            .threshold(50)
            .build();

    private XYChart.Series<String, Number> trainingEvaluationPositiveSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> trainingEvaluationNegativeSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> trainingEvaluationScoreSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> trainingEvaluationF1Series = new XYChart.Series<>();

    private Tile trainingTile = eu.hansolo.tilesfx.TileBuilder.create()
            .skinType(SkinType.SMOOTHED_CHART)
            .prefSize(150, 150)
            .title("Training Progress")
            //            .animated(true) 
            .smoothing(true)
            .series(trainingEvaluationPositiveSeries, trainingEvaluationNegativeSeries, trainingEvaluationScoreSeries, trainingEvaluationF1Series)
            .build();

    private Tile trainingScoreGauge = TileBuilder.create().skinType(SkinType.GAUGE)
            .prefSize(200, 200)
            .title("Score")
            .unit("%")
            .animated(true)
            .animationDuration(250)
            .build();
    private Tile trainingFalsePositiveGauge = TileBuilder.create().skinType(SkinType.GAUGE)
            .prefSize(200, 200)
            .title("False Positives")
            .unit("%")
            .animated(true)
            .animationDuration(250)
            .build();
    private Tile trainingFalseNegativeGauge = TileBuilder.create().skinType(SkinType.GAUGE)
            .prefSize(200, 200)
            .title("False Negatives")
            .unit("%")
            .animated(true)
            .animationDuration(250)
            .build();
    private Tile fitnessSpinner = TileBuilder.create().skinType(SkinType.IMAGE)
            .prefSize(400, 400)
            .title("Calculating Fitness")
            .unit("")
            .image(new Image(getClass().getResourceAsStream("network.gif")))
            .build();
    private Tile fitnessGauge = TileBuilder.create().skinType(SkinType.BAR_GAUGE)
            .prefSize(400, 400)
            .title("Overall Fitness (MCC Score)")
            .unit("")
            .animated(true)
            .animationDuration(500)
            .text("Overall fitness of the model against the full data set.")
            .minValue(0.0)
            .maxValue(1.0)
            .decimals(2)
            .build();

    private JFXSnackbar snackBar = null;

    private JNNTrainingListener uiTrainingListener = new JNNTrainingListener() {
        int currentEpoch = 0;

        @Override
        public void startEpoch(int epoch) {
            currentEpoch = epoch;
        }

        @Override
        public void endEpoch(int epoch) {
            currentEpoch = epoch;
            double newValue = ((double) epoch + 1) / Double.valueOf(epochSpinner.getValue());
            Platform.runLater(() -> trainingProgressIndicator.setProgress(newValue));
        }

        @Override
        public synchronized void evaluation(Evaluation evaluation, double lastScore) {
            final int targetEpoch = currentEpoch;
            Platform.runLater(() -> {
                trainingEvaluationF1Series.getData().add(new XYChart.Data<>("" + targetEpoch, 1 - evaluation.f1()));
                trainingEvaluationNegativeSeries.getData().add(new XYChart.Data<>("" + targetEpoch, evaluation.falseNegativeRate()));
                trainingEvaluationPositiveSeries.getData().add(new XYChart.Data<>("" + targetEpoch, evaluation.falsePositiveRate()));
                trainingEvaluationScoreSeries.getData().add(new XYChart.Data<>("" + targetEpoch, lastScore));
                trainingScoreGauge.setValue(lastScore * 100D);
                trainingFalseNegativeGauge.setValue(evaluation.falseNegativeRate() * 100D);
                trainingFalsePositiveGauge.setValue(evaluation.falsePositiveRate() * 100D);
            });
        }
    };
    private JNNModelEvaluator evaluator;

    @FXML
    void actionDetailsInBrowser(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:9000"));
        } catch (IOException | URISyntaxException ex) {
            handleException(ex);
        }
    }

    @FXML
    void actionAbout(ActionEvent event) {
        alert("Not Yet Implemented", "This feature is not yet implemented.");
    }

    @FXML
    void actionClose(ActionEvent event) {
        alert("Not Yet Implemented", "This feature is not yet implemented.");
    }

    @FXML
    void actionNew(ActionEvent event) {
        alert("Not Yet Implemented", "This feature is not yet implemented.");
    }

    @FXML
    void actionStopTraining(ActionEvent event) {
        trainer.requestEarlyStop();
    }

    @FXML
    void actionOpen(ActionEvent event) {
        File selected = schemeFileChooser.showOpenDialog(findWindow());
        if (selected != null) {
            schemaFile = selected;
            loadSchema();
        }
    }

    @FXML
    void actionPreferences(ActionEvent event) {
        alert("Not Yet Implemented", "This feature is not yet implemented.");
    }

    @FXML
    void actionQuit(ActionEvent event) {
        //TODO: Prompt to save.
        Platform.exit();
    }

    @FXML
    void actionSave(ActionEvent event) {
        if (schemaFile == null) {
            actionSaveAs(event);
        } else {
            saveSchema();
        }
    }

    @FXML
    void actionSaveAs(ActionEvent event) {
        File selected = schemeFileChooser.showSaveDialog(findWindow());
        if (selected != null) {
            schemaFile = selected;
            saveSchema();
        }
    }

    @FXML
    void actionAddLayer(ActionEvent event) {
        hiddenLayerListView.getItems().add(DEFAULT_LAYER_WIDTH);
    }

    @FXML
    void actionDeleteSelectedLayer(ActionEvent event) {
        // Delete in reverse index order so that earlier deletes don't renumber later ones.
        TreeSet<Integer> sortedSelectionIndexes = new TreeSet<>(hiddenLayerListView.getSelectionModel().getSelectedIndices());
        while (!sortedSelectionIndexes.isEmpty()) {
            int index = sortedSelectionIndexes.last();
            hiddenLayerListView.getItems().remove(index);
            sortedSelectionIndexes.remove(index);
        }
    }

    @FXML
    void actionSelectDataSourceFile(ActionEvent event) {

        File selectedFile = datasourceFileChooser.showOpenDialog(findWindow());

        if (selectedFile != null) {
            setDataSourceFile(selectedFile);
            modelFile = null;
            schemaFile = null;
        }
    }

    private void setDataSourceFile(File selectedFile) {
        ITabularInput input;
        try {
            if (selectedFile.getName().endsWith(".csv")) {
                input = new CsvTabularInput(selectedFile);
            } else {
                input = new ExcelTabularInput(selectedFile);
            }
            setDataSource(input);
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    @FXML
    void actionTrain(ActionEvent event) {
        if (!trainerThread.isAlive()) {
            try {
                popSnackBar("Training started.");
                JNNModelSpec spec = generateSpec();
                if (dataSource == null) {
                    alert("No data source", "A data source must be selected to train.");
                    return;
                }
                if (schemaFile == null) {
                    schemaFile = schemeFileChooser.showSaveDialog(findWindow());
                }
                if (schemaFile == null) {
                    //If we can't save the schema, we stop.
                    return;
                }
                if (modelFile == null) {
                    modelFile = modelFileChooser.showSaveDialog(findWindow());
                }
                if (modelFile == null) {
                    // We need a location to save the model, abort otherwise.
                    return;
                }
                trainer.addJnnListener(uiTrainingListener);
                trainingProgressIndicator.setProgress(0.0D);
                trainingEvaluationF1Series.getData().clear();
                trainingEvaluationNegativeSeries.getData().clear();
                trainingEvaluationPositiveSeries.getData().clear();
                trainingEvaluationScoreSeries.getData().clear();
                trainingEvaluationF1Series.setName("(1 - F1) Score");
                trainingEvaluationNegativeSeries.setName("False Negatives");
                trainingEvaluationPositiveSeries.setName("False Positives");
                trainingEvaluationScoreSeries.setName("Score");
                trainingProgressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                final double dataRatio = trainingRatioSlider.getValue();
                trainerThread = new Thread(() -> {
                    try {
                        trainer.buildAndTrainFittestNetwork(spec, dataSource, dataRatio, modelFile, epochSpinner.getValue());
                        try {
                            spec.writeTo(schemaFile);
                        } catch (IOException ex) {
                            handleException(ex);
                        }
                        Platform.runLater(() -> {
                            popSnackBar("Trained schema saved to " + schemaFile.getName());
                            stopTrainingButton.setDisable(true);
                            trainButton.setDisable(false);
                            fitnessBorderPane.setCenter(fitnessSpinner);
                            tabPane.getSelectionModel().select(fitnessTab);
                            // Paint a pretty tile for the fitness progress.

                        });
                        try {
                            JNNModelEvaluator jnnModelEvaluator = new JNNModelEvaluator(MultiLayerNetwork.load(modelFile, false), spec);
                            JNNFitnessEvaluator.FitnessEvaluation fitnessEvaluation = JNNFitnessEvaluator.evaluateFitness(jnnModelEvaluator, dataSource);
                            Platform.runLater(() -> {
                                popSnackBar("MCC Coefficient: " + fitnessEvaluation.getMCC());
                                fitnessBorderPane.setCenter(fitnessGauge);
                                fitnessGauge.setValue(fitnessEvaluation.getMCC());
                            });
                            System.out.println("MCC Coefficient: " + fitnessEvaluation.getMCC());
                            setEvaluator(jnnModelEvaluator);
                        } catch (IOException ex) {
                            handleException(ex);
                        }
                    } catch (Exception ex) {
                            handleException(ex);
                    }
                }, "Training thread");
                trainerThread.start();
                stopTrainingButton.setDisable(false);
                trainButton.setDisable(true);
            } catch (InvalidNeuralConfigException ex) {
                handleException(ex);
            }
        }
    }

    private Window findWindow() {
        return this.rootContainer.getScene().getWindow();
    }

    @FXML
    void initialize() {
        // Set up the snackBar
        snackBar = new JFXSnackbar(rootContainer);

        // Bind the renderers for the field schema table
        fieldDataTypeColumn.setCellValueFactory(new PropertyValueFactory<>("dataFieldType"));
        fieldDataTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(DesignFieldConfig.DataFieldType.values()));
        fieldPurposeColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        fieldPurposeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(DesignFieldConfig.Role.values()));
        fieldParametersColumn.setCellValueFactory(new PropertyValueFactory<>("params"));
        fieldParametersColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fieldNameColumn.setCellValueFactory(new PropertyValueFactory<>("fieldName"));
        fieldNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // Bind the editors so they write back to the model.
        fieldNameColumn.setOnEditCommit((TableColumn.CellEditEvent<DesignFieldConfig, String> t) -> {
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setFieldName(t.getNewValue());
        });
        fieldParametersColumn.setOnEditCommit((TableColumn.CellEditEvent<DesignFieldConfig, String> t) -> {
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setParams(t.getNewValue());
        });
        fieldDataTypeColumn.setOnEditCommit((TableColumn.CellEditEvent<DesignFieldConfig, DesignFieldConfig.DataFieldType> t) -> {
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setDataFieldType(t.getNewValue());
        });
        fieldPurposeColumn.setOnEditCommit((TableColumn.CellEditEvent<DesignFieldConfig, DesignFieldConfig.Role> t) -> {
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setRole(t.getNewValue());
        });

        // Set up the network layout table
        hiddenLayerListView.setCellFactory(TextFieldListCell.forListView(new IntegerStringConverter()));

        // Set up the Training panel
        SpinnerValueFactory.IntegerSpinnerValueFactory integerSpinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE);
        integerSpinnerValueFactory.setConverter(new IntegerStringConverter() {
            @Override
            public Integer fromString(String string) {
                try {
                    return super.fromString(string);
                } catch (NumberFormatException ex) {
                    return 1;
                }
            }
        });
        epochSpinner.setValueFactory(integerSpinnerValueFactory);
        integerSpinnerValueFactory.setValue(100);
        trainingRatioSlider.setMax(1.0);
        trainingRatioSlider.setMajorTickUnit(0.1);
        trainingRatioSlider.setMinorTickCount(1);
        trainingRatioSlider.valueProperty().addListener((ov, old_val, new_val) -> trainingRatioLabel.setText(String.format("%.2f", new_val.doubleValue() * 100D) + " % of data used for training"));
        trainingRatioSlider.setValue(0.5D);
        trainingMonitorBorderPane.setCenter(trainingTile);
        trainingGaugesVBox.getChildren().add(trainingScoreGauge);
        trainingGaugesVBox.getChildren().add(trainingFalseNegativeGauge);
        trainingGaugesVBox.getChildren().add(trainingFalsePositiveGauge);

        // Set up the fitness panel
        // Set up the evaluation table
        evaluationBorderPane.setRight(evaluationConfidenceGaugeTile);
        evaluationKeyTableColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        evaluationValueTableColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        evaluationValueTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        evaluationValueTableColumn.setOnEditCommit((TableColumn.CellEditEvent<InteractiveDataTuple, String> t) -> {
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
            updateEvaluation();
        });

        // Set up the data source file chooser
        datasourceFileChooser.setTitle("Open Data Source File");
        datasourceFileChooser.getExtensionFilters().clear();
        datasourceFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma-separated values", "*.csv"));
        datasourceFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MS Excel", "*.xls", "*.xlsx"));
        // Set up the scheme file chooser
        schemeFileChooser.setTitle("Scheme");
        schemeFileChooser.getExtensionFilters().clear();
        schemeFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Neural Net Schema", "*.jnn"));
        // Set up the model file chooser
        modelFileChooser.setTitle("Neural Net Model");
        modelFileChooser.getExtensionFilters().clear();
        modelFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Neural Net Model", "*.mdl"));

    }

    public JNNModelSpec generateSpec() throws InvalidNeuralConfigException {
        JNNModelSpec returnable = new JNNModelSpec();
        boolean hasLabel = false;
        boolean hasTooManyLabels = false;
        for (DesignFieldConfig item : fieldTable.getItems()) {
            if (item.getRole() != null) {
                switch (item.getRole()) {
                    case LABEL:
                        if (!hasLabel) {
                            DataField labelField = createFieldFromFieldConfig(item);
                            if (labelField instanceof LabelDataField) {
                                returnable.setLabelDataField((LabelDataField) labelField);
                                hasLabel = true;
                            } else {
                                throw new InvalidNeuralConfigException(""
                                        + item.getFieldName()
                                        + " must be of type boolen, closed vocabulary, or multivalue closed vocabulary to be used as a label.");
                            }
                        } else {
                            hasTooManyLabels = true;
                        }
                        break;
                    case DATA:
                        returnable.addDataField(createFieldFromFieldConfig(item));
                        break;
                    default:
                        break;
                }
            }
        }
        if (!hasLabel || hasTooManyLabels) {
            throw new InvalidNeuralConfigException("Exactly one field must be assigned to labelling data.");
        }
        if (returnable.getDataFields().isEmpty()) {
            throw new InvalidNeuralConfigException("At least one field must be assigned to trainable data.");
        }

        int[] layers = new int[hiddenLayerListView.getItems().size()];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = hiddenLayerListView.getItems().get(i); // Auto-unboxing happens here.
        }
        returnable.setHiddenLayers(layers);
        return returnable;
    }

    private void handleException(Throwable ex) {
        Logger.getLogger(InteractiveDesignUIController.class.getName()).log(Level.SEVERE, null, ex);
        alert(ex.getClass().getName(), ex.getMessage());
    }

    private void alert(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText(title);
            alert.setHeaderText(message);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText(title);
                alert.setHeaderText(message);
                alert.showAndWait();
            });
        }
    }

    private void setDataSource(ITabularInput input) {
        this.dataSource = input;
        datasourceFields.clear();
        datasourceFields.addAll(input.getKeys());
        System.out.println(Arrays.deepToString(datasourceFields.toArray()));
        try {
            Document parse = Jsoup.parse(
                    getClass().getResourceAsStream("DataSourceDescriptor.html"),
                    "UTF-8",
                    getClass().getResource("DataSourceDescriptor.html").toExternalForm());
            Element fields = parse.selectFirst("#fields");
            datasourceFields.forEach((field) -> fields.appendElement("li").appendText(field));
            MutableInt counter = new MutableInt();
            input.forEach((Map<String, String> row) -> {
                //There's likely a bunch more to be done here.
                counter.increment();
            });

            parse.selectFirst("#rowCount").appendText(counter.toString());
            parse.selectFirst("#description").appendText(input.getDescription());
            dataSourceWebView.getEngine().loadContent(parse.toString());
            popSnackBar("Data source loaded.");
        } catch (IOException ex) {
            handleException(ex);
        }
        fieldTable.getItems().clear();
        datasourceFields.forEach((fieldName) -> fieldTable.getItems().add(new DesignFieldConfig(fieldName, DesignFieldConfig.DataFieldType.FREE_TEXT, DesignFieldConfig.Role.IGNORE, "")));
    }

    private DataField createFieldFromFieldConfig(DesignFieldConfig item) throws InvalidNeuralConfigException {
        switch (item.getDataFieldType()) {
            case BOOLEAN:
                return new BooleanDataField(item.getFieldName());
            case CLOSED_VOCAB:
                return new ClosedVocabDataField(item.getFieldName(), item.getParams().split(";"));
            case CLOSED_VOCAB_MULTIVALUE:
                return new MultiValuedClosedVocabDataField(item.getFieldName(), item.getParams().split(";"));
            case ENGLISH_WORD_STEM:
                return new EnglishWordStemDataField(item.getFieldName(), parseProjectionSize(item));
            case ENGLISH_WORD_STEM_INCIDENCE:
                return new EnglishIncidenceFilteredTextField(item.getFieldName(), parseProjectionSize(item));
            case FRENCH_WORD_STEM:
                return new FrenchWordStemDataField(item.getFieldName(), parseProjectionSize(item));
            case FRENCH_WORD_STEM_INCIDENCE:
                return new FrenchIncidenceFilteredTextField(item.getFieldName(), parseProjectionSize(item));
            case FREE_TEXT:
                return new FreeTextDataField(item.getFieldName(), " /\\|,.\"", parseProjectionSize(item));
            case WORD2VEC_TEXT:
                return new Word2VecDataField(item.getFieldName(), parseProjectionSize(item));
            //TODO: look at the set of default delimiters.
            case UNBOUNDED:
                String[] unboundedParams = item.getParams().split(";");
                double offset = 0.0D;
                if (unboundedParams.length > 0) {
                    offset = Double.parseDouble(unboundedParams[0]);
                }
                double compressionFactor = 10.0D;
                if (unboundedParams.length > 1) {
                    compressionFactor = Double.parseDouble(unboundedParams[1]);
                }
                return new UnboundedDataField(item.getFieldName(), offset, compressionFactor);
            case NORMALIZED:
                String[] normalizedParams = item.getParams().split(";");
                double min = 0.0D;
                if (normalizedParams.length > 0) {
                    min = Double.parseDouble(normalizedParams[0]);
                }
                double max = 10.0D;
                if (normalizedParams.length > 0) {
                    max = Double.parseDouble(normalizedParams[1]);
                }
                return new NormalizedDataField(item.getFieldName(), min, max);
            case PRENORMALIZED:
                return new NormalizedDataField(item.getFieldName());
            default:
                throw new InvalidNeuralConfigException("Field " + item.getFieldName() + " has unknown type: " + item.getDataFieldType());
        }
    }

    private int[] parseProjectionSize(DesignFieldConfig item) throws NumberFormatException {
        int[] projectionSize = new int[]{250};
        if (!item.getParams().isEmpty()) {
            String[] parts = item.getParams().split(";");
            projectionSize = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                projectionSize[i] = Integer.parseUnsignedInt(parts[i].trim());
            }
        }
        return projectionSize;
    }

    private void loadSchema() {
        try (FileInputStream fis = new FileInputStream(schemaFile)) {
            JNNModelSpec loadedSpec = JNNModelSpec.readFrom(fis);
            LabelDataField labelDataField = loadedSpec.getLabelDataField();
            HashSet<DesignFieldConfig> updatedParams = new HashSet<>(fieldTable.getItems());
            updatedParams.forEach((DesignFieldConfig config) -> {
                config.setRole(DesignFieldConfig.Role.IGNORE);
                config.setParams("");
                loadedSpec.getDataFields().forEach((dataField) -> {
                    if (config.getFieldName().equals(dataField.getName())) {
                        config.setParams(dataField.getParams());
                        config.setRole(DesignFieldConfig.Role.DATA);
                        config.setDataFieldType(dataField.getType());
                    }
                });
                if (config.getFieldName().equals(labelDataField.getName())) {
                    config.setParams(labelDataField.getParams());
                    config.setRole(DesignFieldConfig.Role.LABEL);
                    config.setDataFieldType(labelDataField.getType());
                }
            });
            fieldTable.getItems().clear();
            fieldTable.getItems().addAll(updatedParams);

            hiddenLayerListView.getItems().clear();
            for (int i : loadedSpec.getHiddenLayers()) {
                hiddenLayerListView.getItems().add(i);
            }
            popSnackBar("Schema loaded: " + schemaFile.getName());
        } catch (IOException ex) {
            handleException(ex);
        }
    }

    private boolean saveSchema() {
        try {
            generateSpec().writeTo(schemaFile);
            popSnackBar("Schema saved: " + schemaFile.getName());
        } catch (InvalidNeuralConfigException | IOException ex) {
            handleException(ex);
            return false;
        }
        return true;
    }

    public void setEvaluator(JNNModelEvaluator evaluator) {
        this.evaluator = evaluator;
        Platform.runLater(() -> {
            JNNModelSpec spec = evaluator.getSpec();
            List<InteractiveDataTuple> tuples = spec.getDataFields().stream().map(field -> new InteractiveDataTuple(field.getName(), "")).collect(Collectors.toList());
            evaluationTableView.getItems().clear();
            evaluationTableView.getItems().addAll(tuples);
        });
    }

    private void updateEvaluation() {
        Map<String, String> interactiveData = new TreeMap<>();
        double evaluation = 0.0D;
        try {
            evaluationTableView.getItems().forEach((InteractiveDataTuple tuple) -> interactiveData.put(tuple.getKey(), tuple.getValue()));
            evaluation = evaluator.evaluate(interactiveData);
        } catch (JNNModelSpec.InvalidInputException ex) {
            handleException(ex);
        }
        evaluationConfidenceGaugeTile.setValue(evaluation * 100.0D);
    }

    public void popSnackBar(final String message) {
        Platform.runLater(() -> {
            JFXSnackbarLayout snackbarLayout = new JFXSnackbarLayout(message);
            snackbarLayout.setBackground(new Background(new BackgroundFill(new Color(0.8D, 0.8D, 0.8D, 0.5D), new CornerRadii(5D), Insets.EMPTY)));
            snackBar.enqueue(new JFXSnackbar.SnackbarEvent(snackbarLayout));
        });
    }
}
