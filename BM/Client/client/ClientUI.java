package client;

import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The ClientUI class serves as the main entry point for the JavaFX application.
 * It initializes the client-side controller and launches the login interface.
 */
public class ClientUI extends Application {
	
    /** The client controller instance used for managing client-side operations. */
    public static ClientController chat;

    /**
     * The main entry point for the JavaFX application.
     * This method sets up the client controller and initializes the login interface.
     *
     * @param primaryStage The primary stage for this application, onto which
     *                     the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            String host = getParameters().getRaw().isEmpty() ? "localhost" : getParameters().getRaw().get(0);

            // Initialize the ClientController with the given IP address
            chat = new ClientController(host, 5555);  // Adjust port as needed
            
            // Create and start the LoginController
            LoginController loginController = new LoginController();
            loginController.start(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The main method is the entry point of the Java application.
     * It launches the JavaFX application.
     *
     * @param args Command line arguments passed to the application.
     *             The first argument, if provided, is used as the host address.
     */
    public static void main(String[] args) {
        launch(args);  // Pass the args to the JavaFX application
    }
}
