package controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import client.ClientController;
import entities.Message;
import entities.User;
import entities.User.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * Controller class for the Register new customer screen of the Bite Me application.
 * @author Liroy Ben Shimon
 */
public class RegisterCustomerController implements Initializable{

	/**
	 * Text field for entering the customer's first name.
	 */
	@FXML private TextField firstnameField;

	/**
	 * Text field for entering the customer's last name.
	 */
	@FXML private TextField lastnameField;

	/**
	 * Text field for entering the customer's ID number.
	 */
	@FXML private TextField idField;

	/**
	 * Text field for entering the customer's phone number.
	 */
	@FXML private TextField phoneField;

	/**
	 * Text field for entering the customer's email address.
	 */
	@FXML private TextField emailField;

	/**
	 * Button to submit the registration form.
	 */
	@FXML private Button registerButton;

	/**
	 * Button to return to the previous screen.
	 */
	@FXML private Button backButton;

	/**
	 * Label for displaying error messages or validation feedback.
	 */
	@FXML private Label errorLabel;

	/**
	 * ComboBox for selecting the user type (CUSTOMER_BUSINESS or CUSTOMER_PRIVATE).
	 */
	@FXML private ComboBox<UserRole> userTypeComboBox;

	/**
	 * Controller for handling client-side operations and communication with the server.
	 */
	private ClientController clientController;

	/**
	 * Stage for the manager's screen, used for navigation.
	 */
	private Stage managerStage;

	/**
	 * The main stage for this registration screen.
	 */
	private Stage stage;
	
    /**
     * Initializes the RegisterCustomerController class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (userTypeComboBox != null) {
            userTypeComboBox.getItems().add(UserRole.CUSTOMER_BUSINESS);
            userTypeComboBox.getItems().add(UserRole.CUSTOMER_PRIVATE);
        } else {
            System.err.println("userTypeComboBox is null in initialize method");
        }
    }
	
    /**
     * Sets the Stage for this RegisterCustomerController.
     * @param stage The Stage in which this controller's UI is displayed.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
	
    /**
     * Sets the ClientController for this RegisterCustomerController.
     * @param clientController The ClientController to be used for server communication.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    
    /**
     * Sets the managerStage for this RegisterCustomerController.
     * @param managerStage The Stage in which manager controller's UI is displayed.
     */
    public void setManagerStage(Stage managerStage) {
        this.managerStage = managerStage;
    }
    
    /**
     * Handles the register button action.
     * Validates input and sends a new customer registration request to the server.
     */
    @FXML
    private void handleRegister() {
        System.out.println("Register button clicked");
        if (validateInput()) {
            System.out.println("Input validated");
            String userId = idField.getText();
            String firstName = firstnameField.getText();
            String lastName = lastnameField.getText();
            String email = emailField.getText();
            String phoneNumber = phoneField.getText();
            String password = idField.getText().substring(0, 4); // Generate a default password
            UserRole role = userTypeComboBox.getValue();
            BigDecimal initialCredit = BigDecimal.ZERO;

            User newUser;

            if (role == UserRole.CUSTOMER_PRIVATE) {
                // Create a custom dialog for credit card input
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Credit Card Information");
                dialog.setHeaderText("Please enter your credit card number:");
                dialog.setContentText("Credit Card Number:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    String creditCard = result.get();
                    if (isValidCreditCard(creditCard)) {
                        newUser = new User(userId, phoneNumber, creditCard, UserRole.CUSTOMER_PRIVATE, password, initialCredit);
                    } else {
                        showErrorMessage("Invalid credit card number. Please try again.");
                        return;
                    }
                } else {
                    showErrorMessage("Credit card information is required for private customers.");
                    return;
                }
            } else {
                newUser = new User(userId, firstName, lastName, email, phoneNumber, password, role, initialCredit);
            }

            System.out.println("User created: " + newUser);

            Message<?> response = clientController.handleNewCustomerRegistration(newUser);
            if (response != null) {
                System.out.println("Received response from server: " + response.getMessage());
                switch (response.getMessage()) {
                    case "NEW_CUSTOMER_REGISTRATION_SUCCESS":
                        showSuccessMessage("Customer registered successfully!");
                        clearFields();
                        break;
                    case "NEW_CUSTOMER_REGISTRATION_FAILED":
                        showErrorMessage("Failed to register customer. Please try again.");
                        break;
                    case "NEW_CUSTOMER_REGISTRATION_ERROR":
                        showErrorMessage("An error occurred during customer registration: " + response.getType());
                        break;
                    default:
                        showErrorMessage("Unexpected response from server.");
                        break;
                }
            }
        }
    }

    /**
     * Displays a success message to the user.
     * @param message The success message to be displayed.
     */
    private void showSuccessMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * Displays an error message to the user.
     * @param message The error message to be displayed.
     */
    private void showErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * Clears all input fields in the registration form.
     * This method is typically called after a successful registration
     * to prepare the form for the next user input.
     */
    private void clearFields() {
        firstnameField.clear();
        lastnameField.clear();
        idField.clear();
        phoneField.clear();
        emailField.clear();
    }
    
    /**
     * Validates the input fields.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInput() {
        if (firstnameField.getText().isEmpty() || lastnameField.getText().isEmpty() ||
            idField.getText().isEmpty() || phoneField.getText().isEmpty() ||
            emailField.getText().isEmpty()) {
            errorLabel.setText("All fields are required.");
            return false;
        }
        
        // check first name.
        if (!isValidName(firstnameField.getText())) {
            errorLabel.setText("First name must contain only letters.");
            return false;
        }

        // check last name.
        if (!isValidName(lastnameField.getText())) {
            errorLabel.setText("Last name must contain only letters.");
            return false;
        }

        // check id.
        if (!isValidID(idField.getText())) {
            errorLabel.setText("ID must contain 9 digits exactly.");
            return false;
        }
        
        //check phone number.
        if (!isValidPhoneNumber(phoneField.getText())) {
            errorLabel.setText("Phone number must include 10 digits and start with '05'.");
            return false;
        }

        // check email.
        if (!isValidEmail(emailField.getText())) {
            errorLabel.setText("The email address is not valid.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates a name to ensure it contains only letters.
     * @param name The name of the new user.
     * @return true if the name contains only letters.
     */
    private boolean isValidName(String name) {
        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validates an ID number to ensure it contains exactly 9 digits.
     * @param id The ID number of the new user.
     * @return true if the ID contains exactly 9 digits.
     */
    private boolean isValidID(String id) {
        if (id.length() != 9) {
            return false;
        }
        for (char c : id.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Validates a phone number to ensure it starts with '05' and contains 10 digits.
     * @param phone The phone number of the new user.
     * @return true if the phone number is valid.
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone.length() != 10 || !phone.startsWith("05")) {
            return false;
        }
        for (char c : phone.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Performs a basic validation of an email address.
     * Checks if the email contains '@' and '.' in the correct order.
     * @param email The email address to validate.
     * @return true if the email passes the basic validation, false otherwise.
     */
    private boolean isValidEmail(String email) {
        int atIndex = email.indexOf('@');
        int dotIndex = email.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex && dotIndex < email.length() - 1;
    }
    
    /**
     * Validates a credit card number to ensure it contains exactly 16 digits.
     * @param creditCard The credit card number of the new user.
     * @return true if the credit card number contains exactly 16 digits.
     */
    private boolean isValidCreditCard(String creditCard) {
        if (creditCard.length() != 16) {
            return false;
        }
        for (char c : creditCard.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Handles the back button action.
     * Closes the current registration window and shows the manager screen.
     */
    @FXML
    private void handleBack() {
        if (stage != null) {
            stage.close();
        }
        if (managerStage != null) {
            managerStage.show();
        } else {
            System.out.println("Warning: Manager stage is null");
        }
    }
}