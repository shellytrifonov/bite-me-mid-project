package controller;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import client.ClientController;
import entities.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Controller class for the restaurant screen of the Bite Me application.
 * This class handles the functionality of the restaurant's main page.
 * @author Bar Harush
 */
public class RestaurantController implements Initializable {
	
	/**
	 * Button to navigate to the order management page.
	 */
	@FXML private Button orderManagementButton;

	/**
	 * Button to navigate to the menu update page.
	 */
	@FXML private Button menuUpdateButton;

	/**
	 * Button to initiate the logout process.
	 */
	@FXML private Button logoutButton;

	/**
	 * Controller for handling client-side operations and communication with the server.
	 */
	private ClientController clientController;

	/**
	 * Stage for the login screen, used for navigation after logout.
	 */
	private Stage loginStage;

	/**
	 * Controller for handling the logout process.
	 */
	private LogoutController logoutController;

	/**
	 * The currently logged-in user, typically a restaurant owner or manager.
	 */
	private User currentUser;
	    
	    /**
	     * Initializes the RestaurantController class. This method is automatically called
	     * after the FXML file has been loaded.
	     *
	     * @param location The location used to resolve relative paths for the root object, or null if unknown.
	     * @param resources The resources used to localize the root object, or null if not localized.
	     */
		@Override
		public void initialize(URL location, ResourceBundle resources) {
			orderManagementButton.setOnAction(event -> handleOrderManagementButton());
			menuUpdateButton.setOnAction(event -> handleMenuUpdateButton());
	        logoutButton.setOnAction(event -> handleLogoutButton());
	        logoutController = LogoutController.getInstance();
		}
		
		/**
	     * Sets the ClientController for this RestaurantController.
	     * @param clientController The ClientController to be used for server communication.
	     */
		public void setClientController(ClientController clientController) {
		    this.clientController = clientController;
			if (logoutController == null) {
		        logoutController = LogoutController.getInstance();
		    }
		    logoutController.setClientController(clientController);
		}
		
	    /**
		 * Sets the login stage for this RestaurantController.
		 * This method is used to store a reference to the login stage,
		 * allowing the controller to return to the login screen after logout.
		 *
		 * @param loginStage The Stage object representing the login screen.
		 */
		public void setLoginStage(Stage loginStage) {
			this.loginStage = loginStage;
		}
	    
	    public void setCurrentUser(User user) {
	        this.currentUser = user;
	    }
		
	    /**
	     * Handles the order management button action.
	     * Opens the order management window and hides the current restaurant stage.
	     */
	    public void handleOrderManagementButton () {
	    	System.out.println("Order management is clicked");
	    	System.out.println("Current restaurant ID: " + this.currentUser.getUserId());
	    	openOrderManagementWindow();
	    }
	    
	    /**
	     * Handles the menu update button action.
	     */
	    @FXML
	    private void handleMenuUpdateButton() {
	        System.out.println("Menu Update is clicked");
	        openMenuUpdateWindow();
	    }

	    /**
	     * Opens the order management window.
	     * This method loads the OrderManagementPage.fxml, sets up the controller,
	     * and displays the new stage while hiding the current restaurant stage.
	     */
	    private void openOrderManagementWindow() {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/OrderManagementPage.fxml"));
	            Parent root = loader.load();
	            OrderManagementController orderManagementController = loader.getController();
	            orderManagementController.setClientController(this.clientController);
	            orderManagementController.setRestaurantId(this.currentUser.getUserId());
	            Stage orderManagementStage = new Stage();
	            orderManagementStage.setTitle("Order Management");
	            orderManagementStage.setScene(new Scene(root));
	            orderManagementStage.show();

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    /**
	     * Opens the menu update window.
	     * This method loads the MenuUpdatePage.fxml, sets up the controller,
	     * and displays the new stage while hiding the current restaurant stage.
	     */
		private void openMenuUpdateWindow() {
			try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/MenuUpdatePage.fxml"));
	            Parent root = loader.load();
	            MenuUpdateController menuUpdateController = loader.getController();
	            menuUpdateController.setClientController(this.clientController);
	            if (this.currentUser != null) {
	            	menuUpdateController.setCurrentRestaurantId(this.currentUser.getUserId());
	            } else {
	                System.out.println("Warning: Current user is null");
	                return;
	            }
	            Stage  menuUpdateStage = new Stage();
	            menuUpdateStage.setTitle("Menu Update Page");
	            menuUpdateStage.setScene(new Scene(root));
	            menuUpdateStage.show();
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}

		/**
	     * Handles the action when the logout button is clicked.
	     */
	    @FXML
	    private void handleLogoutButton() {
	        System.out.println("Logout button clicked");
	        Stage stage = (Stage) logoutButton.getScene().getWindow();
	        boolean logoutSuccessful = logoutController.logout(stage);
	        if (logoutSuccessful) {
	            System.out.println("Logout successful");
	            stage.close(); // Close manager page
	            Platform.runLater(() -> {
	                if (loginStage != null) {
	                    loginStage.show(); // Show login page
	                } else {
	                    new LoginController().start(new Stage());
	                }
	            });
	        } else {
	            System.out.println("Logout failed");
	        }
	    }
  
	    /**
	     * Starts the Restaurant page of the application.
	     * This method loads the RestaurantPage.fxml, sets up the controller,
	     * and displays the stage.
	     * 
	     * @param primaryStage The primary stage for this application, onto which
	     * the application scene can be set.
	     */
	    public void start(Stage primaryStage) {
	    	try {
	    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/RestaurantPage.fxml"));
	    		Pane root = loader.load();
	    		RestaurantController controller = loader.getController();
	    		controller.setClientController(this.clientController);
	            controller.setCurrentUser(this.currentUser);
	    		primaryStage.setTitle("Bite Me - Resturant");
	            Scene scene = new Scene(root);
	            primaryStage.setScene(scene);
	            primaryStage.sizeToScene();
	            primaryStage.show();
	    	} catch (IOException e) {e.printStackTrace();}
	    }
}