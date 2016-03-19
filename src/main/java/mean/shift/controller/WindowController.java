package mean.shift.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.codehaus.jackson.map.ObjectMapper;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import mean.shift.filter.MeanShift;
import mean.shift.filter.MeanShiftTask;
import mean.shift.kernel.BiweightKernel;
import mean.shift.kernel.EpanechnikovKernel;
import mean.shift.kernel.GaussianKernel;
import mean.shift.kernel.Kernel;
import mean.shift.kernel.KernelFactory;
import mean.shift.metrics.EuclideanMetrics;
import mean.shift.metrics.ManhattanMetrics;
import mean.shift.metrics.Metrics;
import mean.shift.metrics.MetricsFactory;
import mean.shift.processing.ConfigurationProfile;
import mean.shift.processing.MeanShiftParameter;

public class WindowController implements Initializable {

	private static String NUMERIC_PATTERN = "[1-9][0-9]*";

	// Members
	@FXML
	private AnchorPane mainPane;

    @FXML
    private Button leftImageBtn;

    @FXML
    private Button runBtn;

    @FXML
    private Button rightImageBtn;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private ImageView leftImageView;

    @FXML
    private ImageView rightImageView;

    @FXML
    private TextField spacialParameterBox;

    @FXML
    private TextField rangeParameterBox;

    @FXML
    private TextField iterationNumberBox;

    @FXML
    private TextField convergenceBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ChoiceBox<String> metricsBox;

    @FXML
    private ChoiceBox<String> kernelBox;

    @FXML
    private javafx.scene.control.MenuItem saveMenuItem;

    @FXML
    private Label ProcessTypeMessage;

    @FXML
    private Label timerLabel;

    @FXML
    private RadioButton filtrationRadioBtn;

    @FXML
    private RadioButton segmentationRadioBtn;

    @FXML
    private Label fileNumerLabel;

    @FXML private Label actualImageLabel;
    @FXML private Label optionsLabel;
    @FXML private Label lastProcessingLabel;

    ConfigurationProfile config = null;


    //Event handlers

    @FXML
    protected void handleLeftImageButtonAction(ActionEvent event) {

    	openImage();
    }

    @FXML
    public void handleRightImageButtonAction(ActionEvent event) {

    	saveImage();
    }

    @FXML
    public void handleOpenMenuItem(ActionEvent event) {

    	openImage();
    }

    @FXML
    public void handleSaveMenuItem(ActionEvent event) {

    	saveImage();
    }

    @FXML
    public void handleCloseMenuItem(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    public void handleLoadProfileMenuItem(ActionEvent event) {
    	loadProfile();
    }

	@FXML
    public void handleSaveProfileMenuItem(ActionEvent event) {
    	saveProfile();
    }

	private void updateQueueLabel(int size) {
    	fileNumerLabel.setText("W kolejce znajduje sie " + size + " plikow.");

	}

	//Methods
    /**
     * Otwiera nowy rysunek.
     */
    public void openImage()
    {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Otworz plik z rysunkiem...");
    	fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Wszystkie rysunki", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
            );
    	List<File> fileList = fileChooser.showOpenMultipleDialog(null);
        if (fileList != null) {
        	updateQueueLabel(fileList.size());
        	File firstFile = fileList.get(0);
        	leftImageView.setImage(new Image(firstFile.toURI().toString()));
        	String absolutePath = firstFile.getAbsolutePath();
    	    String imagePath = absolutePath.
    	    	     substring(0, absolutePath.lastIndexOf(File.separator));
    	    config.setPath(imagePath);
        	List<String> imageList = new ArrayList<>();
        	for (File file : fileList) {
        		imageList.add(file.getName());
			}
        	config.setImages(imageList);
        }
    }

    /**
     * Zapisuje przefiltrowany rysunek.
     */
    public void saveImage()
    {
    	try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Wybierz miejsce na dysku, do ktorego chcesz zapisac plik");
			fileChooser.getExtensionFilters().addAll(
			        new FileChooser.ExtensionFilter("PNG", "*.png")
			    );
			File file = fileChooser.showSaveDialog(null);
			if (file != null) {

				ImageIO.write(SwingFXUtils.fromFXImage(rightImageView.getImage(),
			            null), "png", file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Konfiguruje wszystkie elementy okna.
     */
	public void initialize(URL location, ResourceBundle resources) {

    	config = new ConfigurationProfile();
		applyCSS();
		configureMainSplitPane();
		configureAsNumericBox(spacialParameterBox);
		configureAsNumericBox(rangeParameterBox);
		configureAsNumericBox(iterationNumberBox);
		configureAsNumericBox(convergenceBox);
		configureRunButton();
		kernelBox.setItems(FXCollections.observableArrayList(GaussianKernel.getName(),
							EpanechnikovKernel.getName(), BiweightKernel.getName()));
		kernelBox.getSelectionModel().selectFirst();
		metricsBox.setItems(FXCollections.observableArrayList(EuclideanMetrics.getName(), ManhattanMetrics.getName()));
		metricsBox.getSelectionModel().selectFirst();
		ProcessTypeMessage.textProperty().set("");

		timerLabel.textProperty().set("0:00");
	}

	/**
	 * Konfiguruje zadanie MS tak, by nastepne zadanie zaczelo sie wykonywac
	 * dopiero po wykonaniu poprzedniego. Ustawia pasek postepu oraz
	 * wyswietlone rysunki pod aktualny task.
	 *
	 * @param task aktualne zadanie
	 * @param queue kolejka rysunkow do przetworzenia
	 * @param image nazwa rysunku
	 */
	private void configureMeanShiftTask(MeanShiftTask task, Queue<String> queue, String image) {
		leftImageView.setImage(new Image(new File(config.getPath() + "//" + image).toURI().toString()));
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
        	    new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
            	Image filteredImage = task.getValue();
            	rightImageView.setImage(filteredImage);
            	String fileName = "out_" + image + "_"
            			+ (filtrationRadioBtn.isSelected() ? "filtration" : "segmentation")
            			+ "_hs_" + config.getSpatialPar()
            			+ "_hr_" + config.getRangePar()
            			+ "_maxIters_" + config.getMaxIters()
            			+ "_minShift_" + config.getMinShift()
            			+ "_kernel_" + config.getKernel()
            			+ "_metrics_" + config.getMetrics()
            			+ ".png";
            	File file = new File(config.getPath() + "//" + fileName);
            	try {
					ImageIO.write(SwingFXUtils.fromFXImage(filteredImage, null),
							"png", file);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		});

		progressBar.progressProperty().bind(task.progressProperty());
        ProcessTypeMessage.textProperty().bind(task.titleProperty());
        timerLabel.textProperty().bind(task.messageProperty());

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				updateQueueLabel(queue.size());
				String next = queue.poll();
				if (next != null) {
					MeanShiftTask task = createMeanShiftFilter(next);
					configureMeanShiftTask(task, queue, next);
	                new Thread(task).start();
				} else {
			    	rightImageBtn.setDisable(false);
					runBtn.setDisable(false);
					saveMenuItem.setDisable(false);
					rightImageView.setFitHeight(mainPane.getHeight() - 50);
				}
			}
		});
	}

	/**
	 * Umozliwia przyciskowi "uruchom" wykonanie zadania filtrowania.
	 */
	private void configureRunButton() {
		runBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				List<String> imageList = config.getImages();
				if (imageList == null) return;
				updateConfig();
				runBtn.setDisable(true);
				Queue<String> queue = new ArrayDeque<>(imageList);
				String firstImage = queue.poll();
				MeanShiftTask task = createMeanShiftFilter(firstImage);
				configureMeanShiftTask(task, queue, firstImage);
                Thread thread = new Thread(task);
                thread.start();
            }
		});
	}

	/**
	 * Konfiguruje panel rozdzielny by dostosowywal rozmiar pol
	 * z rysunkami do aktualnego rozmiaru okna.
	 */
	private void configureMainSplitPane() {
		final ChangeListener<Number> listener = new ChangeListener<Number>() {
			final Timer timer = new Timer();
			TimerTask task = null;
			final long delayTime = 200;
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, final Number newValue) {
				if (task != null) {
					task.cancel();
				}
				task = new TimerTask() {
					@Override
					public void run() {
						double splitPaneWidth = mainPane.getWidth() * 3.0 / 8.0;
						double splitPaneHeight = mainPane.getHeight() - 50;
						double labelWidth = mainPane.getWidth() / 3.0;
						leftImageView.setFitWidth(splitPaneWidth);
						leftImageView.setFitHeight(splitPaneHeight);
						rightImageView.setFitWidth(splitPaneWidth);
						rightImageView.setFitHeight(splitPaneHeight);
						mainSplitPane.setPrefWidth(splitPaneWidth);
						mainSplitPane.setPrefHeight(splitPaneHeight);
						actualImageLabel.setPrefWidth(labelWidth);
						optionsLabel.setPrefWidth(labelWidth);
						lastProcessingLabel.setPrefWidth(labelWidth);
					}
				};
				timer.schedule(task, delayTime);
			}
		};
		mainSplitPane.widthProperty().addListener(listener);
		mainSplitPane.heightProperty().addListener(listener);
		// zablokowanie ruchu krawedzi oddzielajacych rysunki
		mainSplitPane.lookupAll(".split-pane-divider").stream()
        	.forEach(div ->  div.setMouseTransparent(true));
	}

	/**
	 * Konfiguruje pole tesktowe tak, by akceptowalo wylacznie liczby.
	 * @param text pole tekstowe
	 */
	private void configureAsNumericBox(TextField text) {
		text.focusedProperty().addListener((arg0, oldValue, newValue) -> {
	        if (!newValue) {
	            if(!text.getText().matches(NUMERIC_PATTERN)){
	            	text.setText("1");
	            }
	        }
	    });
	}

	/**
	 * Dodaje style do poszczegďż˝lnych kontrolek widoku.
	 */
	private void applyCSS() {

		leftImageBtn.getStyleClass().add("button-metallic-grey");
		runBtn.getStyleClass().add("button-metallic-grey");
		rightImageBtn.getStyleClass().add("button-metallic-grey");
		timerLabel.getStyleClass().add("timer-label");
	}

	/**
	 * Utworzenie zadania do wykonania filtru Mean Shift
	 * z pobranymi parametrami na wskazanym rysunku.
	 * @return zadanie mean shift
	 */
	protected MeanShiftTask createMeanShiftFilter(String file) {
    	if (config.getPath() == null) return null;
    	String imagePath = config.getPath() + "//" + file;
		File imageFile = new File(imagePath);
		if (!imageFile.exists()) return null;
    	Image image = new Image(imageFile.toURI().toString());
    	int spatialPar = Integer.parseInt(spacialParameterBox.getText());
    	int rangePar = Integer.parseInt(rangeParameterBox.getText());
    	int maxIters = Integer.parseInt(iterationNumberBox.getText());
    	int minShift = Integer.parseInt(convergenceBox.getText());
    	Metrics metrics = MetricsFactory.getMetrics(metricsBox.getValue());
    	Kernel kernel = KernelFactory.getKernel(kernelBox.getValue());
    	MeanShiftParameter parameter = new MeanShiftParameter(image, kernel, spatialPar, rangePar, maxIters, minShift, metrics);

    	if (segmentationRadioBtn.isSelected()) {
    		return MeanShift.getInstance().createSegmentationWorker(parameter);
    	}
		return MeanShift.getInstance().createFilterWorker(parameter);
    }

	/**
	 * Odczyt konfiguracji profilu z pliku JSON.
	 */
    private void loadProfile() {

    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Wybierz plik z konfiguracja");
    	fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki JSON", "*.json")
            );
    	File file = fileChooser.showOpenDialog(null);
        if (file != null) {
        	ConfigurationProfile oldConfig = config;
        	try {
	        	String profilePath = Paths.get(file.toURI()).toString();
	        	ObjectMapper mapper = new ObjectMapper();
				config = mapper.readValue(new File(profilePath), ConfigurationProfile.class);
				if (config.getImages().size() != 0) {
					String imagePath = config.getPath() + "//" + config.getImages().get(0);
					File imageFile = new File(imagePath);
					if (imageFile.exists()) {
						Image image = new Image(imageFile.toURI().toString());
						leftImageView.setImage(image);
					} else {
						throw new IOException();
					}
				}
				updateQueueLabel(config.getImages().size());
				metricsBox.setValue(config.getMetrics());
				kernelBox.setValue(config.getKernel());
				spacialParameterBox.setText(Integer.toString(config.getSpatialPar()));
				rangeParameterBox.setText(Integer.toString(config.getRangePar()));
				iterationNumberBox.setText(Integer.toString(config.getMaxIters()));
				convergenceBox.setText(Integer.toString(config.getMinShift()));
			} catch (IOException e) {
				config = oldConfig;
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Niepowodzenie");
				alert.setHeaderText(null);
				alert.setContentText("Wybrany plik jest niepoprawna konfiguracja.");
				alert.showAndWait();
			}
        }
	}

    /**
     * Zapis konfiguracji do zewnetrznego pliku JSON.
     */
    private void saveProfile() {
    	FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Wybierz miejsce na dysku, gdzie chcesz zapisac konfiguracje");
		fileChooser.getExtensionFilters().add(
		        new FileChooser.ExtensionFilter("JSON", "*.json")
		    );
		File file = fileChooser.showSaveDialog(null);
		if (file != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				updateConfig();
				mapper.writeValue(new File(file.getAbsolutePath()), config);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

    /**
     * Aktualizacja konfiguracji profilu, pobranie danych z pol
     * i zapis.
     */
    private void updateConfig() {

    	config.setKernel(kernelBox.getValue());
    	config.setMetrics(metricsBox.getValue());
		config.setSpatialPar(Integer.parseInt(spacialParameterBox.getText()));
    	config.setRangePar(Integer.parseInt(rangeParameterBox.getText()));
    	config.setMaxIters(Integer.parseInt(iterationNumberBox.getText()));
    	config.setMinShift(Integer.parseInt(convergenceBox.getText()));
    }

}