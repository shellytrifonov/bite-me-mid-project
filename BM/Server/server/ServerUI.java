package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The ServerUI class is the main entry point for the Bite Me server application.
 * It extends JavaFX's Application class to set up and display the server GUI.
 */
public class ServerUI extends Application {

    /**
     * The start method is called after the init method has returned, and after 
     * the system is ready for the application to begin running.
     *
     * @param primaryStage the primary stage for this application, onto which 
     * the application scene can be set.
     * @throws Exception if there is an error loading the FXML file or setting up the scene.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/ServerView.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Bite Me Server");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * The main method is the entry point of the Java application.
     * It launches the JavaFX application.
     *
     * @param args command line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}