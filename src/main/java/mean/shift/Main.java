package mean.shift;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Mean Shift");
        showPersonOverview();
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showPersonOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/MeanShiftGUI.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(personOverview);
            primaryStage.setScene(scene);
            primaryStage.show();
         
            scene.getStylesheets().add(this.getClass().getResource("./view/css/application.css").toExternalForm());   		
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Terminate JVM after closing application
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
            	System.exit(0);
            }
        });
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
