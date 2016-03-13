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

    private String imagePath = null;

    private Task<Void> filterWorker;

    //Event handlers

    @FXML
    protected void handleLeftImageButtonAction(ActionEvent event) {

    	openImage();
    }

    @FXML
    public void handleRightImageButtonAction(ActionEvent event) {

    	saveImage();
    }

    //Methods

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

	public void initialize(URL location, ResourceBundle resources) {

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

		applyCSS();

		configureAsNumericBox(spacialParameterBox);
		configureAsNumericBox(rangeParameterBox);
		configureAsNumericBox(iterationNumberBox);
		configureAsNumericBox(convergenceBox);

		runBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
            	Image image = leftImageView.getImage();
            	if (image == null) return;
				runBtn.setDisable(true);
                filterWorker = createFilterWorker();
                progressBar.progressProperty().bind(filterWorker.progressProperty());
                new Thread(filterWorker).start();
            }
		});

		metricsBox.setItems(FXCollections.observableArrayList("Euklidesowa", "Manhattan"));
		metricsBox.getSelectionModel().selectFirst();
	}

	private void configureAsNumericBox(TextField text) {
		text.focusedProperty().addListener((arg0, oldValue, newValue) -> {
	        if (!newValue) {
	            if(!text.getText().matches(NUMERIC_PATTERN)){
	            	text.setText("1");
	            }
	        }
	    });
	}

	private void applyCSS() {

		leftImageBtn.getStyleClass().add("button-metallic-grey");
		runBtn.getStyleClass().add("button-metallic-grey");
		rightImageBtn.getStyleClass().add("button-metallic-grey");
	}

	protected Task<Void> createFilterWorker() {
        return new Task<Void>() {
            @Override
            protected Void call() {
            	// wywolanie funkcji filtru mean-shift
            	if (imagePath == null)
            		return null;
            	Image image = new Image(imagePath);

            	ColorProcesser cp = new ColorProcesser();
            	int[][] pixels = cp.getPixelArray(image);
            	LuvPixel[] luv = cp.getLuvArray(pixels);
            	LuvPixel[] out = new LuvPixel[luv.length];

            	int width = pixels.length;
            	int height = pixels[0].length;

            	double shift = 0;
        		int iters = 0, maxIters = Integer.parseInt(iterationNumberBox.getText());
        		int hrad = Integer.parseInt(spacialParameterBox.getText());
        		int hcolor = Integer.parseInt(rangeParameterBox.getText());
        		int minShift = Integer.parseInt(convergenceBox.getText());
        		int pixelNumber = luv.length;
            	updateProgress(0,  pixelNumber);

				RectangularKernel kernel = new RectangularKernel();
				Metrics metrics = MetricsFactory.getMetrics(metricsBox.getValue());

        		// dla kazdego piksela
        		for (int i = 0; i < pixelNumber; i++) {

        			// pobierz aktualna pozycje piksela
        			int xc = (int) luv[i].getPosition().getX();
        			int yc = (int) luv[i].getPosition().getY();
        			// miejsce na stare dane
        			int xcOld, ycOld;
        			float LcOld, UcOld, VcOld;
        			// aktualna poyzcja i kolor
        			Point3D color = luv[i].getColor();
        			float Lc = (float)color.getX();
        			float Uc = (float)color.getY();
        			float Vc = (float)color.getZ();
        			// licznik iteracji
        			iters = 0;
        			// mean-shiftowanie
        			do {
        				// zachowanie starych danych
        				xcOld = xc; ycOld = yc;
        				LcOld = Lc; UcOld = Uc; VcOld = Vc;
        				// wartosci przesuniecia
        				float mx = 0,  my = 0, mL = 0, mU = 0, mV = 0;
        				double pointNum = 0.0, colorNum = 0.0;
        				// MEAN SHIFT (17)
        				for (int ry = -hrad; ry <= hrad; ry++) {
        					int y2 = yc + ry;
        					if (y2 >= 0 && y2 < height) {
        						for (int rx = -hrad; rx <= hrad; rx++) {
        							int x2 = xc + rx;
        							if (x2 >= 0 && x2 < width) {
        								double pointDistance = metrics.getDistance(ry, rx);
        								if (pointDistance <= hrad) {
        									color = luv[y2 * width + x2].getColor();

            								float L2 = (float) color.getX();
            								float U2 = (float) color.getY();
            								float V2 = (float) color.getZ();

            								double dL = Lc - L2;
            								double dU = Uc - U2;
            								double dV = Vc - V2;

            								double colorDistance = metrics.getDistance(dL, dU, dV);
            								if (colorDistance <= hcolor) {
            									double pointKernel = kernel.gFunction(Math.pow(pointDistance / hrad, 2));
            									mx += x2 * pointKernel;
            									my += y2 * pointKernel;
            									pointNum += pointKernel;
            									double colorKernel = kernel.gFunction(Math.pow(colorDistance / hcolor, 2));
            									mL += L2 * colorKernel;
            									mU += U2 * colorKernel;
            									mV += V2 * colorKernel;
            									colorNum += colorKernel;
            								}
        								}
        							}
        						}
        					}
        				}
        				// nowe przesuniecie okna
        				xc = (int) (mx * (1.0 / pointNum) + 0.5);
        				yc = (int) (my * (1.0 / pointNum) + 0.5);
        				Lc = (float) (mL * (1.0 / colorNum));
        				Uc = (float) (mU * (1.0 / colorNum));
        				Vc = (float) (mV * (1.0 / colorNum));
        				// mean-shift
        				int dx = xc - xcOld;
        				int dy = yc - ycOld;
        				float dL = Lc - LcOld;
        				float dU = Uc - UcOld;
        				float dV = Vc - VcOld;

        				shift = metrics.getDistance(dx, dy, dL, dU, dV);
        				iters++;
        			} while (shift > minShift && iters < maxIters);

        			out[i] = new LuvPixel(luv[i].getPosition(), new Point3D(Lc, Uc, Vc));
        			updateProgress(i, pixelNumber);
        		}

        		int[][] rgb = cp.getRgbArray(out, width);
        		WritableImage filteredImage = new WritableImage(width, height);
        		PixelWriter pw = filteredImage.getPixelWriter();
        		for (int i = 0; i < width; i++) {
        			for (int j = 0; j < height; j++) {
        				pw.setArgb(i, j, rgb[i][j]);
        			}
        		}

            	rightImageView.setImage(filteredImage);
            	rightImageBtn.setDisable(false);
        		runBtn.setDisable(false);
                return null;
            }
        };
    }

}