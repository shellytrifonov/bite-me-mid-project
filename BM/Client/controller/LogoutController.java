package controller;

import client.ChatClient;
import client.ClientController;
import entities.Message;
import entities.User;
import javafx.stage.Stage;

/**
 * The LogoutController class manages the logout functionality across the application.
 * It follows the Singleton pattern to ensure a single instance is used throughout the app.
 * 
 * @author Shelly Trifonov
 */
public class LogoutController {
	
	/**
	 * The single instance of LogoutController, following the Singleton pattern.
	 */
	private static LogoutController instance;

	/**
	 * The ClientController used for server communication during the logout process.
	 */
	private ClientController clientController;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private LogoutController() {}

    /**
     * Gets the single instance of LogoutController.
     * 
     * @return The LogoutController instance
     */
    public static LogoutController getInstance() {
        if (instance == null) {
            instance = new LogoutController();
        }
        return instance;
    }

    /**
     * Sets the ClientController for this LogoutController.
     * 
     * @param clientController The ClientController to be used for server communication
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Performs the logout operation.
     * This method sends a logout request to the server, closes the current stage,
     * and opens the login screen if successful.
     * 
     * @param currentStage The current Stage to be closed on successful logout
     * @return true if logout was successful, false otherwise
     */
    public boolean logout(Stage currentStage) {
        System.out.println("Logout initiated");

        if (clientController == null) {
            System.out.println("Error: ClientController not set");
            return false;
        }

        Message<User> logoutMessage = new Message<>("LOGOUT", ChatClient.currentUser);
        clientController.accept(logoutMessage);

        Message<?> response = clientController.getResponse("LOGOUT");
        
        System.out.println("Logout response: " + response);

        if (response != null && response.getMessage().equals("LOGOUT_SUCCESS")) {
            ChatClient.currentUser = null; // Clear the current user
            return true;
        } else {
            System.out.println("Logout failed. Response: " + (response != null ? response.getMessage() : "null"));
            return false;
        }
    }
}