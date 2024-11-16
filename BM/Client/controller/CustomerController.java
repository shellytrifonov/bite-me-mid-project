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
 * Controller class for the customer screen of the Bite Me application.
 * This class handles the functionality of the customer's main page.
 * @author Shelly Trifonov
 */
public class CustomerController implements Initializable {
	
    /** Button to create a new order */
	@FXML private Button createOrderButton;
    
    /** Button to view existing orders */
	@FXML private Button viewOrdersButton;
    
    /** Button to log out */
    @FXML private Button logoutButton;
    
    /** Controller for client-server communication */
    private ClientController clientController;
    
    /** Controller for handling logout operations */
    private LogoutController logoutController = LogoutController.getInstance();
    
    /** Stage for the login screen */
    private Stage loginStage;
    
    /** Current user of the application */
    private User currentUser;
    
    /**
     * Initializes the CustomerController class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		createOrderButton.setOnAction(event -> openCreateOrderWindow());
		viewOrdersButton.setOnAction(event -> openViewOrdersWindow());
        logoutButton.setOnAction(event -> handleLogoutButton());
        logoutController = LogoutController.getInstance();
	}
	
    /**
     * Sets the ClientController for this CustomerController.
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
	 * Sets the login stage for this CustomerController.
	 * This method is used to store a reference to the login stage,
	 * allowing the controller to return to the login screen after logout.
	 *
	 * @param loginStage The Stage object representing the login screen.
	 */
	public void setLoginStage(Stage loginStage) {
		this.loginStage = loginStage;
	}
	
    /**
     * Sets the current user for this CustomerController.
     * @param user The User object representing the current user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
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
            stage.close(); // Close customer page
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
     * Handles the action when the Create order button is clicked.
     */
    @FXML
    private void handleCreateOrderButton() {
        System.out.println("Create new order is clicked");
        openCreateOrderWindow();
    }
    
    /**
     * Handles the action when the View orders button is clicked.
     */
    @FXML
    private void handleViewOrdersButton() {
        System.out.println("View orders is clicked");
        openViewOrdersWindow();
    }
    
    /**
     * Opens the Create Order window.
     * This method loads the OrderPage.fxml file and displays it in a new stage.
     */
    private void openCreateOrderWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/OrderPage.fxml"));
            Parent root = loader.load();
            
            OrderController orderController = loader.getController();
            orderController.setClientController(this.clientController);
            orderController.initializeAfterClientControllerSet();
            
            Stage orderStage = new Stage();
            orderController.setCustomerStage((Stage) createOrderButton.getScene().getWindow());
            
            orderStage.setTitle("New Order");
            orderStage.setScene(new Scene(root));
            orderStage.show();
            
            // Hide the customer stage
            ((Stage) createOrderButton.getScene().getWindow()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the View Orders window.
     * This method loads the OrderPage.fxml file and displays it in a new stage.
     */
    private void openViewOrdersWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/ViewOrders.fxml"));
            Parent root = loader.load();
            
            OrderViewController orderViewController = loader.getController();
            orderViewController.setClientController(this.clientController);
            
            if (this.currentUser != null) {
                orderViewController.setCurrentUserId(this.currentUser.getUserId());
            } else {
                System.out.println("Warning: Current user is null");
                return;
            }
            
            Stage orderViewStage = new Stage();
            orderViewController.setStage(orderViewStage);
            
            Stage customerStage = (Stage) createOrderButton.getScene().getWindow();
            orderViewController.setCustomerStage(customerStage);
            
            orderViewStage.setTitle("View Orders");
            orderViewStage.setScene(new Scene(root));
            orderViewStage.show();
            
            // Hide the customer stage
            customerStage.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Displays the customer screen.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     */
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/CustomerPage.fxml"));
            Pane root = loader.load();
            CustomerController controller = loader.getController();
            controller.setClientController(this.clientController);
            controller.setCurrentUser(this.currentUser);

            primaryStage.setTitle("Bite Me - Customer");
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}