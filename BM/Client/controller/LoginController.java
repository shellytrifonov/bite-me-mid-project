package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ClientController;
import client.ClientUI;
import entities.Message;
import entities.User;
import entities.User.UserRole;

/**
 * Controller class for the login screen of the Bite Me application.
 * Handles user authentication and navigation to role-specific pages.
 * @author Shelly Trifonov
 */
public class LoginController implements Initializable {

	/**
	 * The main container for the login screen UI elements.
	 */
	@FXML private AnchorPane mainPane;

	/**
	 * Label displaying the title of the login screen.
	 */
	@FXML private Label titleLabel;

	/**
	 * Text field for entering the username.
	 */
	@FXML private TextField usernameField;

	/**
	 * Password field for entering the user's password.
	 */
	@FXML private PasswordField passwordField;

	/**
	 * Button to initiate the login process.
	 */
	@FXML private Button loginButton;

	/**
	 * Button to exit the application.
	 */
	@FXML private Button exitButton;

	/**
	 * Label for displaying error messages during the login process.
	 */
	@FXML private Label errorLabel;

	/**
	 * Stores the User object of the currently logged-in user.
	 */
	private User loggedInUser;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
    /**
     * Sets the ClientController for this LoginController.
     *
     * @param clientController The ClientController to be used for server communication.
     */
    private ClientController getClientController() {
        return ClientUI.chat;
    }

    /**
     * Handles the login process when the login button is clicked.
     * Validates user input, sends login request to server, and navigates to appropriate page based on user role.
     */
    @FXML
    private void handleLogin() {
        ClientController clientController = getClientController();
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        Message<?> response = clientController.handleLogin(username, password);
        System.out.println("Received response from server: " + response);

        if (response != null) {
            switch (response.getMessage()) {
                case "LOGIN_SUCCESS":
                    loggedInUser = (User) response.getType();
                    navigateToAppropriateScreen(loggedInUser.getRole(),loggedInUser);
                    break;
                case "USER_ALREADY_LOGGED_IN":
                    errorLabel.setText("User is already logged in.");
                    break;
                case "LOGIN_FAILED":
                default:
                    errorLabel.setText("Invalid username or password.");
                    break;
            }
        } else {
            errorLabel.setText("No response from server. Please try again.");
        }
    }

    /**
     * Navigates to the appropriate screen based on the user's role.
     *
     * @param role The role of the logged-in user.
     */
    private void navigateToAppropriateScreen(UserRole role,User currentUser) {
        Stage loginStage = (Stage) loginButton.getScene().getWindow();
        loginStage.hide();

        switch (role) {
            case MANAGER:
            case CEO:
                ManagerController managerController = new ManagerController();
                managerController.setClientController(this.getClientController());
                managerController.setLoginStage(loginStage);
                managerController.setCurrentUser(currentUser);
                Stage managerStage = new Stage();
                managerController.start(managerStage);
                break;
            case CUSTOMER_BUSINESS:
            case CUSTOMER_PRIVATE:
                CustomerController customerController = new CustomerController();
                customerController.setClientController(this.getClientController());
                customerController.setLoginStage(loginStage);
                customerController.setCurrentUser(currentUser);
                Stage customerStage = new Stage();
                customerController.start(customerStage);
                break;
            case RESTAURANT:
                RestaurantController restaurantController = new RestaurantController();
                restaurantController.setClientController(this.getClientController());
                restaurantController.setLoginStage(loginStage);
                restaurantController.setCurrentUser(currentUser);
                Stage restaurantStage = new Stage();
                restaurantController.start(restaurantStage);
                break;
            default:
                errorLabel.setText("Unknown user role.");
        }
    }

    /**
     * Handles the exit button click event.
     */
    @FXML
    private void handleExit() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Displays the login screen.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     */
    public void start(Stage primaryStage){
    	Pane root;
    	try {
    		root = FXMLLoader.load(getClass().getResource("/boundry/LoginScreen.fxml"));
            primaryStage.setTitle("Bite Me - Login");
            primaryStage.setScene(new Scene(root, 400, 600));
            primaryStage.show();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}