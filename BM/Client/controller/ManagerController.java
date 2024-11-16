package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.ClientController;
import entities.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controller class for the Manager screen of the Bite Me application.
 * This class handles the functionality of the manager's main page.
 * @author Liroy Ben Shimon
 */
public class ManagerController implements Initializable {
	
	/**
	 * Button to navigate to the report management page.
	 */
	@FXML private Button manageReportButton;

	/**
	 * Button to open the new customer registration form.
	 */
	@FXML private Button newRegisterButton;

	/**
	 * Button to initiate the logout process.
	 */
	@FXML private Button logoutButton;

	/**
	 * Controller for handling client-side operations and communication with the server.
	 */
	private ClientController clientController;

	/**
	 * Controller for handling the logout process. Initialized with a singleton instance.
	 */
	private LogoutController logoutController = LogoutController.getInstance();

	/**
	 * Stage for the login screen, used for navigation after logout.
	 */
	private Stage loginStage;

	/**
	 * The currently logged-in user, typically a manager.
	 */
	private User currentUser;
    
    /**
     * Initializes the ManagerController class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		manageReportButton.setOnAction(event -> handleManageReportButton());
	    newRegisterButton.setOnAction(event -> handleNewRegisterButton());
        logoutButton.setOnAction(event -> handleLogoutButton());
        logoutController = LogoutController.getInstance();
	}
    
    /**
     * Sets the ClientController for this ManagerController.
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
     * Sets the current user object this ManagerController.
     * @param user The current user object.
     */
	public void setCurrentUser(User currentUser) {
	    this.currentUser = currentUser;
	    if (this.currentUser == null) {
	        System.out.println("Warning: currentUser is null in ManagerController");
	    }
	}
	
	/**
	 * Sets the login stage for this ManagerController.
	 * This method is used to store a reference to the login stage,
	 * allowing the controller to return to the login screen after logout.
	 *
	 * @param loginStage The Stage object representing the login screen.
	 */
	public void setLoginStage(Stage loginStage) {
		this.loginStage = loginStage;
	}
	
    /**
     * Handles the action when the Manage Report button is clicked.
     */
    @FXML
    private void handleManageReportButton() {
        System.out.println("Report management is clicked");
        openReportManagementWindow();
    }
    
    /**
     * Handles the action when the New Register button is clicked.
     */
    @FXML
    private void handleNewRegisterButton() {
        System.out.println("New customer register is clicked");
        openNewCustomerRegistrationForm();
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
     * Opens the Report Management window.
     * This method loads the ReportManagement.fxml file and displays it in a new stage.
     */
    private void openReportManagementWindow() {
        if (currentUser == null) {
            showAlert("Error", "User information is not available. Please try logging in again.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/ReportManagerPage.fxml"));
            Parent root = loader.load();
            
            ReportController reportController = loader.getController();
            reportController.setClientController(this.clientController);
            reportController.setUser(currentUser);
            
            Stage reportStage = new Stage();
            reportController.setStage(reportStage);
            
            Stage managerStage = (Stage) newRegisterButton.getScene().getWindow();
            reportController.setManagerStage(managerStage);
            
            reportStage.setTitle("Report Management");
            reportStage.setScene(new Scene(root));
            reportStage.show();
            
            // Hide the manager stage
            managerStage.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the New Customer Registration window.
     * This method loads the RegisterNewCustomerPage.fxml file and displays it in a new stage.
     */
    private void openNewCustomerRegistrationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/RegisterNewCustomerPage.fxml"));
            Parent root = loader.load();
            
            RegisterCustomerController registerController = loader.getController();
            registerController.setClientController(this.clientController);
            
            Stage registrationStage = new Stage();
            registerController.setStage(registrationStage);
            
            Stage managerStage = (Stage) newRegisterButton.getScene().getWindow();
            registerController.setManagerStage(managerStage);
            
            registrationStage.setTitle("New Customer Registration");
            registrationStage.setScene(new Scene(root));
            registrationStage.show();
            
            // Hide the manager stage
            managerStage.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Displays an error alert dialog with the specified title and content.
     * The alert is shown as an error message and waits for the user to acknowledge it.
     *
     * @param title   the title of the alert dialog window
     * @param content the content text to be displayed within the alert
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Displays the manager screen.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     */
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/boundry/ManagerPage.fxml"));
            Pane root = loader.load();
            ManagerController controller = loader.getController();
            controller.setClientController(this.clientController);
            controller.setCurrentUser(this.currentUser);

            primaryStage.setTitle("Bite Me - Manager");
            primaryStage.setScene(new Scene(root, 400, 600));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}