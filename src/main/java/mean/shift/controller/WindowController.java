package mean.shift.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import mean.shift.MeanShiftFilter;
import mean.shift.kernel.Kernel;
import mean.shift.kernel.RectangularKernel;
import mean.shift.processing.ColorProcesser;
import mean.shift.processing.LuvPixel;
import mean.shift.processing.Metrics;
import mean.shift.processing.MetricsFactory;

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
    private javafx.scene.control.MenuItem saveMenuItem;
    
    private String imagePath = null;

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
    	File file = fileChooser.showOpenDialog(null);
        if (file != null) {
        	imagePath = file.toURI().toString();
        	leftImageView.setImage(new Image(imagePath));
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

		applyCSS();
		configureMainSplitPane();
		configureAsNumericBox(spacialParameterBox);
		configureAsNumericBox(rangeParameterBox);
		configureAsNumericBox(iterationNumberBox);
		configureAsNumericBox(convergenceBox);
		configureRunButton();
		metricsBox.setItems(FXCollections.observableArrayList("Euklidesowa", "Manhattan"));
		metricsBox.getSelectionModel().selectFirst();
	}

	/**
	 * Umozliwia przyciskowi "uruchom" wykonanie zadania filtrowania.
	 * Po wykonaniu zadania rysunek wyjsciowy jest wyswietlony po prawej stronie.
	 */
	private void configureRunButton() {
		runBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
            	Image image = leftImageView.getImage();
            	if (image == null) return;
				runBtn.setDisable(true);
                Task<Image> meanShift = createMeanShiftFilter();
                meanShift.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                	    new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                    	Image filteredImage = meanShift.getValue();
                    	rightImageView.setImage(filteredImage);
                    	rightImageBtn.setDisable(false);
                		runBtn.setDisable(false);
                		saveMenuItem.setDisable(false);
                    }
                });
                progressBar.progressProperty().bind(meanShift.progressProperty());
                new Thread(meanShift).start();
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
				task = new TimerTask()
				{
					@Override
					public void run() {
						double newWidth = mainPane.getWidth() * 3.0 / 8.0;
						double newHeight = mainPane.getHeight();
						leftImageView.setFitWidth(newWidth);
						leftImageView.setFitHeight(newHeight);
						rightImageView.setFitWidth(newWidth);
						rightImageView.setFitHeight(newHeight);
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

	/*
	 * Dodaje style do poszczególnych kontrolek widoku.
	 */
	private void applyCSS() {

		leftImageBtn.getStyleClass().add("button-metallic-grey");
		runBtn.getStyleClass().add("button-metallic-grey");
		rightImageBtn.getStyleClass().add("button-metallic-grey");
	}

	/**
	 * Utworzenie zadania do wykonania filtru Mean Shift
	 * z pobranymi parametrami na wskazanym rysunku.
	 * @return zadanie mean shift
	 */
	protected Task<Image> createMeanShiftFilter() {
    	if (imagePath == null)
    		return null;
    	Image image = new Image(imagePath);
    	Kernel kernel = new RectangularKernel();
    	int spatialPar = Integer.parseInt(spacialParameterBox.getText());
    	int rangePar = Integer.parseInt(rangeParameterBox.getText());
    	int maxIters = Integer.parseInt(iterationNumberBox.getText());
    	int minShift = Integer.parseInt(convergenceBox.getText());
    	Metrics metrics = MetricsFactory.getMetrics(metricsBox.getValue());
		return new MeanShiftFilter(image, kernel, spatialPar, rangePar, maxIters, minShift, metrics);
    }

}