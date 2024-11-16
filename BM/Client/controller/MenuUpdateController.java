package controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientController;
import entities.MenuItem;
import entities.Message;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for the menu update functionality.
 * This class handles the UI and logic for updating menu items in a restaurant.
 * @author Guy Naeh
 */
public class MenuUpdateController implements Initializable {

	/**
	 * ComboBox for selecting menu items to update.
	 */
	@FXML private ComboBox<MenuItem> menuItemsComboBox;

	/**
	 * TextField for displaying and editing the selected menu item's ID.
	 */
	@FXML private TextField itemIdField;

	/**
	 * TextField for displaying and editing the selected menu item's price.
	 */
	@FXML private TextField priceField;

	/**
	 * TextField for displaying and editing the selected menu item's quantity.
	 */
	@FXML private TextField quantityField;

	/**
	 * Button to trigger the update process for the selected menu item.
	 */
	@FXML private Button updateButton;

	/**
	 * Controller for handling client-side operations and communication with the server.
	 */
	private ClientController clientController;

	/**
	 * The ID of the current restaurant whose menu is being updated.
	 */
	private String currentRestaurantId;

    /**
     * Sets the client controller for this menu update controller.
     *
     * @param clientController The client controller to be used for server communication.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    
    /**
     * Sets the current restaurant ID and loads its menu items.
     *
     * @param restaurantId The ID of the current restaurant.
     */
    public void setCurrentRestaurantId(String restaurantId) {
        if (restaurantId == null) {
            System.out.println("Warning: Attempt to set null restaurant ID in MenuUpdateController");
            return;
        }
        this.currentRestaurantId = restaurantId;
        System.out.println("Current restaurant ID set in MenuUpdateController: " + restaurantId);
        loadMenuItems();
    }

    /**
     * Initializes the controller. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	menuItemsComboBox.setPromptText("Choose Item");
        updateButton.setOnAction(event -> handleUpdateMenuItem());
        menuItemsComboBox.setOnAction(event -> handleMenuItemSelection());
    }

    /**
     * Loads menu items for the current restaurant from the server.
     */
    @SuppressWarnings("unchecked")
	private void loadMenuItems() {
        try {
            System.out.println("Current Restaurant ID: " + currentRestaurantId);
            Message<?> response = clientController.getMenuItems(currentRestaurantId);

            if (response != null && response.getType() instanceof List<?>) {
                List<MenuItem> menuItems = (List<MenuItem>) response.getType();
                menuItemsComboBox.getItems().clear();
                menuItemsComboBox.getItems().addAll(menuItems);
            } else {
                showAlert(AlertType.INFORMATION, "No Items", "No menu items found.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Error loading menu items: " + e.getMessage());
        }
    }

    /**
     * Handles the selection of a menu item from the combo box.
     * Updates the item details in the text fields.
     */
    @FXML
    private void handleMenuItemSelection() {
        MenuItem selectedItem = menuItemsComboBox.getValue();
        if (selectedItem != null) {
            itemIdField.setText(String.valueOf(selectedItem.getItemId()));
            priceField.setText(selectedItem.getPrice().toString());
            quantityField.setText(String.valueOf(selectedItem.getQuantity()));
        }
    }

    /**
     * Handles the update menu item action.
     * Validates input, sends update request to server, and processes the response.
     */
    @FXML
    private void handleUpdateMenuItem() {
        System.out.println("Update button clicked");
        if (validateInput()) {
            System.out.println("Input validated");
            int itemId = Integer.parseInt(itemIdField.getText());
            BigDecimal price = new BigDecimal(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());

            MenuItem updatedItem = new MenuItem();
            updatedItem.setItemId(itemId);
            updatedItem.setPrice(price);
            updatedItem.setQuantity(quantity);

            Object[] updateData = new Object[] { itemId, price, quantity, currentRestaurantId };
            Message<Object[]> updateMessage = new Message<>("UPDATE_MENU_ITEM", updateData);

            Message<?> response = clientController.updateMenuItem(updateMessage);

            if (response != null) {
                switch (response.getMessage()) {
                    case "ITEM_UPDATED":
                        showSuccessMessage("Menu item updated successfully!");
                        loadMenuItems(); // Reload the menu items to show the updated data
                        break;
                    case "ITEM_NOT_FOUND":
                        showErrorMessage("No matching item found.");
                        break;
                    case "UPDATE_FAILED":
                        showErrorMessage("Failed to update item.");
                        break;
                    default:
                        showErrorMessage("Unexpected response from server.");
                        break;
                }
                
                // Close the current stage
                Stage currentStage = (Stage) updateButton.getScene().getWindow();
                currentStage.close();
            } else {
                showErrorMessage("No response from server.");
            }
        }
    }

    /**
     * Validates the input in the text fields.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInput() {
        if (itemIdField.getText().isEmpty() || priceField.getText().isEmpty() || quantityField.getText().isEmpty()) {
            showErrorMessage("All fields must be filled.");
            return false;
        }

        try {
            Integer.parseInt(itemIdField.getText());
        } catch (NumberFormatException e) {
            showErrorMessage("Item ID must be a valid number.");
            return false;
        }

        try {
            new BigDecimal(priceField.getText());
        } catch (NumberFormatException e) {
            showErrorMessage("Price must be a valid decimal number.");
            return false;
        }

        try {
            Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showErrorMessage("Quantity must be a valid number.");
            return false;
        }

        return true;
    }

    /**
     * Shows a success message alert.
     *
     * @param message The success message to display.
     */
    private void showSuccessMessage(String message) {
        showAlert(AlertType.INFORMATION, "Success", message);
    }

    /**
     * Shows an error message alert.
     *
     * @param message The error message to display.
     */
    private void showErrorMessage(String message) {
        showAlert(AlertType.ERROR, "Error", message);
    }

    /**
     * Shows an alert dialog with the specified type, title, and message.
     *
     * @param alertType The type of the alert.
     * @param title The title of the alert.
     * @param message The message to display in the alert.
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}