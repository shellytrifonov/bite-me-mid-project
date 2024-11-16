package controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import client.ClientController;
import entities.Message;
import entities.Order;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Controller class for the view orders screen of the Bite Me application.
 * This class handles the functionality of the customer's view orders page.
 * @author Shelly Trifonov
 */
public class OrderViewController implements Initializable, PropertyChangeListener  {
	
	/**
	 * The button that allows the user to go back to the previous screen.
	 */
	@FXML private Button backButton;

	/**
	 * The ListView that displays a list of orders for the customer.
	 */
	@FXML private ListView<Order> ordersListView;

	/**
	 * The label that is displayed when there are no orders available for the customer.
	 */
	@FXML private Label noOrdersLabel;

	/**
	 * The button that allows the user to confirm receipt of a selected order.
	 */
	@FXML private Button confirmReceiveButton;

   
    /** 
     * The ClientController instance used for communication with the server. 
     */
    private ClientController clientController;
    
    /** 
     * The Stage in which the customer controller's UI is displayed. 
     */
    private Stage customerStage;
    
    /** 
     * The current Stage in which this controller's UI is displayed. 
     */
    private Stage stage;
    
    /** 
     * The ID number of the current user. 
     */
    private String currentUserId;
    
    /** 
     * The utility for handling communication between controllers. 
     */
    private ControllerCommunicationUtility communicationUtility;

    /**
     * Initializes the OrderViewController class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        backButton.setOnAction(event -> handleBack());
        confirmReceiveButton.setOnAction(event -> handleConfirmReceive());
        ordersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showOrderDetails(newValue);
            }
        });
        setupListView();
        communicationUtility = ControllerCommunicationUtility.getInstance();
        communicationUtility.addPropertyChangeListener(this);
    }
    
    /**
     * Handles the action when the confirm receive button is clicked.
     * If an order is selected, it initiates the confirmation process for order receipt.
     */
    @FXML
    private void handleConfirmReceive() {
        Order selectedOrder = ordersListView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            confirmOrderReceipt(selectedOrder.getOrderId());
        } else {
            showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order to confirm receipt.");
        }
    }
    /**
     * Sets the Stage for this OrderViewController.
     * @param stage The Stage in which this controller's UI is displayed.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
	
    /**
     * Sets the ClientController for this OrderViewController.
     * @param clientController The ClientController to be used for server communication.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    
    /**
     * Sets the customerStage for this OrderViewController.
     * @param customerStage The Stage in which customer controller's UI is displayed.
     */
    public void setCustomerStage(Stage customerStage) {
        this.customerStage = customerStage;
    }
    
    /**
     * Sets the current user id number for this OrderViewController.
     * @param userId The id number of the current user.
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        loadOrders();
    }
    
    /**
     * Handles property change events. This method is triggered when a property change event is fired.
     * 
     * @param evt The property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("message".equals(evt.getPropertyName())) {
            Message<?> message = (Message<?>) evt.getNewValue();
            handleMessage(message);
        }
    }
    
    /**
     * Processes incoming messages and updates the UI or performs actions accordingly.
     * 
     * @param message The message received from the server.
     */
    private void handleMessage(Message<?> message) {
        switch (message.getMessage()) {
            case "ORDER_ACCEPTED":
            case "ORDER_READY":
            case "ORDER_IN_DELIVERY":
                int orderId = (int) message.getType();
                Platform.runLater(() -> {
                    showAlert(AlertType.INFORMATION, "Order Update", getOrderUpdateMessage(message.getMessage(), orderId));
                    loadOrders(); // Refresh the order list
                });
                break;
            case "CLIENT_MESSAGE":
                if (message.getType() instanceof Message) {
                    @SuppressWarnings("unchecked")
					Message<String> clientMessage = (Message<String>) message.getType();
                    String targetCustomerId = clientMessage.getMessage();
                    String messageContent = clientMessage.getType();
                    if (targetCustomerId.equals(currentUserId)) {
                        Platform.runLater(() -> {
                            showAlert(AlertType.INFORMATION, "Message from Restaurant", messageContent);
                            int extractedOrderId = extractOrderId(messageContent);
                            if (extractedOrderId != -1 && (messageContent.contains("is ready") || messageContent.contains("out for delivery"))) {
                                showConfirmationDialog(extractedOrderId);
                            }
                        });
                    }
                }
                break;
        }
    }

    /**
     * Extracts the order ID from the given message string.
     * 
     * @param message The message containing the order ID.
     * @return The extracted order ID, or -1 if not found.
     */
    private int extractOrderId(String message) {
        String[] words = message.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("#") && i + 1 < words.length) {
                try {
                    return Integer.parseInt(words[i + 1]);
                } catch (NumberFormatException e) {
                    // Not a number, continue searching
                }
            }
        }
        return -1; // Return -1 if no order ID is found
    }

    /**
     * Constructs an appropriate message based on the order update type.
     * 
     * @param messageType The type of message received.
     * @param orderId The ID of the order being updated.
     * @return A string representing the order update message.
     */
    private String getOrderUpdateMessage(String messageType, int orderId) {
        switch (messageType) {
            case "ORDER_ACCEPTED":
                return "Order #" + orderId + " has been accepted by the restaurant.";
            case "ORDER_READY":
                return "Order #" + orderId + " is ready for pickup or delivery.";
            default:
                return "Order #" + orderId + " has been updated.";
        }
    }
    
    /**
     * Displays a confirmation dialog asking the user if they have received their order.
     * 
     * @param orderId The ID of the order to confirm.
     */
    private void showConfirmationDialog(int orderId) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Order Receipt");
        alert.setHeaderText("Have you received your order?");
        alert.setContentText("Please confirm if you have received Order #" + orderId);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                confirmOrderReceipt(orderId);
            }
        });
    }

    /**
     * Sends a confirmation message to the server that the order has been received.
     * 
     * @param orderId The ID of the order being confirmed.
     */
    private void confirmOrderReceipt(int orderId) {
        // Send a request to update the order status to "DELIVERED"
        Message<?> response = clientController.handleUpdateOrderStatus(orderId, "DELIVERED");
        System.out.println(response.getMessage());
        // Check the response from the server
        if (response != null && "UPDATE_ORDER_STATUS_RESPONSE".equals(response.getMessage())) {
            loadOrders(); // Refresh the order list
            showAlert(Alert.AlertType.INFORMATION, "Confirmation", "Order #" + orderId + " has been marked as delivered.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to confirm the delivery for Order #" + orderId + ". Please try again.");
        }
    }



    /**
     * Configures the ListView to display orders with custom formatting.
     */
    private void setupListView() {
        ordersListView.setCellFactory(new Callback<ListView<Order>, ListCell<Order>>() {
            @Override
            public ListCell<Order> call(ListView<Order> param) {
                return new ListCell<Order>() {
                    @Override
                    protected void updateItem(Order item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText("Order #" + item.getOrderId() + " - " + item.getStatus() + 
                                    " - " + item.getDeliveryType());
                        }
                    }
                };
            }
        });
    }
	
    /**
     * Handles the back button action.
     * Closes the current window and shows the customer screen.
     */
    @FXML
    private void handleBack() {
        if (stage != null) {
            stage.close();
        }
        if (customerStage != null) {
            customerStage.show();
        } else {
            System.out.println("Warning: Customer stage is null");
        }
    }
    
    /**
     * Shows an alert dialog with the given type, title, and content.
     *
     * @param alertType The type of the alert (e.g., WARNING, ERROR, INFORMATION).
     * @param title The title of the alert dialog.
     * @param content The content message of the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
   
    /**
     * Loads the orders for the current user and displays them in the UI.
     */
    public void loadOrders() {
        if (currentUserId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "User ID not set. Unable to load orders.");
            return;
        }
        Message<String> getMessage = new Message<>("GET_CUSTOMER_ORDERS", currentUserId);
        clientController.accept(getMessage);
        Message<?> response = clientController.getResponse("GET_CUSTOMER_ORDERS");
        if (response != null && response.getType() instanceof List) {
            @SuppressWarnings("unchecked")
			List<Order> orders = (List<Order>) response.getType();
            Platform.runLater(() -> {
                if (orders.isEmpty()) {
                    ordersListView.setVisible(false);
                    noOrdersLabel.setText("You have no orders yet.");
                    noOrdersLabel.setVisible(true);
                } else {
                    ordersListView.getItems().clear();
                    ordersListView.getItems().addAll(orders);
                    ordersListView.setVisible(true);
                    noOrdersLabel.setVisible(false);
                }
            });
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders.");
        }
    }
    
    /**
     * Displays a dialog with the details of the specified order.
     * 
     * @param order the order for which details are to be displayed
     */
    private void showOrderDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details");
        dialog.setHeaderText("Details for Order #" + order.getOrderId());

        TextArea textArea = new TextArea(order.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        if (order.getStatus() == Order.OrderStatus.READY || order.getStatus() == Order.OrderStatus.IN_DELIVERY) {
            ButtonType confirmButton = new ButtonType("Confirm Receipt", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(confirmButton);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButton) {
                    confirmOrderReceipt(order.getOrderId());
                }
                return null;
            });
        }

        dialog.showAndWait();
    }
}