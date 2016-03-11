package mean.shift.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class WindowController implements Initializable {
	@FXML
	private AnchorPane mainPane;

    @FXML
    private Button leftImageBtn;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private ImageView leftImageView;

    @FXML
    protected void handleLeftImageButtonAction(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Otworz plik z rysunkiem...");
    	fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Wszystkie rysunki", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
            );
    	File file = fileChooser.showOpenDialog(null);
        if (file != null) {
        	Image image = new Image(file.toURI().toString());
        	leftImageView.setImage(image);
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
						leftImageView.setFitWidth(mainPane.getWidth() * 3.0 / 8.0);
						leftImageView.setFitHeight(mainPane.getHeight());
					}
				};
				timer.schedule(task, delayTime);
			}
		};
		mainSplitPane.widthProperty().addListener(listener);
		mainSplitPane.heightProperty().addListener(listener);
	}

}